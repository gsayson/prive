# Prive
Prive is a web server for the JVM - it is lightweight and fast, and is an ideal solution compared to
embedding other heavyweight web servers such as Apache Tomcat, etc. This is a project for the 2022 Summer JavaJam.

## Supported protocols
As of now only one protocol is supported.<br>
The `protocol` package is shortened from `org.broskiclan.prive.protocol`.

|                    **Protocol**                    | **Supported** | **Implementation** |
|:--------------------------------------------------:|:-------------:|:------------------:|
| [HTTP/1.1](https://www.rfc-editor.org/rfc/rfc9110) |    1.0.0+     | `protocol.HTTP11`  |
|  [HTTP/2](https://www.rfc-editor.org/rfc/rfc8740)  |     Soon      |        N/A         |
|  [HTTP/3](https://www.rfc-editor.org/rfc/rfc9114)  |     Soon      |        N/A         |

## No servlets included
In Jitter there is no such thing as servlets. Instead, we rely on reflection to call implementations of two things:
- Interceptors (`org.broskiclan.http.interceptor.Interceptor`)
- Handlers (`org.broskiclan.http.request`)

## Handlers & Interceptors
Handlers are mapped to a URL, and interceptors will run code before running the handler.

### Handler Parameters
The following handler parameters are supported:

#### Path parameters (annotated with `@PathParam`)
`String` (will add more on next release, finish up for JavaJam)

#### Others
- `QueryParameters` - the query parameters given.
- `MappingConfiguration` - the ability to set a mapping.
- `Navigator` - the ability to `GET` another mapping.

### Returning values from a handler or interceptor
Supported return types - resolved from top (most specific) to bottom (least specific):
- `HTTPResponse` from `org.broskiclan.prive.http.response` - a fully-blown HTTP response.
- `ResponseObject<T>` from `org.broskiclan.prive.http.response` - the recommended type to return. It
  marshals the type into JSON (by default) and allows one to modify headers and set the response code.

## Error Handling
If there is an exception, it will invoke the set exception handler.
If there is an error invoking the exception handler, it will execute the default exception handler.

## Example

```java
import java.util.concurrent.Executors;
import dev.priveweb.core.PriveApplication;
import dev.priveweb.core.http.response.ResponseObject;
import dev.priveweb.core.http.request.verbs.*;
import dev.priveweb.core.http.ResponseCode;

public class Main {

	public static void main(String[] args) {
		// this is blocking
		// register the handlers of this object
		PriveApplication.start(new PriveWebServer(Executors.newWorkStealingPool(), 8080).register(new Main()));
	}
	
	@GetRequest("/", "/another-url") // multiple urls possible
	public ResponseObject<String> getHandler() {
		// ...
		return ResponseObject.<String>builder()
				.responseCode(ResponseCode.S_200)
				.responseBody("Hello, world!")
				.responseBodyClass(String.class)
				.marshalInto("text/plain") // by default this is "application/json"
				.build();
	}

}
```