package dev.priveweb.core.http.request;

import dev.priveweb.core.http.request.verbs.*;
import dev.priveweb.core.server.impl.PriveWebServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

/**
 * The http type used. Commonly used request methods are as follows:
 * <ul>
 *     <li>{@code GET} - a http to retrieve data from a resource;</li>
 *     <li>{@code POST} - a http for submitting data to a resource (possibly changing the resource's state);</li>
 * </ul>
 * The {@code HEAD} http will be handled internally by {@linkplain PriveWebServer Prive}; it
 * will still run the handling {@code GET} http code, though it will not return any response body.
 * For more details check the <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">MDN documentation</a>.
 */
public enum RequestMethod {

	GET,
	POST,
	DELETE,
	HEAD,
	PUT,
	PATCH,
	OPTIONS;

	/**
	 * Get the request methods used from an annotated object.
	 * <p>This method is mapper to the library, and it is placed here for convenience.<br>
	 * This method may change or be removed anytime.</p>
	 * @param obj the object to check.
	 * @return the request methods used.
	 */
	@ApiStatus.Internal
	public static RequestMethod[] getRequestMethod(@NotNull AnnotatedElement obj) {
		ArrayList<RequestMethod> methods = new ArrayList<>();
		if(obj.isAnnotationPresent(GetRequest.class)) methods.add(GET);
		if(obj.isAnnotationPresent(PostRequest.class)) methods.add(POST);
		if(obj.isAnnotationPresent(DeleteRequest.class)) methods.add(DELETE);
		if(obj.isAnnotationPresent(PutRequest.class)) methods.add(PUT);
		if(obj.isAnnotationPresent(PatchRequest.class)) methods.add(PATCH);
		if(obj.isAnnotationPresent(OptionsRequest.class)) methods.add(OPTIONS);
		return methods.toArray(RequestMethod[]::new);
	}

}
