package dev.priveweb.core.http.response;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import dev.priveweb.core.exception.ContainsResponseCode;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.marshal.Marshallable;
import dev.priveweb.core.protocol.HTTPProtocol;
import lombok.*;
import dev.priveweb.core.http.Header;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * A response object that handles the underlying details of {@link Marshallable}.
 * @param <T>
 */
@Data
@Builder
public final class ResponseObject<T> implements Marshallable, ContainsResponseCode {

	/**
	 * Determines what MIME type to marshal into.
	 * By default, this is {@code application/json}.
	 */
	@Builder.Default
	@Setter(AccessLevel.NONE)
	private String marshalInto = "application/json";
	@NotNull @Getter private final ResponseCode responseCode;
	@NotNull private final T responseBody;
	@NotNull private final Class<T> responseBodyClass;
	@Singular
	@NotNull
	private final List<Header> headers;

	/**
	 * Converts the response into JSON. This may be an
	 * expensive operation, so it is recommended to cache this.
	 * @return the response, in JSON.
	 */
	// @SneakyThrows(NoSuchFieldException.class)
	public @NotNull String toJSON() {
		Moshi moshi = new Moshi.Builder().build();
		JsonAdapter<T> jsonAdapter = moshi.adapter(responseBodyClass);
		return jsonAdapter.toJson(responseBody);
	}

	@Override
	public @NotNull @Unmodifiable HTTPResponse marshal(HTTPProtocol protocol) {
		String response;
		if(marshalInto.equals("application/json")) {
			response = toJSON();
		} else {
			response = responseBody.toString();
		}
		return HTTPResponse.builder()
				.headers(headers)
				.responseCode(responseCode)
				.responseBody(response.getBytes(StandardCharsets.UTF_8))
				.protocol(protocol.toString())
				.build();
	}

}
