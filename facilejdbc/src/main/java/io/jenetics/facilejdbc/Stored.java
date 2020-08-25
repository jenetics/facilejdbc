package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

public final class Stored<K, V> {
	private final K key;
	private final V value;

	private Stored(final K key, final V value) {
		this.key = requireNonNull(key);
		this.value = value;
	}

	public K key() {
		return key;
	}

	public V value() {
		return value;
	}

	public static <V> Stored<Long, V> of(final long id, final V value) {
		return new Stored<>(id, value);
	}

}
