package dev.priveweb.core.server;

import dev.priveweb.core.exception.DefaultFaultRecovery;
import dev.priveweb.core.exception.FaultRecoveryStrategy;
import dev.priveweb.core.mapper.MappingConfiguration;
import org.jetbrains.annotations.*;

import java.io.IOException;

/**
 * This interface allows one to directly interact with the underlying
 * {@code PriveServer}. <em>The use of this interface should only happen if the server must be configured.</em>
 */
public interface PriveServer {

	/**
	 * Whether the current {@link PriveServer} is on verbose mode.
	 * @return whether the server is logging verbosely.
	 */
	boolean isVerbose();

	/**
	 * Registers the given object's HTTP request handlers.
	 * <p>If you need to only modify the mapping of a URL, using {@link MappingConfiguration MappingConfiguration} is better.</p>
	 * @param obj the object to register.
	 * @return this {@link PriveServer}
	 * @see MappingConfiguration
	 */
	PriveServer register(Object obj);

	/**
	 * Starts the web server.
	 * <p>Please use {@link dev.priveweb.core.PriveApplication#start(PriveServer, boolean)} instead of this method.</p>
	 */
	@Blocking
	@ApiStatus.Internal
	void start(boolean verbose) throws IOException;

	/**
	 * Terminates the web server, gracefully if possible.
	 */
	@NonBlocking
	void halt();

	/**
	 * Gets the {@link FaultRecoveryStrategy} from the server.
	 * @return the {@link FaultRecoveryStrategy} used.
	 */
	@NotNull FaultRecoveryStrategy getFaultRecoveryStrategy();

	/**
	 * Sets the {@link FaultRecoveryStrategy} of the server.
	 * <p><em>Calling this method with {@code null} will set {@link DefaultFaultRecovery DefaultFaultRecovery} as the {@code FaultRecoveryStrategy}.</em></p>
	 */
	void setFaultRecoveryStrategy(@Nullable FaultRecoveryStrategy faultRecoveryStrategy);

}
