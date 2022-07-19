package dev.priveweb.core.http.request;

import dev.priveweb.core.http.request.QueryParameters;

import java.lang.annotation.*;

/**
 * Path parameters. This annotation only supports
 * the type {@link String}
 * Essentially, only the {@link String}
 * type can be annotated. Annotating other parameters with this annotation will produce
 * a {@code WARN} log and the annotated parameter will return {@code null}.
 * <p>
 *     Note that path parameters are given in the appearing order.
 * </p>
 * <pre>
 *    {@literal @GetRequest("/params/{param1}/{param2}")}
 *     public void handler(@PathParam String p1, @PathParam String p2) {
 *     		// ...
 *     }
 * </pre>
 * <p>This does not inject query parameters; for those, have a {@link QueryParameters} parameter instead.</p>
 * @see QueryParameters
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam {
}
