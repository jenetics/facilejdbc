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
 * Helper methods for creating deconstructors from {@link Record} types.
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
	 * Factory methods for creating deconstructors.
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

		final List<Dctor.Field<? super T>> recordFields =
			Stream.of(type.getRecordComponents())
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
	 * Converts to given a record component to a column name in
	 * <a href="https://en.wikipedia.org/wiki/Snake_case">snake_case</a>.
	 *
	 * @see #toSnakeCase(String)
	 *
	 * @param component the record component
	 * @return the name of the record component in <em>snake_case</em>
	 * @throws NullPointerException if the given record {@code component} is
	 *         {@code null}
	 */
	public static String toSnakeCase(final RecordComponent component) {
		return toSnakeCase(component.getName());
	}

	/**
	 * Converts to given record component name to a column name in
	 * <a href="https://en.wikipedia.org/wiki/Snake_case">snake_case</a>. The
	 * following list shows some examples.
	 * <ul>
	 *     <li>{@code name} &rarr; {@code name}</li>
	 *     <li>{@code simpleName} &rarr; {@code simple_name}</li>
	 *     <li>{@code SimpleName} &rarr; {@code simple_name}</li>
	 *     <li>{@code Simple_Name} &rarr; {@code simple_name}</li>
	 *     <li>{@code Simple___Name} &rarr; {@code simple___name}</li>
	 *     <li>{@code IOError} &rarr; {@code io_error}</li>
	 *     <li>{@code IoError} &rarr; {@code io_error}</li>
	 *     <li>{@code UncheckedSQLException} &rarr; {@code unchecked_sql_exception}</li>
	 * </ul>
	 *
	 * @see #toSnakeCase(RecordComponent)
	 *
	 * @param name the record component name
	 * @return the name of the record component in <em>snake_case</em>
	 */
	public static String toSnakeCase(final String name) {
		if (name == null) {
			return null;
		}

		final var result = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			final char ch = name.charAt(i);

			if (i == 0) {
				result.append(Character.toLowerCase(ch));
			} else {
				if (Character.isUpperCase(ch)) {
					final var lastChar = name.charAt(i - 1);
					final var nextChar = i < name.length() - 1
						? name.charAt(i + 1) : '\0';

					if ((lastChar != '_' && !Character.isUpperCase(lastChar)) ||
						(Character.isLowerCase(nextChar) && lastChar != '_'))
					{
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

	/**
	 * Creates a {@link RowParser} for the given record {@code type}. This
	 * method gives you the greatest flexibility in creating row-parser
	 * instances.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, primary_author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parser(
	 *     Book.class,
	 *     component -> switch (component.getName()) {
	 *         case "author" -> "primary_author";
	 *         // The rest of the components are converted to snake_case.
	 *         default -> Records.toSnakeCase(component);
	 *     },
	 *     component -> switch (component.getName()) {
	 *         // Converting the ISBN string into an 'Isbn' object.
	 *         case "isbn" -> RowParser.string("isbn").map(Isbn::new);
	 *         // Returning 'null' for using the default component type.
	 *         default -> null;
	 *     }
	 * );
	 * }</pre>
	 *
	 * @param type the record type
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param fields the additional record component conversions
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
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

	/**
	 * Creates a {@link RowParser} for the given record {@code type}. This
	 * method gives you the greatest flexibility in creating row-parser
	 * instances.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, primary_author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parser(
	 *     Book.class,
	 *     Map.of("author", "primary_author"),
	 *     Map.of("isbn", RowParser.string("isbn").map(Isbn::new))
	 * );
	 * }</pre>
	 *
	 * @see #parser(Class, Function, Function)
	 *
	 * @param type the record type
	 * @param toColumnName function for mapping the record component anme to the
	 *        column names of the DB
	 * @param fields the additional record component name conversions
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T extends Record> RowParser<T> parser(
		final Class<T> type,
		final Map<? super String, String> toColumnName,
		final Map<? super String, ? extends RowParser<?>> fields
	) {
		return parser(
			type,
			component -> {
				final var name = toColumnName.get(component.getName());
				return name != null ? name : toSnakeCase(component.getName());
			},
			component -> fields.get(component.getName())
		);
	}

	/**
	 * Creates a {@link RowParser} for the given record {@code type} and an
	 * additional record-component to column name mapping.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, primary_author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parserWithColumnNames(
	 *     Book.class,
	 *     component -> switch (component.getName()) {
	 *         case "author" -> "primary_author";
	 *         // The rest of the components are converted to snake_case.
	 *         default -> Records.toSnakeCase(component);
	 *     }
	 * );
	 * }</pre>
	 *
	 * @see #parserWithColumnNames(Class, Map)
	 *
	 * @param type the record type
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T extends Record> RowParser<T> parserWithColumnNames(
		final Class<T> type,
		final Function<? super RecordComponent, String> toColumnName
	) {
		return parser(type, toColumnName, component -> null);
	}

	/**
	 * Creates a {@link RowParser} for the given record {@code type} and an
	 * additional record-component name to column name mapping.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, primary_author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parserWithColumnNames(
	 *     Book.class,
	 *     Map.of("author", "primary_author")
	 * );
	 * }</pre>
	 *
	 * @see #parserWithColumnNames(Class, Function)
	 *
	 * @param type the record type
	 * @param toColumnName function for mapping the record component to the
	 *        column names of the DB
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T extends Record> RowParser<T> parserWithColumnNames(
		final Class<T> type,
		final Map<? super String, String> toColumnName
	) {
		return parser(type, toColumnName, Map.of());
	}

	/**
	 * Creates a {@link RowParser} for the given record {@code type} and an
	 * additional record-component to column mapping.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parserWithFields(
	 *     Book.class,
	 *     component -> switch (component.getName()) {
	 *         // Converting the ISBN string into an 'Isbn' object.
	 *         case "isbn" -> RowParser.string("isbn").map(Isbn::new);
	 *         // Returning 'null' for using the default component type.
	 *         default -> null;
	 *     }
	 * );
	 * }</pre>
	 *
	 * @see #parserWithFields(Class, Map)
	 *
	 * @param type the record type
	 * @param fields the additional record component conversions
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T extends Record> RowParser<T> parserWithFields(
		final Class<T> type,
		final Function<? super RecordComponent, ? extends RowParser<?>> fields
	) {
		return parser(type, Records::toSnakeCase, fields);
	}

	/**
	 * Creates a {@link RowParser} for the given record {@code type} and an
	 * additional record-component name to column mapping.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parserWithFields(
	 *     Book.class,
	 *     Map.of("isbn", RowParser.string("isbn").map(Isbn::new))
	 * );
	 * }</pre>
	 *
	 * @see #parserWithFields(Class, Function)
	 *
	 * @param type the record type
	 * @param fields the additional record component conversions
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T extends Record> RowParser<T> parserWithFields(
		final Class<T> type,
		final Map<? super String, ? extends RowParser<?>> fields
	) {
		return parser(type, Records::toSnakeCase, cmp -> fields.get(cmp.getName()));
	}

	/**
	 * Creates a {@link RowParser} for the given record {@code type}.
	 * <pre>{@code
	 * // Handling different column names and column types:
	 * // [title, author, isbn, pages, published_at]
	 * final RowParser<Book> parser = Records.parser(Book.class);
	 * }</pre>
	 *
	 * @see RowParser#of(Class)
	 *
	 * @param type the record type
	 * @param <T> the record type
	 * @return a new row-parser for the given record {@code type}
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T extends Record> RowParser<T> parser(final Class<T> type) {
		return parser(type, Records::toSnakeCase, component -> null);
	}

}


