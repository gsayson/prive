package dev.priveweb.core.data;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Contains a list of {@link DataCoder}s to handle the {@code Transfer-Encoding} header.
 * <p>
 *     {@link DataCoder}s are resolved from this registry from the first index, {@code 0}.
 *     Essentially, {@code DataCoder}s from index {@code 0} have higher priority than those
 *     with indexes {@linkplain List#size() near the end index of the list}.
 * </p>
 * <p>
 *     This class maintains safety during concurrent operations.
 *     Hence, unless otherwise stated, all methods in this class are blocking.
 * </p>
 * <pre>{@code
 * PriveServer server = ...;
 * DataEncodingRegistry registry = server.getDataEncoderRegistry();
 * DataCoder encoder = registry.resolveEncoder("gzip"); // Transfer-Encoding: gzip
 * byte[] string = "Hello, world!".getBytes();
 * byte[] encoded = encoder.encode("Hello, world!".getBytes());
 * Checks.ensureEquals(string, encoder.decode(encoded));
 * }</pre>
 */
public final class DataEncodingRegistry {

	public DataEncodingRegistry() {
		//no instance
	}

	private static final List<@NotNull DataCoder> dataCoders = new ArrayList<>();
	private static final ReadWriteLock rwl = new ReentrantReadWriteLock(true);
	private static final Lock wl = rwl.writeLock();
	private static final Lock rl = rwl.readLock();

	/**
	 * Registers one or more {@link DataCoder}s.
	 * @param coders The coders to register.
	 */
	@Blocking
	public void register(@NotNull DataCoder... coders) {
		wl.lock();
		dataCoders.addAll(List.of(coders));
		wl.unlock();
	}

	/**
	 * Registers one {@link DataCoder} in the given {@code position}.
	 * @param coder The coder to register.
	 * @param position The index to add the given {@code DataCoder} to.
	 */
	@Blocking
	public void register(@NotNull DataCoder coder, int position) {
		wl.lock();
		dataCoders.add(position, coder);
		wl.unlock();
	}

	/**
	 * Resolves a {@link DataCoder} from the contained list
	 * that supports the given encoding name.
	 * @param encodingName The encoding name, according to
	 *                     <a href="https://www.rfc-editor.org/rfc/rfc9112.html#transfer.codings">RFC 9112, section 7</a>.
	 * @return the resolved {@link DataCoder}, else {@code null}.
	 */
	@Contract(pure = true)
	public @Nullable DataCoder resolve(String encodingName) {
		rl.lock();
		for(DataCoder coder : dataCoders) {
			if(coder.getCoderName().equalsIgnoreCase(encodingName)) return coder;
		}
		rl.unlock();
		return null;
	}

}
