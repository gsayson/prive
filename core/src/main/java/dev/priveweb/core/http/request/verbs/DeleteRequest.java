package dev.priveweb.core.http.request.verbs;

import dev.priveweb.core.http.request.RequestMethod;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CorrespondsTo(RequestMethod.DELETE)
public @interface DeleteRequest {

	/**
	 * The path of the HTTP request; e.g.
	 * <p>{@code /abc/def/g}</p>
	 * @return the path of the HTTP request.
	 */
	String[] value();

}
