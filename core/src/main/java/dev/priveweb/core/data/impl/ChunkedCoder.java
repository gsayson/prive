package dev.priveweb.core.data.impl;

import dev.priveweb.core.util.Checks;
import gnu.trove.list.TCharList;
import gnu.trove.list.linked.TCharLinkedList;
import dev.priveweb.core.data.DataCoder;
import dev.priveweb.core.exception.MalformedRequestException;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class implements the {@code chunked} encoding.
 */
public class ChunkedCoder implements DataCoder {

	@Override
	public @NotNull String getCoderName() {
		return "chunked";
	}

	@Override
	public byte[] encode(byte @NotNull [] bytes, @NotNull Charset charset) {
		// Do not encode anything, there is no point encoding for `chunked`.
		return bytes;
	}

	@Override
	public byte[] decode(byte @NotNull [] bytes, @NotNull Charset charset) throws MalformedRequestException {
		if(bytes.length == 0) return bytes;
		Checks.ensurePositive(bytes.length); // prevent overflow
		String s = new String(bytes, charset);
		// perform basic checks
		if(!s.endsWith("0\r\n\r\n")) {
			throw new MalformedRequestException("the bytes do not end with \\r\\n, thus they do not conform to the chunked encoding");
		}
		var chars = s.toCharArray();
		TCharList totalBuf = new TCharLinkedList(); // total buffer
		TCharList loopBuf = new TCharLinkedList(); // loop buffer
		int charsToRead = -1; // -1 is a value for declaring an undefined number of chars to read.
		int charsRead = 0;
		char c;
		for(int i = 0; i < chars.length; ++i) {
			c = chars[i];
			if(charsToRead < 0) {
				if(c == '\r' && chars[i + 1] == '\n') {
					try {
						var str = String.valueOf(loopBuf.toArray(new char[loopBuf.size()])).strip();
						str = new StringTokenizer(str).nextToken(); // ignore our unsupported chunked extensions (basically anything): https://httpwg.org/specs/rfc9112.html#chunked.extension
						charsToRead = Integer.parseInt(str, 16);
						if(charsToRead < 0) {
							totalBuf = null;
							loopBuf = null;
							throw new MalformedRequestException("the number of bytes to read is negative");
						}
						loopBuf.clear();
					} catch(NumberFormatException numberFormatException) {
						totalBuf = null;
						loopBuf = null;
						throw new MalformedRequestException("the number of bytes to read is not in hexadecimal", numberFormatException);
					}
					i++;
				} else {
					loopBuf.add(c);
				}
			} else {
				// there are still chars to read
				if(charsRead == charsToRead) {
					charsToRead = -1; // reset
					charsRead = 0;
				} else {
					totalBuf.add(c);
					charsRead++;
				}
			}
		}
		return new String(totalBuf.toArray(new char[totalBuf.size()])).getBytes(charset);
	}

	// UTILITY

	/**
	 * Converts the given bytes into a byte array.
	 * @param bytes the given bytes.
	 * @return the byte array.
	 */
	private static byte @NotNull [] toArray(@NotNull List<Byte> bytes) {
		return bytes.stream()
				.mapToInt(Byte::intValue)
				.collect(
						ByteArrayOutputStream::new,
						(bs, i) -> bs.write((byte) i),
						(bs1, bs2) -> bs1.write(bs2.toByteArray(), 0, bs2.size()))
				.toByteArray();
	}

}
