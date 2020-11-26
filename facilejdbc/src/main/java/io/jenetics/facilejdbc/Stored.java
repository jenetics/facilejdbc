package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

public final record Stored<K, V>(K key, V value) {
	public Stored {
		requireNonNull(key);
	}
}
