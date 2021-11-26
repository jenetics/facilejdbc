package io.jenetics.facilejdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.stream.Stream;

final class Reflections {
	private Reflections() {
	}

	static <T extends Record> Constructor<T> ctor(final Class<T> type) {
		final Class<?>[] columnTypes = Stream.of(type.getRecordComponents())
			.map(RecordComponent::getType)
			.toArray(Class<?>[]::new);

		try {
			return type.getConstructor(columnTypes);
		} catch (NoSuchMethodException e) {
			throw new ClassFormatError(
				"Canonical record constructor must be available: " +
					e.getMessage()
			);
		}
	}

	static <T> T create(final Constructor<T> ctor, final Object[] args) {
		try {
			return ctor.newInstance(args);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException rte) {
				throw rte;
			} else if (e.getCause() instanceof Error error) {
				throw error;
			} else {
				throw new RuntimeException(e.getCause());
			}
		} catch (InstantiationException|IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
