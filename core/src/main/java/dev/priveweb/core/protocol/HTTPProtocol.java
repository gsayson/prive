package dev.priveweb.core.protocol;

/**
 * The HTTP protocol in use.
 */
public enum HTTPProtocol {

	HTTP1_1("HTTP/1.1"),
	; // currently only supported

	private final String s;
	HTTPProtocol(String s) { this.s = s; }

	@Override
	public String toString() {
		return s;
	}

}
