package dev.priveweb.core.exception;

import dev.priveweb.core.http.ResponseCode;
import org.jetbrains.annotations.NotNull;

/**
 * This interface denotes that the object implementing this interface is associated
 * with a response code.
 */
public interface ContainsResponseCode {

	/**
	 * Returns the response code associated with the implementor.
	 * @return the response code.
	 */
	@NotNull ResponseCode getResponseCode();

}
