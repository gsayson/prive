package dev.priveweb.core.mapper;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import dev.priveweb.core.exception.HandlerNotFoundException;
import dev.priveweb.core.exception.MalformedRequestException;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.http.request.*;
import dev.priveweb.core.http.request.verbs.*;
import dev.priveweb.core.http.request.PathParam;
import dev.priveweb.core.http.request.RequestBody;
import dev.priveweb.core.http.response.HTTPResponse;
import dev.priveweb.core.http.session.Session;
import dev.priveweb.core.mapper.impl.MappingConfigurationImpl;
import dev.priveweb.core.server.PriveServer;
import dev.priveweb.core.server.impl.PriveWebServer;
import dev.priveweb.core.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Resolves all the methods for an object.
 */
public class MappingResolver {

	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(MappingResolver.class));
	@Getter private final PriveWebServer server;
	private final Object object;
	@Getter private final MappingConfigurationImpl mappingConfigurer;

	/**
	 * Creates a new {@link MappingResolver} with the given {@link PriveWebServer}, and the given {@link Object}
	 * whose methods would be scanned.
	 * @param server the {@link PriveWebServer} to use.
	 */
	public MappingResolver(@NotNull PriveWebServer server, @NotNull Object object) {
		this.server = server;
		this.object = object;
		this.mappingConfigurer = new MappingConfigurationImpl(server, this);
		for(Method m : object.getClass().getDeclaredMethods()) {
			var reqMethods = RequestMethod.getRequestMethod(m);
			for(RequestMethod method : reqMethods) {
				for(String route : getRoutes(m).getRoutesOfRequestMethod(method)) {
					if(!route.endsWith("/")) route += "/";
					try {
						mappingHandlerMap.put(new Pair<>(route, method), new MappingHandler(m, route, server));
					} catch(IllegalArgumentException e) {
						throw new InternalError(e); // should not reach this block
					}
				}
			}
		}
	}

	// mapper stuff below

	@Getter
	private final Map<Pair<String, RequestMethod>, MappingHandler> mappingHandlerMap = new HashMap<>();

	/**
	 * Invokes the request handler for the given object.
	 * @param path the path to resolve. (e.g. {@code /hayami/api/request})
	 * @param requestMethod the {@link RequestMethod} to use.
	 * @return (possibly <code>null</code>) the returned object from the method's invocation.
	 */
	@Nullable
	public Object invokeRequestHandler(

			@NotNull String path,
			@NotNull RequestMethod requestMethod,
			@Nullable QueryParameters parameters,
			@NotNull HTTPRequest request,
			@Nullable String requestBody,
			@NotNull String... pathvars

	) throws HandlerNotFoundException, MalformedRequestException {

		if(requestMethod != RequestMethod.POST && requestMethod != RequestMethod.PATCH && requestMethod != RequestMethod.PUT) {
			if(requestBody != null) {
				throw new MalformedRequestException(requestMethod + " requests cannot have request bodies");
			}
		}
		var p = new Pair<>(path, requestMethod);
		MappingHandler v = mappingHandlerMap.get(new Pair<>(path, requestMethod));
		for(Map.Entry<Pair<String, RequestMethod>, MappingHandler> entry : mappingHandlerMap.entrySet()) {
			var key = entry.getKey();
			if(!key.getLeft().endsWith("/")) key = new Pair<>(key.getLeft() + "/", key.getRight());
			var rep = new Pair<>(path, requestMethod);
			if(key.equals(rep)) {
				v = entry.getValue();
				break;
			}
		}
		if(v == null) {
			throw new HandlerNotFoundException(request);
		};
		return Objects.requireNonNull(v, "Handler cannot be null").invoke(object, requestMethod, parameters, mappingConfigurer, requestBody, request, new Session() {}, pathvars);

	}

	public static final class MappingHandler {

		private static final Moshi moshi = new Moshi.Builder().build();
		private static final Map<Class<?>, JsonAdapter<?>> jsonAdapterMap = new Hashtable<>();
		@Getter @Setter private Method method;
		@Getter private final Set<RequestMethod> requestMethods;
		private final PriveWebServer server;
		@Getter private final String route;

		// single route; map values can use the same handler and route
		public MappingHandler(@NotNull Method method, @NotNull String route, @NotNull PriveWebServer server) {
			this.method = method;
			this.requestMethods = Set.of(RequestMethod.getRequestMethod(method));
			this.server = server;
			this.route = route;
			if(requestMethods.size() == 0) throw new IllegalArgumentException("Non-annotated method");
		}

		public MappingHandler(@NotNull Method method, @NotNull String route, @NotNull PriveWebServer server, @NotNull Set<RequestMethod> requestMethods) {
			this.method = method;
			this.requestMethods = requestMethods;
			this.server = server;
			this.route = route;
		}

		// ask for method to double-check and pass
		@Nullable
		@SneakyThrows
		public Object invoke(
				@NotNull Object object, // the object to use when invoking its containing method
				@NotNull RequestMethod method, // the RequestMethod used
				@Nullable QueryParameters queryParameters, // the query parameters (if there are none, it is null)
				@NotNull MappingConfiguration configuration, // the MappingConfiguration.
				@Nullable String requestBody, // the request body
				@NotNull HTTPRequest request, // the request itself
				@NotNull Session session,
				@NotNull String... pathvars // the path variables.
		) {
			var body = requestBody;
			if(server.isVerbose()) logger.info("Resolving path {} with {} path variables: {}", route, pathvars.length, Arrays.toString(pathvars));
			if(!requestMethods.contains(method)) {
				logger.warn("{!warn}Attempted to invoke non-existent {} request handler", method.name());
				return null;
			}
			if(server.isVerbose()) logger.info("Invoking {} request handler for route '{}'", method.name(), route);
			var params = this.method.getParameters();
			List<Object> actualParamList = new ArrayList<>(params.length);
			int pathvarCount = 0;
			for(int i = 0; i < params.length; i++) {
				Parameter param = params[i];
				Class<?> paramClass = param.getType();
				if(paramClass == method.getDeclaringClass()) {
					// param is instance of RequestMethod
					actualParamList.add(i, method);
				} else if(paramClass == QueryParameters.class) {
					actualParamList.add(i, queryParameters);
				} else if(paramClass == MappingConfiguration.class) {
					actualParamList.add(i, configuration);
				} else if(paramClass == Session.class) {
					actualParamList.add(i, session);
				} else if(paramClass == HTTPRequest.class) {
					actualParamList.add(i, request);
				} else if(paramClass == String.class) {
					if(param.isAnnotationPresent(PathParam.class)) {
						// perform checks first
						String pathvar;
						try {
							pathvar = pathvars[pathvarCount++];
						} catch(ArrayIndexOutOfBoundsException ignored) {
							pathvar = null;
						}
						actualParamList.add(i, pathvar);
					} else if(param.isAnnotationPresent(RequestBody.class)) {
						actualParamList.add(i, requestBody);
					}
				} else if(paramClass.isAnnotationPresent(RequestBody.class)) {
					JsonAdapter<?> jsonAdapter;
					if(requestBody != null) {
						if(jsonAdapterMap.containsKey(paramClass)) {
							jsonAdapter = jsonAdapterMap.get(paramClass);
						} else {
							jsonAdapter = moshi.adapter(paramClass);
							jsonAdapterMap.put(paramClass, jsonAdapter);
						}
						Object o;
						try {
							o = jsonAdapter.fromJson(requestBody);
						} catch(JsonDataException e) {
							if(server.isVerbose()) {
								logger.warn("{!warn}Unable to deserialize object '{}' (substring to index 25)", requestBody.substring(0, 25));
								logger.warn("{!warn}Value will be set as null.");
							}
							o = null;
 						}
						actualParamList.add(i, o);
					} else {
						actualParamList.add(i, null);
					}
				} else if(paramClass == PriveServer.class) {
					return server;
				} else {
					actualParamList.add(i, null);
				}
			}
			try {
				if(server.isVerbose()) logger.info("Calling method '{}' through reflection with parameters {}", this.method.getName(), actualParamList);
				Object obj = this.method.invoke(object, actualParamList.toArray(Object[]::new));
				if(server.isVerbose()) {
					if(obj == null) {
						logger.warn("{!warn}Returned object is null, setting response object to 200 OK");
						obj = HTTPResponse.builder()
								.responseCode(ResponseCode.S_204)
								.responseBody(new byte[0])
								.build();
					} else {
						logger.info("Successfully invoked, got result {}", obj.getClass().getSimpleName());
					}
				}
				return Objects.requireNonNull(obj);
			} catch(Exception e) {
				logger.error("{!error}Failed to invoke {} handler for '{}'; {}", method, route, e.toString());
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", MappingHandler.class.getSimpleName() + "[", "]").add("method=" + method).add("requestMethods=" + requestMethods).add("server=" + server).add("route='" + route + "'").toString();
		}
	}

	// utility methods

	@Contract("_ -> new")
	private static @NotNull RouteObject getRoutes(@NotNull AnnotatedElement obj) {
		ArrayList<String> get = new ArrayList<>();
		ArrayList<String> post = new ArrayList<>();
		if(obj.isAnnotationPresent(GetRequest.class)) get.addAll(List.of(obj.getAnnotation(GetRequest.class).value()));
		if(obj.isAnnotationPresent(PostRequest.class)) post.addAll(List.of(obj.getAnnotation(PostRequest.class).value()));
		if(obj.isAnnotationPresent(DeleteRequest.class)) post.addAll(List.of(obj.getAnnotation(DeleteRequest.class).value()));
		if(obj.isAnnotationPresent(OptionsRequest.class)) post.addAll(List.of(obj.getAnnotation(OptionsRequest.class).value()));
		if(obj.isAnnotationPresent(PatchRequest.class)) post.addAll(List.of(obj.getAnnotation(PatchRequest.class).value()));
		if(obj.isAnnotationPresent(PutRequest.class)) post.addAll(List.of(obj.getAnnotation(PutRequest.class).value()));
		return new RouteObject(get.toArray(String[]::new), post.toArray(String[]::new));
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", MappingResolver.class.getSimpleName() + "[", "]").add("mappingHandlerMap=" + mappingHandlerMap).toString();
	}
}
