package dev.priveweb.core.http.request;

import dev.priveweb.core.server.impl.PriveWebServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.util.*;

/**
 * Container for parsing and getting query parameters.
 * If there are no query parameters given for a route, the parameter
 * of this type will return null.
 */
public class QueryParameters {

	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(QueryParameters.class));

	private QueryParameters() {
		//no instance
	}

	private final Map<String, List<String>> queryFields = new Hashtable<>();

	/**
	 * Parses the query string without the full URL, excluding or including the leading {@code ?} sign.
	 * @param queryString the query string to parse.
	 * @param server the {@link PriveWebServer} to use; allow verbose logging and make the creation of this class
	 *               harder for the client.
	 * @return a new {@link QueryParameters} instance.
	 */
	public static @NotNull QueryParameters parseQueryStringWithoutURL(@NotNull String queryString, @NotNull PriveWebServer server) {
		if(queryString.isBlank()) return new QueryParameters();
		if(queryString.startsWith("?")) queryString = queryString.substring(1);
		StringTokenizer tokenizer = new StringTokenizer(queryString, "&;");
		QueryParameters parameters = new QueryParameters();
		while(tokenizer.hasMoreTokens()) {
			String fieldAssignment = tokenizer.nextToken(); // do not delimit '='
			if(!fieldAssignment.contains("=") || fieldAssignment.indexOf('=') != fieldAssignment.lastIndexOf('=')) {
				if(server.isVerbose()) logger.warn("{!warn}Invalid assignment '{}'; this will be skipped", fieldAssignment);
			} else {
				if(server.isVerbose()) logger.info("Field assignment '{}' parsed successfully", fieldAssignment);
				var split = fieldAssignment.split("=");
				String field = split[0];
				String value = split[1];
				parameters.queryFields.computeIfAbsent(field, k -> new LinkedList<>());
				parameters.queryFields.compute(field, (k, v) -> {
					assert v != null;
					v.add(value);
					return v;
				});
			}
		}
		logger.info("Completed parsing: toString() returns {}", parameters);
		return parameters;
	}

	public static @NotNull QueryParameters parseQueryStringWithURL(@NotNull String url, @NotNull PriveWebServer server) {
		String queryParams = "";
		int endIndex = url.lastIndexOf("?");
		if(endIndex != -1) {
			String temp = url.substring(0, endIndex);
			queryParams = url.replace(temp, "");
		}
		return parseQueryStringWithoutURL(queryParams, server);
	}

	/**
	 * Returns the given query parameters in the form of a {@link List},
	 * as there may be multiple values. For example:
	 * <pre>
	 *     http://localhost:8080/?values=1;values=2;
	 * </pre>
	 * will return such a {@link List}:
	 * <pre>
	 *     {@link List}.{@link List#of(Object, Object) of}("1", "2")
	 * </pre>
	 * <p>This list is <em>{@linkplain Collections#unmodifiableList(List) unmodifiable}</em>;
	 * performing operations on the returned list will throw an {@link UnsupportedOperationException}.</p>
	 * @return given query parameters in the form of a {@link List}.
	 * @throws UnsupportedOperationException if the returned list is mutated.
	 */
	@Nullable
	@Unmodifiable
	@SuppressWarnings("JavadocLinkAsPlainText")
	public List<String> getQueryParameters(String field) {
		return queryFields.get(field);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", QueryParameters.class.getSimpleName() + "[", "]").add("queryFields=" + queryFields).toString();
	}

}
