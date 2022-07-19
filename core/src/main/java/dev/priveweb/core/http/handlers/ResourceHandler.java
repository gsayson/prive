package dev.priveweb.core.http.handlers;

import dev.priveweb.core.http.Header;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.http.interceptor.Setup;
import dev.priveweb.core.http.request.verbs.GetRequest;
import dev.priveweb.core.http.response.HTTPResponse;
import dev.priveweb.core.mapper.MappingConfiguration;
import dev.priveweb.core.server.PriveServer;
import dev.priveweb.core.util.IOUtils;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.SneakyThrows;
import dev.priveweb.core.http.request.PathParam;
import dev.priveweb.core.http.request.RequestMethod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Handler for {@link GetRequest GetRequest}s on managed resources.
 * <p>This does not provide security.</p>
 */
@Data
@Builder
public class ResourceHandler implements Setup {

	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(ResourceHandler.class));

	/**
	 * The path prefix to any resource. For example,<br>
	 * If we were to have a resource that we want to put a resource prefix to:
	 * <pre>/image.png</pre>
	 * We can add a prefix {@code /resource/}, thus making the path to access it:
	 * <pre>/resource/image.png</pre>
	 */
	@Builder.Default
	private @NotNull String resourcePrefix = "/";

	@Singular
	private @NotNull Set<Path> resources;

	/**
	 * Whether to show the {@code index.html} file if pointing to a directory.
	 */
	@Builder.Default
	private boolean showIndexHTMLFileIfInDirectory = true;

	// add routes dynamically
	@Override
	@SneakyThrows
	public void setup(@NotNull MappingConfiguration configuration, @NotNull PriveServer server) {
		var resourcePrefix2 = resourcePrefix;
		if(!resourcePrefix2.endsWith("/")) {
			resourcePrefix2 += "/";
		}
		configuration.map(resourcePrefix2 + "{}", RequestMethod.GET, getClass().getDeclaredMethod("getResource", String.class));
	}

	/**
	 * The handler for getting resources. This is not annotated;
	 * the registration is done by the {@link #setup(MappingConfiguration, PriveServer)}.
	 * @param resource The resource to retrieve.
	 */
	public HTTPResponse getResource(@PathParam String resource) throws IOException {
		var builder = HTTPResponse.builder();
		builder.protocol("HTTP/1.1");
		Path target = null;
		for(Path res : resources) {
			if(res.toString().replace("\\", "/").endsWith(resource)) {
				if(Files.isDirectory(res)) {
					var res2 = res.toString();
					if(!res2.endsWith("/")) res2 += "/"; 
					if(showIndexHTMLFileIfInDirectory) {
						res2 += "index.html";
						target = Path.of(res2);
					}
				} else target = res;
				break;
			}
		}
		if(target == null) {
			return builder.responseCode(ResponseCode.C_404)
					.header(new Header("Content-Length", "0"))
					.responseBody(new byte[0])
					.build();
		} else {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			BufferedOutputStream dataOutputStream = new BufferedOutputStream(byteArrayOutputStream);
			var data = IOUtils.readFromPath(target);
			dataOutputStream.write(data, 0, data.length);
			dataOutputStream.flush();
			byteArrayOutputStream.flush();
			return builder.responseCode(ResponseCode.S_200)
					.responseBody(byteArrayOutputStream.toByteArray())
					.header(new Header("Content-Type", Files.probeContentType(target) + "; charset=utf-8"))
					.header(new Header("Content-Length", String.valueOf(data.length)))
					.build();
		}
	}

}