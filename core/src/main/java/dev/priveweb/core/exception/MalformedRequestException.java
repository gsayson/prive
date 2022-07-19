package dev.priveweb.core.exception;

import dev.priveweb.core.http.ResponseCode;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when there is a malformed request.
 */
public class MalformedRequestException extends Exception implements ContainsResponseCode {

	@Override
	public @NotNull ResponseCode getResponseCode() {
		return ResponseCode.C_400;
	}

	public MalformedRequestException(String msg) {
		super(msg);
	}

	public MalformedRequestException(String msg, Exception e) {
		super(msg, e);
	}

}
