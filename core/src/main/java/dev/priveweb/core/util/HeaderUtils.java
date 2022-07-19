package dev.priveweb.core.util;

import dev.priveweb.core.http.Header;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

/**
 * Utilities for headers.
 */
public abstract class HeaderUtils {

	private HeaderUtils() {
		//no instance
	}

	@Contract(pure = true)
	public static boolean containsHeader(@NotNull Collection<Header> headers, @NotNull String headerName) {
		return headers.stream().anyMatch(header -> header.header().equalsIgnoreCase(headerName));
	}

	/**
	 * Gets the values of a header.
	 * @param headers The header set.
	 * @param headerName The header name to look for.
	 * @return a list of {@link String}s delimited by a comma ({@code ,}).
	 */
	@Unmodifiable
	@Contract(pure = true)
	public static List<String> getHeader(@NotNull List<Header> headers, @NotNull String headerName) {
		return headers.stream()
				.filter(header -> header.header().equalsIgnoreCase(headerName))
				.map(Header::value)
				.toList();
	}

}
