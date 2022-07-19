package dev.priveweb.core.data;

import dev.priveweb.core.exception.MalformedRequestException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

/**
 * An interface to define data encoding and decoding methods.
 */
public interface DataCoder {

	/**
	 * Return the encoding name. For example, {@code gzip} or {@code chunked}.
	 * <p>Providing incorrect output can lead to Prive malfunctioning.</p>
	 * @return the encoding name.
	 */
	@Contract(pure = true)
	@NotNull String getCoderName();

	/**
	 * Encodes the given bytes according to this {@link DataCoder}'s encoding,
	 * which can be found through {@link #getCoderName()}.
	 * @param bytes The bytes to encode.
	 * @param charset The charset of which the bytes are in.
	 * @return the encoded bytes.
	 */
	@Contract(pure = true)
	byte[] encode(byte[] bytes, @NotNull Charset charset);

	/**
	 * Decodes the given bytes according to this {@link DataCoder}'s encoding,
	 * which can be found through {@link #getCoderName()}.
	 * @param bytes The bytes to decode.
	 * @param charset The charset of which the bytes are in.
	 * @return the decoded bytes.
	 * @throws MalformedRequestException if the given bytes does not correspond to the supported encoding.
	 */
	@Contract(pure = true)
	byte[] decode(byte[] bytes, @NotNull Charset charset) throws MalformedRequestException;

}
