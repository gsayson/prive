package dev.priveweb.core.http;

import dev.priveweb.core.http.request.RequestMethod;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * HTTP response codes.
 * Each enum constant starts with a letter:
 * <ul>
 *     <li>{@code I} is an informational response</li>
 *     <li>{@code S} is a successful response</li>
 *     <li>{@code R} is a redirection response</li>
 *     <li>{@code C} is a client error response</li>
 *     <li>{@code SV} is a server error response</li>
 * </ul>
 */
@SuppressWarnings("unused")
public enum ResponseCode {

	// INFORMATIONAL responses
	I_100("Continue"),
	I_101("Switching Protocols"),
	/**
	 * This is a WebDAV response.
	 */
	I_103("Processing"),

	// SUCCESS responses
	S_200("OK"),
	S_201("Created"),
	S_203("Non-Authoritative Information"),
	S_204("No Content"),
	S_205("Reset Content"),
	S_206("Partial Content"),
	/**
	 * This is a WebDAV response.
	 */
	S_207("Multi-Status"),
	/**
	 * This is a WebDAV response.
	 */
	S_208("Already Reported"),
	/**
	 * This is a response from the HTTP Delta encoding.
	 */
	S_226("IM Used"),
	R_300("Multiple Choices"),
	R_301("Moved Permanently"),
	R_302("Found"),
	R_303("See Other"),
	R_304("Not Modified"),
	// skip R_305, due to security concerns,
	// skip R_306, it is unused and only reserved.
	R_307("Temporary Redirect."),
	R_308("Permanent Redirect"),

	// CLIENT ERROR responses
	C_400("Bad Request"),
	C_401("Unauthorized"),
	/**
	 * According to <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">the MDN docs</a>,
	 * this error response is experimental.
	 */
	@ApiStatus.Experimental
	C_402("Payment Required"),
	C_403("Forbidden"),
	C_404("Not Found"),
	C_405("Method Not Allowed"),
	C_406("Not Acceptable"),
	C_407("Proxy Authentication Required"),
	C_408("Request Timeout"),
	C_409("Conflict"),
	C_410("Gone"),
	C_411("Length Required"),
	C_412("Precondition Failed"),
	C_413("Payload Too Large"),
	C_414("URI Too Long"),
	C_415("Unsupported Media Type"),
	C_416("Range Not Satisfiable"),
	C_417("Expectation Failed"),
	C_418("I'm a teapot"),
	C_421("Misdirected Request"),
	/**
	 * This is a WebDAV response.
	 */
	C_422("Unprocessable Entity"),
	/**
	 * This is a WebDAV response.
	 */
	C_423("Locked"),
	/**
	 * This is a WebDAV response.
	 */
	C_424("Unprocessable Entity"),
	/**
	 * According to <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">the MDN docs</a>,
	 * this error response is experimental.
	 */
	@ApiStatus.Experimental
	C_425("Too Early"),
	C_426("Upgrade Required"),
	C_428("Precondition Required"),
	C_429("Too Many Requests"),
	C_431("Request Header Fields Too Large"),
	C_451("Unavailable For Legal Reasons"),

	// SERVER ERROR responses
	SV_500("Internal Server Error"),
	/**
	 * This code cannot be used on {@link RequestMethod#GET GET}
	 * or {@code HEAD} responses; Doing so results in an exception, returning {@linkplain #SV_500 Error 500}.
	 */
	SV_501("Not Implemented"),
	SV_502("Bad Gateway"),
	SV_503("Service Unavailable"),
	SV_504("Gateway Timeout"),
	SV_505("HTTP Version Not Supported"),
	SV_506("Variant Also Negotiates"),
	/**
	 * This is a WebDAV response.
	 */
	SV_507("Insufficient Storage"),
	/**
	 * This is a WebDAV response.
	 */
	SV_508("Loop Detected"),
	SV_510("Not Extended"),
	SV_511("Network Authentication Required");

	/**
	 * The message accompanying the response code.
	 */
	@Getter
	private final String message;

	/**
	 * Constructor for the {@link ResponseCode} enum.
	 * @param message The message accompanying the response code,
	 *                according to <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">the MDN docs</a>.
	 */
	ResponseCode(String message) {
		this.message = message;
	}

	@Override
	@SuppressWarnings("RegExpSimplifiable")
	public @NotNull String toString() {
		return name().replaceAll("[^0-9]", "") + " " + message;
	}

	@SuppressWarnings("RegExpSimplifiable")
	public int toInteger() {
		return Integer.parseInt(name().replaceAll("[^0-9]", ""));
	}

}