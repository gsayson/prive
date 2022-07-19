package dev.priveweb.core.util;

import dev.priveweb.core.marshal.Marshallable;
import dev.priveweb.core.protocol.HTTPProtocol;
import dev.priveweb.core.http.Header;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.http.response.HTTPResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class IOUtils {

	private static final Logger logger = AnsiLogger.of(LoggerFactory.getLogger(IOUtils.class));

	private IOUtils() {
		//no instance
	}

	public static @NotNull String getProgramPath() throws IOException {
		return "D:\\java\\prive\\core\\src\\main\\resources"; //new File(".").getCanonicalPath();
	}

	public static byte[] readFromPath(Path path) throws IOException {
		// if negative, the file is way too big to serve;
		// I don't know about other systems, so this guard check is here.
		if((int) Files.size(path) < 0) {
			logger.error("{!error}Unable to serve file in '{}', size is too big", path);
		}

		// if(Files.size(path > 2GB) { ... }
		if(Files.size(path) > 2000000000) {
			// use BufferedInputStream method
			logger.warn("{!warn}Serving file in '{}' over 2GB, the thread serving the client may be slow", path);
			try(InputStream inputStream = new BufferedInputStream(Files.newInputStream(path), (int) Files.size(path))) {
				return inputStream.readAllBytes();
			}
		} else {
			return Files.readAllBytes(path);
		}
	}

	@NotNull
	public static HTTPResponse marshalObjectIntoResponse(@Nullable Object object, @NotNull HTTPProtocol protocol) {
		// fast-path cases
		if(object == null) return HTTPResponse.builder()
				.responseCode(ResponseCode.S_200)
				.responseBody(new byte[0])
				.header(new Header("Content-Length", "0"))
				.build();
		if(object instanceof Marshallable marshallable) return marshallable.marshal(protocol);
		throw new RuntimeException("Unsupported return type");
	}

}
