package io.jenetics.facilejdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.SQLNonTransientException;
import java.util.stream.Stream;

/**
 * Some reflection helper methods.
 */
final class Reflections {
	private Reflections() {
	}

	static <T extends Record> Constructor<T> ctor(final Class<T> type) {
		final Class<?>[] columnTypes = Stream.of(type.getRecordComponents())
			.map(RecordComponent::getType)
			.toArray(Class<?>[]::new);

		try {
			return type.getDeclaredConstructor(columnTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"Canonical record constructor must be available.", e
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

	static Object value(final RecordComponent component, final Object record)
		throws SQLNonTransientException
	{
		try {
			return component.getAccessor().invoke(record);
		} catch (IllegalAccessException e) {
			throw new SQLNonTransientException(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException re) {
				throw re;
			} else if (e.getCause() instanceof Error error) {
				throw error;
			} else {
				throw new SQLNonTransientException(e.getCause());
			}
		}
	}

}
