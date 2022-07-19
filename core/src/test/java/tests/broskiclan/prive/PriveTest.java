package tests.broskiclan.prive;

import dev.priveweb.core.data.impl.ChunkedCoder;
import dev.priveweb.core.http.ResponseCode;
import dev.priveweb.core.http.handlers.ResourceHandler;
import dev.priveweb.core.http.request.HTTPRequest;
import dev.priveweb.core.http.request.RequestMethod;
import dev.priveweb.core.http.response.ResponseObject;
import dev.priveweb.core.protocol.HTTPProtocol;
import dev.priveweb.core.server.impl.PriveWebServer;
import dev.priveweb.core.util.Checks;
import dev.priveweb.core.util.IOUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

@SuppressWarnings("all")
public class PriveTest {

	@Test
	@SneakyThrows
	public void launch() {
		var server = new PriveWebServer(Executors.newWorkStealingPool(), 1234);
		server.start(true);
		Thread.sleep(200);
		try(Socket socket = new Socket("localhost", 1234)) {}
		server.halt();
	}

	@Test
	@SneakyThrows
	public void handler_mechanism_on_web_server() {
		var server = new PriveWebServer(Executors.newWorkStealingPool(), 8664); // random port
		server.register(new TestListener());
		server.start(true);
		try(
				Socket socket = new Socket("localhost", 8664);
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())))
		{
			writer.println(
					HTTPRequest.builder()
							.protocol("HTTP/1.1")
							.requestMethod(RequestMethod.GET)
							.requestedResource("/nav")
							.build()
			);
			writer.flush();
		}
		server.halt();
	}

	@Test
	public void test_JSON_marshalling() {
		ResponseObject<TestClass> responseObject = ResponseObject.<TestClass>builder()
				.responseCode(ResponseCode.S_200)
				.responseBody(new TestClass())
				.responseBodyClass(TestClass.class)
				.build();
		System.out.println(responseObject.marshal(HTTPProtocol.HTTP1_1));
	}

	public static class TestClass {
		private final String string;
		private final List<String> strings;
		public TestClass() {
			this.string = "Hello, world!";
			this.strings = List.of("Hello", "world!");
		}
	}

	@Test
	@SneakyThrows
	public void chunked_encoding_test() {
		var charset = Charset.defaultCharset();
		var uuidStr = UUID.randomUUID().toString();
		var bytes = (Integer.toHexString(uuidStr.length()) + "\r\n" + uuidStr + "\r\n" + Integer.toHexString(uuidStr.length()) + "\r\n" + uuidStr + "\r\n0\r\n\r\n").getBytes(charset);
		var decode = new ChunkedCoder().decode(bytes, charset);
		Checks.ensureEquals(decode.length, uuidStr.length() + uuidStr.length());
	}

	@Test
	public void test_request_body() throws IOException, InterruptedException, URISyntaxException {
		var server = new PriveWebServer(Executors.newWorkStealingPool(), 8080);
		server.start(true);
		HttpClient client = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:8080/non-existent-url/"))
				.POST(HttpRequest.BodyPublishers.ofString("Hello, world!"))
				.build();
		var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		server.halt();
	}

}
