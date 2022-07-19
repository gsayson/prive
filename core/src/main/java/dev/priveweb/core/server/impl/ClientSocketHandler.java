package dev.priveweb.core.server.impl;

import dev.priveweb.core.exception.FaultRecoveryStrategy;
import dev.priveweb.core.exception.HandlerNotFoundException;
import dev.priveweb.core.exception.MalformedRequestException;
import dev.priveweb.core.exception.TransferEncodingNotImplementedException;
import dev.priveweb.core.exposure.PriveWebServerExpose;
import dev.priveweb.core.protocol.HTTPProtocol;
import dev.priveweb.core.util.HeaderUtils;
import lombok.SneakyThrows;
import dev.priveweb.core.http.Header;
import dev.priveweb.core.http.request.HTTPRequest;
import dev.priveweb.core.http.request.QueryParameters;
import dev.priveweb.core.http.request.RequestMethod;
import dev.priveweb.core.http.response.HTTPResponse;
import dev.priveweb.core.mapper.MappingResolver;
import dev.priveweb.core.util.Checks;
import dev.priveweb.core.util.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler for client sockets.
 */
class ClientSocketHandler implements Runnable {

	private final Socket sock;
	private final boolean verbose;
	private final PriveWebServer server;
	private final FaultRecoveryStrategy faultRecoveryStrategy;
	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(PriveWebServer.class));
	private static final Unsafe unsafe;

	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (Unsafe) f.get(null);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new InternalError(e);
		}
	}

	ClientSocketHandler(Socket socket, boolean verbose, PriveWebServer server, FaultRecoveryStrategy faultRecoveryStrategy) {
		this.sock = socket;
		this.verbose = verbose;
		this.server = server;
		this.faultRecoveryStrategy = faultRecoveryStrategy;
	}

	@Override
	@SneakyThrows(IOException.class)
	public void run() {
		try(
				Socket socket = sock;
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1)); // use a superset of US-ASCII to comply with the HTTP spec
				PrintStream out = new PrintStream(socket.getOutputStream())
		) {
			String requested;

			String input = in.readLine();
			List<String> headers = new LinkedList<>();
			String line;
			boolean headersParsed = false; // whether the headers are already parsed.
			while(in.ready() && (line = in.readLine()) != null) {
				if(!headersParsed) {
					if(line.isBlank()) {
						headersParsed = true;
						// ready to move on to next line
					} else {
						line = line.stripLeading(); // if \r exists then remove it
						if(line.split(":")[0].isBlank()) continue;
						headers.add(
								line.replace("\0", "\s")
										.replace("\r", "\s")
										.replace("\n", "\s")
						);
					}
				}
			}
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // get HTTP method
			if(verbose) //noinspection PlaceholderCountMatchesArgumentCount
				logger.info("Handling {} request from {highlight}", method, socket.getRemoteSocketAddress());
			requested = parse.nextToken().toLowerCase();
			String queryParams = "";
			int endIndex = requested.lastIndexOf("?");
			if(endIndex != -1) {
				String temp = requested.substring(0, endIndex);
				queryParams = requested.replace(temp, "");
				requested = temp;
			}

			var hList = headers.parallelStream().map(s -> {
				var s1 = s.split(":");
				var sb = new StringBuilder();
				for(int i = 1; i < s1.length; i++) {
					String s2 = s1[i];
					if(s2.endsWith("\r\n")) {
						s2 = s2.substring(0, s2.lastIndexOf("\r\n") - 1);
					} else if(s2.endsWith("\n")) {
						s2 = s2.substring(0, s2.lastIndexOf("\n") - 1);
					}
					sb.append(s2);
				}
				return new Header(s1[0], sb.toString().stripLeading());
			}).toList();

			Object responseObject = null;
			@Nullable String requestBody;

			HTTPRequest.HTTPRequestBuilder builder = HTTPRequest.builder();
			HTTPRequest httpRequest = builder.protocol("HTTP/1.1")
					.headers(hList)
					.requestedResource(requested)
					.requestMethod(RequestMethod.valueOf(method))
					.build();

			if(verbose) {
				if(httpRequest.getRequestMethod() == RequestMethod.HEAD) {
					logger.info("Found HEAD request, substituting it for GET request");
				}
			}

			if(
					// THIS MAY BE A REQUEST SMUGGLING ATTACK!!!
					HeaderUtils.containsHeader(hList, "Transfer-Encoding")
					&& HeaderUtils.containsHeader(hList, "Content-Length")
			) {
				if(verbose) logger.warn("{!warn}Rejected request from {}; There is a potential request smuggling attack", socket.getInetAddress().getHostAddress());
				requestBody = null;
				responseObject = faultRecoveryStrategy.handleException(new MalformedRequestException("request smuggling attack possible; request rejected"), httpRequest, server);
			}

			// priority -> T.E. -> C.L.
			else if(HeaderUtils.containsHeader(hList, "Transfer-Encoding")) {
				var l = new ArrayList<>(HeaderUtils.getHeader(hList, "Transfer-Encoding"));
				Collections.reverse(l);
				requestBody = null;
				for(String s : HeaderUtils.getHeader(hList, "Transfer-Encoding")) {
					var tc = server.getTransferCoders().resolve(s);
					if(tc == null) {
						// 501 Not Implemented
						responseObject = faultRecoveryStrategy.handleException(new TransferEncodingNotImplementedException(s), httpRequest, server);
						requestBody = null;
						break;
					} else {
						ByteBuffer buffer = ByteBuffer.allocate(socket.getInputStream().available());
						buffer.put(socket.getInputStream().readNBytes(socket.getInputStream().available()));
						requestBody = new String(buffer.array(), StandardCharsets.ISO_8859_1);
					}
				}
			} else if(HeaderUtils.containsHeader(hList, "Content-Length")) {
				int contentLength = Integer.parseInt(HeaderUtils.getHeader(hList, "Content-Length").get(0));
				if(contentLength > 0) {
					byte[] bytes = new byte[contentLength];
					for(int i = 0; i < contentLength; i++) {
						bytes[i] = (byte) socket.getInputStream().read();
					}
					requestBody = new String(bytes);
				} else {
					requestBody = null;
				}
			} else {
				requestBody = null;
			}

			if(requestBody != null) {
				try {
					unsafe.putObject(
							httpRequest,
							unsafe.objectFieldOffset(HTTPRequest.class.getDeclaredField("requestBody")),
							requestBody
					);
				} catch(NoSuchFieldException e) {
					unsafe.throwException(e); // we should never get here
				}
			}

			if(!requested.endsWith("/")) requested += "/";
			requested = URLDecoder.decode(requested, StandardCharsets.UTF_8);

			if(server.isVerbose()) System.out.println("\n" + httpRequest + "\n");

			// invoke mapping resolvers
			for(MappingResolver resolver : PriveWebServerExpose.getMappingResolvers(server)) {
				if(responseObject != null) break;
				for(var entry : resolver.getMappingHandlerMap().entrySet()) {
					if(entry.getKey().getRight() == (httpRequest.getRequestMethod() == RequestMethod.HEAD ? RequestMethod.GET : httpRequest.getRequestMethod())) {
						var url = entry.getKey().getLeft();
						if(!url.endsWith("/")) url += "/";
						if(url.equals(requested) && !url.contains("{}")) {
							// fast-path if no pathvar present ({})
							try {
								responseObject = resolver.invokeRequestHandler(url, httpRequest.getRequestMethod() == RequestMethod.HEAD ? RequestMethod.GET : httpRequest.getRequestMethod(), QueryParameters.parseQueryStringWithoutURL(queryParams, server), httpRequest, requestBody);
								if(verbose) logger.info("Successfully obtained response object from resolver {}", Integer.toHexString(resolver.hashCode()));
								break;
							} catch(Exception exception) {
								responseObject = faultRecoveryStrategy.handleException(exception, httpRequest, server);
							} catch(Error error) {
								responseObject = faultRecoveryStrategy.handleError(error, httpRequest, server);
							}
							break;
						} else {
							// check pathvars present
							var resolverPath = url.split("/");
							var requestedPath = requested.split("/");
							if(resolverPath.length == requestedPath.length) {
								List<String> pathvarList = new LinkedList<>();
								boolean errorFlag = false;
								// start resolving
								for(int i = 0; i < resolverPath.length; i++) {
									var res = resolverPath[i];
									var req = requestedPath[i];
									if(!res.equals(req)) {
										if(res.equals("{}")) {
											pathvarList.add(req);
										} else {
											errorFlag = true;
											break;
										}
									}
								}
								if(errorFlag) {
									if(verbose) logger.warn("{!warn}Cannot locate {} handler for route {}", httpRequest.getRequestMethod(), httpRequest.getRequestedResource());
								} else {
									if(verbose) logger.warn("{!warn}Successfully located {} handler for route {}", httpRequest.getRequestMethod(), httpRequest.getRequestedResource());
									try {
										responseObject = resolver.invokeRequestHandler(url, httpRequest.getRequestMethod() == RequestMethod.HEAD ? RequestMethod.GET : httpRequest.getRequestMethod(), QueryParameters.parseQueryStringWithoutURL(queryParams, server), httpRequest, requestBody, pathvarList.toArray(String[]::new));
										if(verbose) logger.info("Successfully obtained response object from resolver {}", Integer.toHexString(resolver.hashCode()));
										break;
									} catch(Exception exception) {
										responseObject = faultRecoveryStrategy.handleException(exception, httpRequest, server);
									} catch(Error error) {
										responseObject = faultRecoveryStrategy.handleError(error, httpRequest, server);
									}
									break;
								}
							}
						}
					}
				}
			}

			if(responseObject == null) responseObject = faultRecoveryStrategy.handleException(new HandlerNotFoundException(httpRequest), httpRequest, server);

			// serialize HTTPResponse
			try {
				HTTPResponse response = IOUtils.marshalObjectIntoResponse(responseObject, HTTPProtocol.HTTP1_1);
				out.print(response.getProtocol() + " " + response.getResponseCode() + PriveWebServer.HTTP_LF);
				var headerList = response.getHeaders();
				headerList = headerList.stream().map(header -> {
					if(header.header().equalsIgnoreCase("Server")) {
						return new Header("Server", PriveWebServer.SERVER_HEADER_VALUE);
					} else return header;
				}).toList();
				for(Header header : headerList) {
					out.print(header.header() + ": " + header.value() + PriveWebServer.HTTP_LF);
				}
				if(httpRequest.getRequestMethod() != RequestMethod.HEAD) {
					out.print(PriveWebServer.HTTP_LF);
					var cTypeList = response.getHeaders().stream().filter(header -> header.header().equalsIgnoreCase("Content-Type")).toList();
					String cType = cTypeList.size() > 0 ? cTypeList.get(0).value() : "";
					if(cType.startsWith("text/")) {
						out.print(new String(response.getResponseBody(), StandardCharsets.UTF_8));
						out.flush();
					} else {
						out.flush();
						var dos = new DataOutputStream(socket.getOutputStream());
						dos.write(response.getResponseBody(), 0, response.getResponseBody().length);
						dos.flush();
						Checks.ensureEquals(dos.size(), response.getResponseBody().length);
					}
				} else {
					out.flush();
				}
			} catch(Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

}