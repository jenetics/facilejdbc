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
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static io.jenetics.facilejdbc.Dctor.field;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.SQLNonTransientException;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods for handling {@link Record} types with <em>FacileJDBC</em>.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 2.0
 */
public final class Records {
	private Records() {
	}

	/**
	 * Create a new deconstructor for the given record type.
	 *
	 * @since 2.0
	 *
	 * @param record the record type to deconstruct
	 * @param toColumnName function for mapping the component names to the
	 *        column names of the DB
	 * @param fields the fields which overrides/extends the automatically
	 *        extracted fields from the record
	 * @param <T> the record type
	 * @return a new deconstructor for the given record type
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if there are duplicate fields
	 */
	@SafeVarargs
	public static <T extends Record> Dctor<T> dctor(
		final Class<T> record,
		final UnaryOperator<String> toColumnName,
		final Dctor.Field<? super T>... fields
	) {
		requireNonNull(record);
		requireNonNull(toColumnName);
		requireNonNull(fields);

		final List<Dctor.Field<? super T>> list = asList(fields);
		final Map<String, Dctor.Field<? super T>> fieldMap = toMap(list);

		return Dctor.of(
			Stream.of(record.getRecordComponents())
				.map(c -> Records.<T>toFiled(c, toColumnName, fieldMap))
				.toList()
		);
	}

	private static <T> Map<String, Dctor.Field<? super T>>
	toMap(final List<? extends Dctor.Field<? super T>> fields) {
		return fields.stream()
			.collect(Collectors.toMap(
				Dctor.Field::name,
				f -> f,
				(a, b) -> { throw new IllegalArgumentException(format(
					"Duplicate field detected: %s", a.name()));}));
	}

	private static <T extends Record> Dctor.Field<? super T> toFiled(
		final RecordComponent component,
		final UnaryOperator<String> toColumnName,
		final Map<String, Dctor.Field<? super T>> fields
	) {
		final String name = toColumnName.apply(component.getName());
		return fields.getOrDefault(
			name,
			field(name, record -> value(component, record))
		);
	}

	private static Object value(
		final RecordComponent component,
		final Object record
	)
		throws SQLNonTransientException
	{
		try {
			return component.getAccessor().invoke(record);
		} catch (IllegalAccessException e) {
			throw new SQLNonTransientException(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException re) {
				throw re;
			} else if (e.getCause() instanceof Error er) {
				throw er;
			} else {
				throw new SQLNonTransientException(e.getCause());
			}
		}
	}

}


