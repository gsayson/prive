package dev.priveweb.core.marshal;

import dev.priveweb.core.protocol.HTTPProtocol;
import dev.priveweb.core.http.response.HTTPResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Allows an object to be marshalled into
 * a response.
 */
@FunctionalInterface
public interface Marshallable {

	/**
	 * Marshals an object into a proper HTTP response.
	 * @return the HTTP response.
	 */
	@NotNull HTTPResponse marshal(HTTPProtocol protocol);

}
