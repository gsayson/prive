package dev.priveweb.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A utility class for asserting correct program execution.
 */
@SuppressWarnings("unused")
public abstract class Checks {

	private Checks() {
		//no instance
	}

	/**
	 * Ensures that two objects are equal.
	 * @param o1 The first object to compare.
	 * @param o2 The second object to compare.
	 * @throws RuntimeException if both objects are not equal.
	 */
	@SuppressWarnings({"StatementWithEmptyBody"})
	public static void ensureEquals(@Nullable Object o1, @Nullable Object o2) {
		if(o1 == o2 && o1 == null) {}
		else if(o1 != o2 && (o1 == null || o2 == null)) {
			throw new RuntimeException("a null object and a non-null object are not equal, " + (o1 == null ? "o1" : "o2") + " is null");
		} else if(o1.getClass() != o2.getClass()) {
			throw new RuntimeException("classes of o1 and o2 do not correspond");
		} else if(o1.getClass().isArray()) {
			ensureEquals((Object[]) o1, (Object[]) o2);
		} else if(!Objects.equals(o1, o2)) {
			throw new RuntimeException("both non-null objects " + o1 + " and object " + o2 + " are not equal");
		}
	}

	public static void ensureEquals(@Nullable Object[] o1, @Nullable Object[] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensureEquals(List.of(o1), List.of(o2));
	}

	public static void ensureEquals(byte @NotNull [] o1, byte @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	public static void ensureEquals(short @NotNull [] o1, short @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	public static void ensureEquals(int @NotNull [] o1, int @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	public static void ensureEquals(long @NotNull [] o1, long @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	public static void ensureEquals(float @NotNull [] o1, float @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	public static void ensureEquals(double @NotNull [] o1, double @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	public static void ensureEquals(char @NotNull [] o1, char @NotNull [] o2) {
		if(o1.length != o2.length)
			throw new RuntimeException("array o1 with length " + o1.length + " and array o2 with length " + o2.length + " do not correspond in length");
		ensure(Arrays.equals(o1, o2));
	}

	/**
	 * Ensures that two {@link Iterable}s are equal.
	 * @param i1 The first {@code Iterable} to compare.
	 * @param i2 The second {@code Iterable} to compare.
	 * @throws RuntimeException if both {@code Iterable}s are not equal.
	 */
	public static <T> void ensureEquals(@Nullable Iterable<T> i1, @Nullable Iterable<T> i2) {
		if(i1 == i2 && i1 == null) return;
		if(i1 != i2 && (i1 == null || i2 == null)) {
			throw new RuntimeException("a null object and a non-null object are not equal, " + (i1 == null ? "i1" : "i2") + " is null");
		}
		var iter1 = i1.iterator();
		var iter2 = i2.iterator();
		while(iter1.hasNext() && iter2.hasNext()) {
			ensureEquals(iter1.next(), iter2.next());
		}
		// there should not be any more elements remaining
		if(iter1.hasNext() || iter2.hasNext()) {
			throw new RuntimeException("there are still elements remaining in iterator " + (iter1.hasNext() ? "i1" : "i2"));
		}
	}

	public static void ensure(boolean bool) {
		if(!bool) {
			throw new RuntimeException("bool is not true");
		}
	}

	public static void ensurePositive(@NotNull Number number) {
		if(number.longValue() < 0)
			throw new RuntimeException("negative number passed");
	}

}
