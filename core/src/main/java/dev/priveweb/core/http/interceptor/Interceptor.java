package dev.priveweb.core.http.interceptor;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor {

	/**
	 * The class to intercept request handlers to.
	 * <pre>
	 * class X {
	 * 	{@literal @GetRequest("/intercepted")}
	 * 	public void intercepted() {
	 * 	    // normal code here, pretend interceptor doesn't exist
	 * 	}
	 *
	 * 	{@literal @GetRequest("/normal")}
	 * 	public void normal() {
	 * 	    // this is also intercepted
	 * 	}
	 * }
	 * </pre>
	 * @return the class to intercept.
	 */
	Class<?> value();

}
