package dev.priveweb.core.http.request;

import dev.priveweb.core.http.request.HTTPRequest;
import dev.priveweb.core.http.request.RequestMethod;
import dev.priveweb.core.mapper.MappingResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation tells the {@link MappingResolver MappingResolver}
 * to pass the {@link HTTPRequest}'s <em>request body</em> to whatever parameter this annotates.
 * <p>
 *     The annotated parameter will <em>never</em> be {@code null} if
 *     the following conditions are satisfied:
 *     <ul>
 *         <li>The annotated parameter is a {@link String}, or it can be serialized through JSON to a {@link String};</li>
 *         <li>The HTTP {@link RequestMethod} is either {@link RequestMethod#POST}, {@link RequestMethod#PATCH}, or {@link RequestMethod#PUT}.</li>
 *     </ul>
 * </p>
 * <pre>{@code
 * @PostRequest("/post")
 * public HTTPResponse handler(@RequestBody Object o) {
 *     return HTTPResponse.builder()
 * 				.header(new Header("Content-Type", "application/json"))
 * 				.responseCode(ResponseCode.S_200)
 * 				.responseBody(response.getBytes(StandardCharsets.UTF_8))
 * 				.protocol("HTTP/1.1")
 * 				.build();
 * }
 *
 * // this is what happens behind the scenes
 * private static String toJSON(Object o) {
 *		Moshi moshi = new Moshi.Builder().build();
 * 	JsonAdapter<T> jsonAdapter = moshi.adapter(responseBodyClass);
 * 	return jsonAdapter.toJson(responseBody);
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
}
