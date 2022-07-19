package dev.priveweb.core.exception;

import dev.priveweb.core.http.Header;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.http.request.HTTPRequest;
import dev.priveweb.core.http.response.HTTPResponse;
import dev.priveweb.core.marshal.Marshallable;
import dev.priveweb.core.server.PriveServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.nio.charset.StandardCharsets;

/**
 * The default fault recovery used in Prive.
 * <ul>
 *     <li>For {@link Exception}s this checks whether it inherits {@link ContainsResponseCode} and returns a {@link Marshallable} based on that and the exception details.</li>
 *     <li>For {@link Error}s this terminates the server and exits with a status code of {@code 1}.</li>
 * </ul>
 */
public class DefaultFaultRecovery implements FaultRecoveryStrategy {

	/**
	 * This should not be used by external code, to set this as
	 * the Prive server's fault recovery call {@link PriveServer#setFaultRecoveryStrategy(FaultRecoveryStrategy)}
	 */
	@ApiStatus.Internal
	public static final DefaultFaultRecovery INSTANCE = new DefaultFaultRecovery();
	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(DefaultFaultRecovery.class));

	private DefaultFaultRecovery() {}

	@NotNull
	@Override
	public Marshallable handleException(@NotNull Exception e, @NotNull HTTPRequest request, @NotNull PriveServer server) {
		ResponseCode responseCode = e instanceof ContainsResponseCode responseCodeException ? responseCodeException.getResponseCode() : ResponseCode.SV_500;
		return HTTPResponse.builder()
				.responseCode(responseCode)
				.protocol("HTTP/1.1")
				.header(new Header("Content-Type", "application/json"))
				.responseBody((
						"{\"exception\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getLocalizedMessage() + "\",\"cause\":\"" + e.getCause() + "\",\"responseCode\":\"" + responseCode + "\"}"
				).getBytes(StandardCharsets.UTF_8))
				.build();
	}

	@Override
	public @Nullable Marshallable handleError(@NotNull Error e, @NotNull HTTPRequest request, @NotNull PriveServer server) {
		logger.error("{!error}Fatal error {} encountered, halting server and exiting - message: {}", e.getClass().getName(), e.getLocalizedMessage());
		server.halt();
		System.exit(1);
		return null;
	}

}
