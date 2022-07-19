package dev.priveweb.core;

import dev.priveweb.core.server.PriveServer;
import lombok.SneakyThrows;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;
import sun.misc.Signal;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for starting Prive applications.
 */
public abstract class PriveApplication {

	static {
		AnsiLogger.setAnsiEnabled(Ansi.isDetected());
	}

	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(PriveApplication.class));

	private PriveApplication() {
		//no instance
	}

	/**
	 * Starts the web server and enters into a {@code while} loop
	 * that exits upon the {@code CTRL+C} signal.
	 * <p>
	 *     This method hides the possible propagation of an {@link java.io.IOException IOException} through
	 * 	   {@link SneakyThrows @SneakyThrows}.
	 * </p>
	 * @param server The {@link PriveServer}.
	 * @param verbose Whether to run in verbose mode.
	 */
	@Blocking
	@SneakyThrows
	@SuppressWarnings({"StatementWithEmptyBody", "PlaceholderCountMatchesArgumentCount"})
	public static void start(@NotNull PriveServer server, boolean verbose) {
		AtomicBoolean atomicBoolean = new AtomicBoolean(true);
		// there is really only one shutdown hook we need to handle.
		logger.info("Registered {id} signal handler", "SIGINT");
		Signal.handle(new Signal("INT"), sig -> atomicBoolean.set(false));
		server.start(verbose);
		logger.info("Prive instance {id} started", Integer.toHexString(server.hashCode()));
		while(atomicBoolean.get());
		server.halt();
		logger.info("Prive instance {id} halted", Integer.toHexString(server.hashCode()));
	}

	/**
	 * Only starts the server.
	 * <p>
	 *     It is recommended to use {@link #start(PriveServer, boolean)}, as it is blocking
	 *     and waits for a {@code CTRL+C} signal. This method is strongly discouraged for production
	 *     use.
	 * </p>
	 * @param server The {@link PriveServer}.
	 * @param verbose Whether to run in verbose mode.
	 */
	@SneakyThrows
	public static void startOnly(@NotNull PriveServer server, boolean verbose) {
		server.start(verbose);
	}

}
