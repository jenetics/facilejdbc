package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This interface is responsible for creating a record from given DB columns.
 *
 * @param <T> the record type
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface Ctor<T> {

	/**
	 * Contains the record field name and its value. These fields are read from
	 * the DB.
	 *
	 * @param name the name of the DB column
	 * @param value the SQL value, read from the DB
	 */
	final record Field(String name, Object value){}

	/**
	 * Constructs a <em>data</em> object from the given DB fields.
	 *
	 * @param fields the DB fields from where the object can be created from
	 * @return a newly created <em>data</em> object
	 */
	T apply(final Field[] fields);


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Creates a {@code Ctor} object from the given {@link Record} type.
	 *
	 * @param type the record type
	 * @param toFieldName maps the DB column names to the corresponding field
	 *        names of the created <em>data</em> object
	 * @param toFieldValue maps the DB value into the corresponding field type,
	 *        needed by the created <em>data</em> object
	 * @param <T> the record type
	 * @return a new constructor function for the given record {@code type}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T extends Record> Ctor<T> of(
		final Class<T> type,
		final UnaryOperator<String> toFieldName,
		final BiFunction<? super Class<?>, Object, Object> toFieldValue
	) {
		requireNonNull(type);
		requireNonNull(toFieldName);
		requireNonNull(toFieldValue);

		final var comps = type.getRecordComponents();
		final var indexes = IntStream.range(0, comps.length)
			.mapToObj(i -> Map.entry(comps[i].getName(), i))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Constructor<T> ctor = ctor(type);

		return fields -> {
			final var objects = new Object[comps.length];
			for (var field : fields) {
				final var name = toFieldName.apply(field.name());
				final var index = indexes.get(name);
				if (index != null) {
					objects[index] = toFieldValue.apply(
						comps[index].getType(),
						field.value()
					);
				}
			}

			return create(ctor, objects);
		};
	}

	private static <T> T create(final Constructor<T> ctor, final Object[] args) {
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

	private static <T extends Record> Constructor<T> ctor(final Class<T> type) {
		final var signature = Arrays.stream(type.getRecordComponents())
			.map(RecordComponent::getType)
			.toArray(Class<?>[]::new);

		try {
			return type.getConstructor(signature);
		} catch (NoSuchMethodException e) {
			throw new ClassFormatError(
				"Canonical record constructor must be available: " +
					e.getMessage()
			);
		}
	}

	/**
	 * Creates a {@code Ctor} object from the given {@link Record} type.
	 *
	 * @param type the record type
	 * @param <T> the record type
	 * @return a new constructor function for the given record {@code type}
	 * @throws NullPointerException if the given record {@code type} is
	 *         {@code null}
	 */
	static <T extends Record> Ctor<T> of(final Class<T> type) {
		return of(type, Ctor::toCamelCase, Mappings::mapTo);
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
