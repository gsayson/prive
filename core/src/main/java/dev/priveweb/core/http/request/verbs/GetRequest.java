package dev.priveweb.core.http.request.verbs;

import dev.priveweb.core.http.request.RequestMethod;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CorrespondsTo(RequestMethod.GET)
@CorrespondsTo(RequestMethod.HEAD) // this is basically a GET request without a response body.
public @interface GetRequest {

	/**
	 * The path of the HTTP request; e.g.
	 * <p>{@code /abc/def/g}</p>
	 * @return the path of the HTTP request.
	 */
	String[] value();

}
