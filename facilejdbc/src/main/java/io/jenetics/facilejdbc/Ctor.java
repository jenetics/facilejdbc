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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
		final List<? extends Ctor.Field<? extends T>> fields
	) {
		requireNonNull(type);
		requireNonNull(toColumnName);
		requireNonNull(toColumnType);
		requireNonNull(fields);

		final Map<String, Ctor.Field<? extends T>> fieldsMap = fields.stream()
			.collect(toMap(
				Ctor.Field::name,
				f -> f,
				(a, b) -> { throw new IllegalArgumentException(format(
					"Duplicate field detected: %s", a.name()));}
			));

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
				if (fieldsMap.containsKey(columnNames[i])) {
					f[i] = fieldsMap.get(columnNames[i]).parse(row, conn);
				} else {
					f[i] = row.getObject(columnNames[i], columnTypes[i]);
				}
			}

			return create(constructor, f);
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

}
