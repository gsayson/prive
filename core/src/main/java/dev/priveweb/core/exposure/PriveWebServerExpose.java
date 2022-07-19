package dev.priveweb.core.exposure;

import lombok.SneakyThrows;
import dev.priveweb.core.server.impl.PriveWebServer;
import dev.priveweb.core.mapper.MappingResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Exposes methods for exposure.
 */
public class PriveWebServerExpose {

	/**
	 * Gets the mapper resolvers.
	 * @param webServer The web server to expose.
	 * @return the map of {@link MappingResolver}s.
	 */
	@SneakyThrows
	@SuppressWarnings("unchecked")
	public static Set<MappingResolver> getMappingResolvers(@NotNull PriveWebServer webServer) {
		var f = webServer.getClass().getDeclaredField("resolverSet");
		f.setAccessible(true); // use this instead of trySetAccessible(boolean); this is guaranteed to not fail
		return (Set<MappingResolver>) f.get(webServer);
	}

	/**
	 * Sets the mapper resolvers.
	 * @param webServer The web server to expose.
	 * @param mappingResolvers The mapping resolver set.
	 */
	@SneakyThrows
	public static void setMappingResolvers(@NotNull PriveWebServer webServer, Set<MappingResolver> mappingResolvers) {
		var f = webServer.getClass().getDeclaredField("resolverSet");
		f.setAccessible(true); // use this instead of trySetAccessible(boolean); this is guaranteed to not fail
		f.set(webServer, mappingResolvers);
	}

	private PriveWebServerExpose() {
		//no instance
	}

}
