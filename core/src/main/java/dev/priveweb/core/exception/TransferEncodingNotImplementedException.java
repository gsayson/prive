package dev.priveweb.core.exception;

import dev.priveweb.core.http.ResponseCode;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a {@code Transfer-Coding} is not implemented.
 */
public class TransferEncodingNotImplementedException extends Exception implements ContainsResponseCode {

	/**
	 * Creates a {@link TransferEncodingNotImplementedException}.
	 * @param te The name of the {@code Transfer-Encoding}.
	 */
	public TransferEncodingNotImplementedException(@NotNull String te) {
		super(te);
	}

	@Override
	public @NotNull ResponseCode getResponseCode() {
		return ResponseCode.SV_501;
	}

}
