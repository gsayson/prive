package dev.priveweb.core.exception;

import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.http.request.HTTPRequest;
import org.jetbrains.annotations.NotNull;

/**
 * This is essentially a 404 exception.
 */
public class HandlerNotFoundException extends Exception implements ContainsResponseCode {

	/**
	 * Creates a new {@link HandlerNotFoundException}.
	 * @param request The request made to the server.
	 */
	public HandlerNotFoundException(@NotNull HTTPRequest request) {
		super("cannot locate " + request.getRequestMethod() + " handler for " + request.getRequestedResource());
	}

	@Override
	public @NotNull ResponseCode getResponseCode() {
		return ResponseCode.C_404;
	}

}
