package dev.priveweb.core.http.response;

import dev.priveweb.core.exception.ContainsResponseCode;
import dev.priveweb.core.http.Header;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.marshal.Marshallable;
import dev.priveweb.core.protocol.HTTPProtocol;
import dev.priveweb.core.server.impl.PriveWebServer;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * An object representing the HTTP response from the server to the client.
 * Note that unlike {@link ResponseObject}, this class does not
 * handle any header creation and sorting.
 * <p>Please ensure that the charset used is {@code UTF-8}.</p>
 */
@Data
@Builder
public class HTTPResponse implements Marshallable, ContainsResponseCode {

	@Singular
	private final List<Header> headers;

	/**
	 * The response code sent to the client.
	 */
	@Getter
	private final ResponseCode responseCode;

	/**
	 * The HTTP protocol used. (e.g. {@code HTTP/1.1})
	 */
	private final String protocol;

	/**
	 * The response body.
	 */
	private final byte @NotNull [] responseBody;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(protocol)
				.append(" ")
				.append(responseCode)
				.append(PriveWebServer.HTTP_LF);
		for(Header header : headers) {
			builder.append(header.header())
					.append(": ")
					.append(header.value())
					.append(PriveWebServer.HTTP_LF);
		}
		builder.append(PriveWebServer.HTTP_LF);
		builder.append(new String(responseBody, StandardCharsets.UTF_8));
		return builder.toString();
	}

	@Override
	public @NotNull HTTPResponse marshal(HTTPProtocol protocol) {
		return this;
	}

}