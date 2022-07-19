package dev.priveweb.core.http.request;

import dev.priveweb.core.http.Header;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * An object representing the HTTP response from the server to the client.
 */
@Getter
@Builder
public class HTTPRequest {

	@NotNull
	@Singular
	private List<@NotNull Header> headers;

	/**
	 * The request method used by the client.
	 */
	private @NotNull RequestMethod requestMethod;

	/**
	 * The HTTP protocol used. (e.g. {@code HTTP/1.1})
	 */
	private @NotNull String protocol;

	/**
	 * The requested resource.
	 */
	private @NotNull String requestedResource;

	/**
	 * The request body, if applicable.
	 */
	private @Nullable String requestBody;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(requestMethod)
				.append(" ")
				.append(requestedResource)
				.append(" ")
				.append(protocol)
				.append("\n");
		headers.forEach(
				h -> {
					if(!h.header().isBlank())
						builder.append(h.header())
								.append(": ")
								.append(h.value())
								.append("\r\n");
				}
		);
		builder.append("\r\n")
				.append(requestBody == null ? "" : requestBody);
		return builder.toString();
	}

}