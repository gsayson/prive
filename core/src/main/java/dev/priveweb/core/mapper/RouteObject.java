package dev.priveweb.core.mapper;

import dev.priveweb.core.http.request.RequestMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record RouteObject(
		@NotNull String[] get,
		@NotNull String[] post
) {
	@Contract(pure = true)
	public String[] getRoutesOfRequestMethod(@NotNull RequestMethod method) {
		switch(method) {
			case GET -> {
				return get;
			}
			case POST -> {
				return post;
			}
			default -> {
				throw new RuntimeException("Not implemented");
			}
		}
	}
}
