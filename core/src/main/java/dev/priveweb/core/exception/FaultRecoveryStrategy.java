package dev.priveweb.core.exception;

import dev.priveweb.core.http.request.HTTPRequest;
import dev.priveweb.core.marshal.Marshallable;
import dev.priveweb.core.server.PriveServer;
import dev.priveweb.core.server.impl.PriveWebServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A strategy for handling faults that occur during execution.
 */
public interface FaultRecoveryStrategy {

	/**
	 * Called to handle an {@link Exception}.
	 * @param e The caught exception.
	 * @param request The HTTP request.
	 * @param server The {@link PriveServer} used.
	 * @return a {@link Marshallable} that will be serialized and sent to the client,
	 * for example {@link HTTPRequest}.
	 */
	@NotNull
	Marshallable handleException(@NotNull Exception e, @NotNull HTTPRequest request, @NotNull PriveServer server);

	/**
	 * Called to handle a possibly fatal {@link Error}. Note that {@linkplain PriveWebServer#halt() halting the server} can
	 * and will be typically expected. In the case that it does not halt, the returned {@link Marshallable} cannot be {@code null}.
	 * <p>While the annotation {@link Nullable @Nullable} on this method specifies that this method can return {@code null},
	 * that is only for cases where the server is halted, as a returned {@link Marshallable} would serve no purpose and
	 * also cannot be processed by the server.</p>
	 * @param e The caught error.
	 * @param request The HTTP request.
	 * @param server The {@link PriveServer} used.
	 * @return a {@link Marshallable} that will be serialized and sent to the client,
	 * for example {@link HTTPRequest}, if the server is not halted. Else, {@code null} is expected.
	 */
	@Nullable
	Marshallable handleError(@NotNull Error e, @NotNull HTTPRequest request, @NotNull PriveServer server);

}