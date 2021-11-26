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
import static io.jenetics.facilejdbc.Reflections.create;
import static io.jenetics.facilejdbc.Reflections.ctor;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
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


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	static <T extends Record> Ctor<T> of(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName,
		final Function<? super RecordComponent, Class<?>> toColumnType,
		final Function<? super RecordComponent, ? extends RowParser<?>> fields
	) {
		requireNonNull(type);
		requireNonNull(toColumnName);
		requireNonNull(toColumnType);
		requireNonNull(fields);

		final RecordComponent[] components = type.getRecordComponents();

		final String[] columnNames = Stream.of(components)
			.map(toColumnName)
			.toArray(String[]::new);

		final Class<?>[] columnTypes = Stream.of(type.getRecordComponents())
			.map(toColumnType)
			.toArray(Class<?>[]::new);

		final Constructor<T> ctor = ctor(type);

		return (row, conn) -> {
			final Object[] values = new Object[components.length];
			for (int i = 0; i < components.length; ++i) {
				final var field = fields.apply(components[i]);
				if (field != null) {
					values[i] = field.parse(row, conn);
				} else {
					values[i] = row.getObject(columnNames[i], columnTypes[i]);
				}
			}

			return create(ctor, values);
		};
	}

}
