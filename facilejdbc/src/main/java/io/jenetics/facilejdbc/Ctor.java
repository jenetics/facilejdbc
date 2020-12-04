package io.jenetics.facilejdbc;

import static io.jenetics.facilejdbc.Mappings.mapTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Ctor<T> extends Function<List<? extends Ctor.Field<?>>, T> {

	final record Field<T>(String name, T value){}

	static <T extends Record> Ctor<T>
	of(final Class<T> type, final UnaryOperator<String> toFieldName) {
		final var comps = type.getRecordComponents();
		final var indexes = IntStream.range(0, comps.length)
			.mapToObj(i -> Map.entry(comps[i].getName(), i))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final var signature = Arrays.stream(comps)
			.map(RecordComponent::getType)
			.toArray(Class<?>[]::new);

		final Constructor<T> constructor;
		try {
			constructor = type.getConstructor(signature);
		} catch (NoSuchMethodException e) {
			throw new ClassFormatError(e.getMessage());
		}

		return fields -> {
			final var objects = new Object[comps.length];
			for (var field : fields) {
				final var name = toFieldName.apply(field.name());
				final var index = indexes.get(name);
				if (index != null) {
					objects[index] = mapTo(comps[index].getType(), field.value());
				}
			}

			try {
				return constructor.newInstance(objects);
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
		};
	}

	static <T extends Record> Ctor<T> of(final Class<T> type) {
		return of(type, Ctor::toCamelCase);
	}

	private static String toCamelCase(final String name) {
		final var result = new StringBuilder();

		boolean underscore = false;
		for (int i = 0; i < name.length(); i++) {
			final char ch = name.charAt(i);
			if (ch == '_') {
				underscore = true;
			} else {
				if (underscore) {
					result.append(Character.toUpperCase(ch));
				} else {
					result.append(Character.toLowerCase(ch));
				}
				underscore = false;
			}
		}

		return result.toString();
	}

}
