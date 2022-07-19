package dev.priveweb.core.http.request.verbs;

import dev.priveweb.core.http.request.RequestMethod;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@CorrespondsTo(RequestMethod.POST)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostRequest {

	/**
	 * The path of the HTTP request; e.g.
	 * <p>{@code /abc/def/g}</p>
	 * @return the path of the HTTP request.
	 */
	String[] value();

}
