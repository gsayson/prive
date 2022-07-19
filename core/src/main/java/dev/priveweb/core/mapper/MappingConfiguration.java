package dev.priveweb.core.mapper;

import dev.priveweb.core.server.PriveServer;
import dev.priveweb.core.http.request.RequestMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * This interface allows one to map a URL to a request handler. If you need to
 * map many URLS to all of an object's handlers, it is recommended to call {@link PriveServer#register(Object)}.
 * <p>Technical note: {@code MappingConfiguration}s contain one {@link Object}'s {@link MappingResolver}, so mapping would take place on the object itself.</p>
 */
public interface MappingConfiguration {

	/**
	 * Maps a URL relative to the root path (e.g. {@code /route} is relative to {@code http(s)://localhost/route}), to a {@link Method}.
	 * <p>Note: {@link Method}s can be obtained by {@link #getClass()}{@code .}{@link Class#getDeclaredMethod(String, Class[]) getDeclaredMethod(String, Class[])}</p>
	 * @param url the URL (relative to the root)
	 * @param requestMethod the {@link RequestMethod} to map to.
	 * @param method the {@link Method} to map to.
	 * @throws IllegalArgumentException if the given {@link Method} does not URL.
	 */
	void map(@NotNull String url, @NotNull RequestMethod requestMethod, @Nullable Method method);
}
