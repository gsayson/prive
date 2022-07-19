package dev.priveweb.core.util;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * A pair.
 * @param <L> The left element.
 * @param <R> The right element.
 */
@ApiStatus.NonExtendable
public class Pair<L, R> {

	@Getter private final L left;
	@Getter private final R right;

	public Pair(L l, R r) {
		this.left = l;
		this.right = r;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Pair<?, ?> pair)) return false;
		if(!Objects.equals(left, pair.left)) return false;
		return Objects.equals(right, pair.right);
	}

	@Override
	public int hashCode() {
		return left.hashCode() + right.hashCode();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Integer.toHexString(hashCode()) + "[", "]").add("left=" + left).add("right=" + right).toString();
	}
}
