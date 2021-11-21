/*
 * Facile JDBC Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
public interface Ctor<T> extends RowParser<T> {


	interface Field<T> extends RowParser<T> {
		String name();

		static <T> Field<T> of(final String name, final RowParser<? extends T> parser) {
			requireNonNull(name);
			requireNonNull(parser);

			return new Field<T>() {
				@Override
				public String name() {
					return name;
				}
				@Override
				public T parse(final Row row, final Connection conn)
					throws SQLException
				{
					return parser.parse(row, conn);
				}
			};
		}
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	static <T extends Record> Ctor<T> of(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName,
		final Function<? super RecordComponent, Class<?>> toColumnType,
		final Function<? super RecordComponent, RowParser<?>> parsers
	) {
		final RecordComponent[] components = type.getRecordComponents();

		final String[] columnNames = Stream.of(components)
			.map(toColumnName)
			.toArray(String[]::new);

		final Class<?>[] columnTypes = Stream.of(type.getRecordComponents())
			.map(toColumnType)
			.toArray(Class<?>[]::new);

		final Constructor<T> constructor;
		try {
			constructor = type.getConstructor(columnTypes);
		} catch (NoSuchMethodException e) {
			throw new ClassFormatError(
				"Canonical record constructor must be available: " +
					e.getMessage()
			);
		}

		return (row, conn) -> {
			final Object[] f = new Object[components.length];
			for (int i = 0; i < components.length; ++i) {
				f[i] = row.getObject(columnNames[i], columnTypes[i]);
			}

			return create(constructor, f);
		};
	}

	/**
	 * Creates a {@code Ctor} object from the given {@link Record} type.
	 *
	 * @param type the record type
	 * @param toFieldName maps the DB column names to the corresponding field
	 *        names of the created <em>data</em> object
	 * @param fieldMapping maps the DB value into the corresponding field type,
	 *        needed by the created <em>data</em> object
	 * @param <T> the record type
	 * @return a new constructor function for the given record {@code type}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T extends Record> Ctor<T> of(
		final Class<T> type,
		final UnaryOperator<String> toFieldName,
		final Mapping fieldMapping
	) {
		requireNonNull(type);
		requireNonNull(toFieldName);
		requireNonNull(fieldMapping);

		final var comps = type.getRecordComponents();
		final var indexes = IntStream.range(0, comps.length)
			.mapToObj(i -> Map.entry(comps[i].getName(), i))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Constructor<T> ctor = ctor(type);

		return null;

//		return fields -> {
//			final var objects = new Object[comps.length];
//			for (var field : fields) {
//				final var name = toFieldName.apply(field.name());
//				final var index = indexes.get(name);
//				if (index != null) {
//					objects[index] = fieldMapping.map(
//						field.value(),
//						comps[index].getType()
//					);
//				}
//			}
//
//			return create(ctor, objects);
//		};
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

//	/**
//	 * Creates a {@code Ctor} object from the given {@link Record} type.
//	 *
//	 * @param type the record type
//	 * @param <T> the record type
//	 * @return a new constructor function for the given record {@code type}
//	 * @throws NullPointerException if the given record {@code type} is
//	 *         {@code null}
//	 */
//	static <T extends Record> Ctor<T> of(final Class<T> type) {
//		return of(type, Ctor::toCamelCase, Mappings::mapper);
//	}

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
