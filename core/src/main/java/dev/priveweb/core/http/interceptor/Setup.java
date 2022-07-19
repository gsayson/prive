package dev.priveweb.core.http.interceptor;

import dev.priveweb.core.mapper.MappingConfiguration;
import dev.priveweb.core.server.PriveServer;
import org.jetbrains.annotations.NotNull;

/**
 * Allows a class to have setup code run by Prive.
 */
@FunctionalInterface
public interface Setup {

	/**
	 * Performs setup code for the object.
	 * @param mapper The mapper provided to run set-up code.
	 * @param server The server itself.
	 */
	void setup(@NotNull MappingConfiguration mapper, @NotNull PriveServer server);

}
