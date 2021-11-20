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
import static io.jenetics.facilejdbc.Dctor.field;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.SQLNonTransientException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
	 * @see #dctor(Class, Function, Dctor.Field[])
	 *
	 * @param type the record type to deconstruct
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param fields the fields which overrides/extends the
	 *        automatically extracted fields from the record
	 * @param <T> the record type
	 * @return a new deconstructor for the given record type
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if there are duplicate fields defined
	 */
	public static <T extends Record> Dctor<T> dctor(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName,
		final List<? extends Dctor.Field<? super T>> fields
	) {
		requireNonNull(type);
		requireNonNull(toColumnName);
		requireNonNull(fields);

		final Map<String, Dctor.Field<? super T>> fieldsMap = fields.stream()
			.collect(
				Collectors.toMap(
					Dctor.Field::name,
					f -> f,
					(a, b) -> {
						throw new IllegalArgumentException(
							"Duplicate field detected: '%s'.".formatted(a.name())
						);
					},
					LinkedHashMap::new
				)
			);

		final List<Dctor.Field<? super T>> recordFields = Stream.of(type.getRecordComponents())
			.map(c -> Records.<T>toFiled(c, toColumnName, fieldsMap))
			.collect(Collectors.toList());

		recordFields.addAll(fieldsMap.values());

		return Dctor.of(recordFields);
	}

	private static <T extends Record> Dctor.Field<? super T> toFiled(
		final RecordComponent component,
		final Function<? super RecordComponent, String> toColumnName,
		final Map<String, Dctor.Field<? super T>> fields
	) {
		final String columnName = toColumnName.apply(component);

		return fields.containsKey(columnName)
			? fields.remove(columnName)
			: field(columnName, record -> value(component, record));
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
			} else if (e.getCause() instanceof Error error) {
				throw error;
			} else {
				throw new SQLNonTransientException(e.getCause());
			}
		}
	}

	/**
	 * Create a new deconstructor for the given record type.
	 *
	 * @see #dctor(Class, Function, List)
	 *
	 * @param type the record type to deconstruct
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param fields the fields which overrides/extends the
	 *        automatically extracted fields from the record
	 * @param <T> the record type
	 * @return a new deconstructor for the given record type
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if there are duplicate fields defined
	 */
	@SafeVarargs
	public static <T extends Record> Dctor<T> dctor(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName,
		final Dctor.Field<? super T>... fields
	) {
		return dctor(type, toColumnName, List.of(fields));
	}

	/**
	 * Create a new deconstructor for the given record type.
	 *
	 * @see #dctor(Class, Function, List)
	 *
	 * @param type the record type to deconstruct
	 * @param fields the fields which overrides/extends the
	 *        automatically extracted fields from the record
	 * @param <T> the record type
	 * @return a new deconstructor for the given record type
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if there are duplicate fields defined
	 */
	@SafeVarargs
	public static <T extends Record> Dctor<T> dctor(
		final Class<T> type,
		final Dctor.Field<? super T>... fields
	) {
		return dctor(type, Records::toSnakeCase, List.of(fields));
	}

	/**
	 * Converts to given record component to a column name in
	 * <a href="https://en.wikipedia.org/wiki/Snake_case">snake_case</a>. The
	 * following list shows some examples.
	 * <ul>
	 *     <li>{@code name} &rarr; {@code name}</li>
	 *     <li>{@code simpleName} &rarr; {@code simple_name}</li>
	 *     <li>{@code SimpleName} &rarr; {@code simple_name}</li>
	 *     <li>{@code Simple_Name} &rarr; {@code simple_name}</li>
	 *     <li>{@code Simple___Name} &rarr; {@code simple___name}</li>
	 * </ul>
	 *
	 * @param component the record component
	 * @return the name of the record component in <em>snake_case</em>
	 * @throws NullPointerException if the given record {@code component} is
	 *         {@code null}
	 */
	public static String toSnakeCase(final RecordComponent component) {
		return toSnakeCase(component.getName());
	}

	static String toSnakeCase(final String name) {
		final var result = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			final char ch = name.charAt(i);

			if (i == 0) {
				result.append(Character.toLowerCase(ch));
			} else {
				if (Character.isUpperCase(ch)) {
					if (name.charAt(i - 1) != '_') {
						result.append('_');
					}
					result.append(Character.toLowerCase(ch));
				} else {
					result.append(ch);
				}
			}
		}

		return result.toString();
	}

}


