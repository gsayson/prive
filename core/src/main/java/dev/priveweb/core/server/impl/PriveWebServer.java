package dev.priveweb.core.server.impl;

import dev.priveweb.core.PriveApplication;
import dev.priveweb.core.data.DataEncodingRegistry;
import dev.priveweb.core.data.impl.ChunkedCoder;
import dev.priveweb.core.exception.DefaultFaultRecovery;
import dev.priveweb.core.exception.FaultRecoveryStrategy;
import dev.priveweb.core.http.request.RequestMethod;
import dev.priveweb.core.http.request.verbs.*;
import dev.priveweb.core.server.PriveServer;
import lombok.Getter;
import lombok.Setter;
import dev.priveweb.core.http.interceptor.Setup;
import dev.priveweb.core.mapper.MappingResolver;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the Prive web server's implementation.
 */
@SuppressWarnings("RedundantThrows")
public final class PriveWebServer implements PriveServer {

	public static final String VERSION = "1.0.0";
	public static final String SERVER_HEADER_VALUE = "prive/" + VERSION + " (" + System.getProperty("os.name") + ")";
	public static final String HTTP_LF = "\r\n";
	@NotNull private final ServerSocket @NotNull [] serverSockets;
	@BlockingExecutor private final ExecutorService serverSocketExecutor;
	private final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(PriveWebServer.class));
	@Getter private boolean running = false;
	@Getter private boolean verbose = false;
	@Getter private FaultRecoveryStrategy faultRecoveryStrategy = DefaultFaultRecovery.INSTANCE;
	/**
	 * Whether to reuse TCP connections.
	 * <p><b>This is disabled by default due to security concerns</b> (i.e. to prevent request smuggling). Enabling it
	 * may pose a security risk, unless there are appropriate countermeasures implemented.</p>
	 */
	@Getter @Setter private boolean connectionReuseEnabled = false;
	/**
	 * Gets the {@link DataEncodingRegistry} containing
	 * all available {@code Transfer-Encoding} implementations.
	 */
	@Getter private final DataEncodingRegistry transferCoders = new DataEncodingRegistry();

	/**
	 * Creates a new {@link PriveWebServer} with the given {@link ServerSocket}
	 * and the given {@link ExecutorService}.
	 *
	 * @param executor          The executor to use.
	 * @param serverSockets     The server sockets to use.
	 */
	public PriveWebServer(@NotNull ExecutorService executor, ServerSocket @NotNull ... serverSockets) {
		this.serverSocketExecutor = executor;
		this.serverSockets = serverSockets;
		transferCoders.register(new ChunkedCoder()); // "Transfer-Encoding: chunked" MUST BE SUPPORTED!!!
	}

	/**
	 * Creates a new {@link PriveWebServer} with a default {@link ServerSocket}
	 * with the given port and the given {@link ExecutorService}. All connections
	 * are not secured.
	 *
	 * @param serverSocketExecutor The socket executor to use.
	 * @param ports                The ports to listen to.
	 */
	public PriveWebServer(@NotNull ExecutorService serverSocketExecutor, int @NotNull ... ports) throws IOException {
		this.serverSocketExecutor = serverSocketExecutor;
		List<ServerSocket> serverSockets = new ArrayList<>();
		for(int p : ports) {
			serverSockets.add(new ServerSocket(p) {{
				setReuseAddress(connectionReuseEnabled);
			}});
		}
		this.serverSockets = serverSockets.toArray(ServerSocket[]::new);
		transferCoders.register(new ChunkedCoder());
	}

	/**
	 * Runs the web server.
	 * <p>
	 *     Please use {@link dev.priveweb.core.PriveApplication#start(PriveServer, boolean) PriveApplication.start(PriveServer, boolean)} instead.
	 *     This method should not be considered as a public API.
	 * </p>
	 * @param verbose Whether to run in verbose mode.
	 * @see dev.priveweb.core.PriveApplication#start(PriveServer, boolean)
	 * @see ApiStatus.Internal
	 */
	// suppress IOException warning of not throwing; it is actually thrown.
	@NonBlocking
	@ApiStatus.Internal
	public void start(boolean verbose) throws IOException {
		if(running) return; // don't run twice!
		var clazz = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		if(clazz != PriveApplication.class) {
			logger.warn("{!error}==============================================");
			logger.warn("{!warn}|| WARNING! ||");
			logger.warn("{!warn}Please use PriveApplication to start");
			logger.warn("{!warn}any PriveServer, as it handles the");
			//noinspection PlaceholderCountMatchesArgumentCount
			logger.warn("{!warn}boring work. This method should {important}", "NOT");
			logger.warn("{!warn}be considered a public API, and the");
			logger.warn("{!warn}accessibility of this method may change.");
			logger.warn("{!error}==============================================");
		}
		this.verbose = verbose;
		running = true;
		logger.info("{!important}Initialized Prive {}{}", VERSION, verbose ? " in verbose mode" : "");
		logger.info("{!important}Using {} as the fault recovery strategy", faultRecoveryStrategy.getClass().getSimpleName());
		logger.info("{!important}JRE Version    : " + Runtime.version());
		logger.info("{!important}Maximum memory : " + Runtime.getRuntime().maxMemory() + " bytes");
		ExecutorService service = Executors.newWorkStealingPool();
		for(ServerSocket serverSocket : serverSockets) {
			serverSocketExecutor.submit(() -> {
				while(running) {
					try {
						@SuppressWarnings("BlockingMethodInNonBlockingContext") // context is serverSocketExecutor
						var socket = serverSocket.accept();
						ClientSocketHandler handler = new ClientSocketHandler(socket, verbose, this, faultRecoveryStrategy);
						service.submit(handler);
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				}
				logger.info("{!important}Halted socket handler for port {}", serverSocket.getLocalPort());
			});
		}
	}

	/**
	 * Forcefully terminates all connections with the web server.
	 */
	@NonBlocking
	public void halt() {
		running = false;
	}

	@Override
	public void setFaultRecoveryStrategy(@Nullable FaultRecoveryStrategy faultRecoveryStrategy) {
		if(faultRecoveryStrategy == null) {
			if(this.faultRecoveryStrategy == DefaultFaultRecovery.INSTANCE) return;
			this.faultRecoveryStrategy = DefaultFaultRecovery.INSTANCE;
		} else {
			this.faultRecoveryStrategy = faultRecoveryStrategy;
		}
	}

	// http stuff

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // exposure.PriveWebServerExpose
	private final Set<MappingResolver> resolverSet = new HashSet<>();

	/**
	 * Registers the given object's HTTP mappings.
	 * @param obj the object to register.
	 * @return this {@link PriveWebServer}
	 */
	@Contract("_ -> this")
	@SuppressWarnings("UnusedReturnValue")
	public PriveWebServer register(@NotNull Object obj) {
		// check for setup code
		var mr = new MappingResolver(this, obj);
		if(obj instanceof Setup setup) {
			//noinspection PlaceholderCountMatchesArgumentCount
			logger.info("Invoking setup code for object {id}@{id}", obj.getClass().getName(), Integer.toHexString(obj.hashCode()));
			setup.setup(mr.getMappingConfigurer(), this);
		}
		for(Method method : obj.getClass().getDeclaredMethods()) {
			for(RequestMethod verb : RequestMethod.getRequestMethod(method)) {
				for(String url : getURL(method, verb)) {
					if(mr.getMappingHandlerMap().containsValue(new MappingResolver.MappingHandler(method, url, this))) {
						logger.warn("{!error}Conflicting handlers for \"{} {}\" detected, will replace", verb, url);
					}
				}
			}
		}
		resolverSet.add(mr);
		return this;
	}

	@Contract(pure = true)
	private static String[] getURL(@NotNull Method method, @NotNull RequestMethod requestMethod) {
		// we can get annotations directly, getRequestMethod(AnnotatedElement) already checks for it
		switch(requestMethod) {
			case GET -> {
				return method.getAnnotation(GetRequest.class).value();
			}
			case POST -> {
				return method.getAnnotation(PostRequest.class).value();
			}
			case DELETE -> {
				return method.getAnnotation(DeleteRequest.class).value();
			}
			case HEAD -> {
				throw new Error("How did we get here?");
			}
			case PUT -> {
				return method.getAnnotation(PutRequest.class).value();
			}
			case PATCH -> {
				return method.getAnnotation(PatchRequest.class).value();
			}
			case OPTIONS -> {
				return method.getAnnotation(OptionsRequest.class).value();
			}
		}
		// we will never reach here, unless the JVM is stupid.
		throw new InternalError();
	}

}
