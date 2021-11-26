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
import static java.util.stream.Collectors.toMap;
import static io.jenetics.facilejdbc.Dctor.field;
import static io.jenetics.facilejdbc.Reflections.create;
import static io.jenetics.facilejdbc.Reflections.ctor;
import static io.jenetics.facilejdbc.Reflections.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods for creating de-constructors from {@link Record} types.
 * In the simplest case the component names of the records (converted to
 * <em>snake_case</em>) correspond to column names of the table.
 * <p>
 * Creating a {@link Dctor} from a given record type:
 * <pre>{@code
 * // The book record.
 * record Book(
 *     String title,
 *     String author,
 *     String isbn,
 *     int pages,
 *     LocalDate publishedAt
 * ){}
 *
 * // Matching column names, with book columns:
 * // [title, author, isbn, pages, published_at]
 * final Dctor<Book> dctor = Records.dctor(Book.class);
 * }</pre>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 2.0
 */
public final class Records {
	private Records() {
	}

	/* *************************************************************************
	 * Factory methods for creating de-constructors.
	 * ************************************************************************/

	/**
	 * Create a new deconstructor for the given record type. This method gives
	 * you the greatest flexibility
	 *
	 * <pre>{@code
	 * // Handling additional columns and different column names, with book columns:
	 * // [title, primary_author, isbn13, pages, published_at, title_hash]
	 * final Dctor<Book> dctor = Records.dctor(
	 *     Book.class,
	 *     // Mapping the record component names to the table column names.
	 *     component -> switch (component.getName()) {
	 *         case "author" -> "primary_author";
	 *         case "isbn" -> "isbn13";
	 *         // The rest of the components are converted to snake_case.
	 *         default -> Records.toSnakeCase(component);
	 *     },
	 *     List.of(
	 *         // Transform the record value, before writing it to the DB.
	 *         field("pages", book -> book.pages()*3),
	 *         // Define an additional column and it's value.
	 *         field("title_hash", book -> book.title().hashCode())
	 *     )
	 * );
	 * }</pre>
	 *
	 * @see #dctor(Class, Function, Dctor.Field[])
	 *
	 * @param type the record type to deconstruct
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param fields The fields which overrides/extends the
	 *        automatically extracted fields from the record. It also allows
	 *        defining additional column values, derived from the given record
	 *        values.
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
			.collect(toMap(
				Dctor.Field::name,
				f -> f,
				(a, b) -> {
					throw new IllegalArgumentException(
						"Duplicate field detected: %s".formatted(a.name())
					);
				},
				LinkedHashMap::new
			));

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

	/**
	 * Create a new deconstructor for the given record type. This method gives
	 * you the greatest flexibility
	 *
	 * <pre>{@code
	 * // Handling additional columns and different column names, with book columns:
	 * // [title, primary_author, isbn13, pages, published_at, title_hash]
	 * final Dctor<Book> dctor = Records.dctor(
	 *     Book.class,
	 *     // Mapping the record component names to the table column names.
	 *     component -> switch (component.getName()) {
	 *         case "author" -> "primary_author";
	 *         case "isbn" -> "isbn13";
	 *         // The rest of the components are converted to snake_case.
	 *         default -> Records.toSnakeCase(component);
	 *     },
	 *     // Transform the record value, before writing it to the DB.
	 *     field("pages", book -> book.pages()*3),
	 *     // Define an additional column and it's value.
	 *     field("title_hash", book -> book.title().hashCode())
	 * );
	 * }</pre>
	 *
	 * @see #dctor(Class, Function, List)
	 *
	 * @param type the record type to deconstruct
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param fields The fields which overrides/extends the
	 *        automatically extracted fields from the record. It also allows
	 *        defining additional column values, derived from the given record
	 *        values.
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
	 * <pre>{@code
	 * // Matching column names, with book columns:
	 * // [title, author, isbn, pages, published_at]
	 * final Dctor<Book> dctor = Records.dctor(Book.class);
	 *
	 * // Handling additional column, with book columns:
	 * // [title, author, isbn, pages, published_at, title_hash]
	 * final Dctor<Book> dctor = Records.dctor(
	 *     Book.class,
	 *     field("title_hash", book -> book.title().hashCode())
	 * );
	 *
	 * // Handling column "transformation", with book columns:
	 * // [title, author, isbn, pages, published_at, title_hash]
	 * final Dctor<Book> dctor = Records.dctor(
	 *     Book.class,
	 *     field("pages", book -> book.pages()*3),
	 *     field("title_hash", book -> book.title().hashCode())
	 * );
	 * }</pre>
	 *
	 * @see #dctor(Class, Function, List)
	 * @see #dctor(Class, Function, Dctor.Field[])
	 * @see Dctor#of(Class, Dctor.Field[])
	 *
	 * @param type the record type to deconstruct
	 * @param fields The fields which overrides/extends the
	 *        automatically extracted fields from the record. It also allows
	 *        defining additional column values, derived from the given record
	 *        values.
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

	/* *************************************************************************
	 * Factory methods for creating row-parsers.
	 * ************************************************************************/

	public static <T extends Record> RowParser<T> parser(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName,
		final Function<? super RecordComponent, ? extends RowParser<?>> fields
	) {
		requireNonNull(type);
		requireNonNull(toColumnName);
		requireNonNull(fields);

		final RecordComponent[] components = type.getRecordComponents();

		final String[] columnNames = Stream.of(components)
			.map(toColumnName)
			.toArray(String[]::new);

		final Constructor<T> ctor = ctor(type);

		return (row, conn) -> {
			final Object[] values = new Object[components.length];
			for (int i = 0; i < components.length; ++i) {
				final var field = fields.apply(components[i]);
				if (field != null) {
					values[i] = field.parse(row, conn);
				} else {
					values[i] = row.getObject(
						columnNames[i],
						components[i].getType()
					);
				}
			}

			return create(ctor, values);
		};
	}

	public static <T extends Record> RowParser<T> parser(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName
	) {
		return parser(type, toColumnName, component -> null);
	}

	public static <T extends Record> RowParser<T> parser(final Class<T> type) {
		return parser(type, Records::toSnakeCase, component -> null);
	}

}


