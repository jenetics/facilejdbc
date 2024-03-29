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
import static java.util.stream.Collectors.toMap;
import static io.jenetics.facilejdbc.spi.SqlTypeMapper.map;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.jenetics.facilejdbc.function.SqlFunction;
import io.jenetics.facilejdbc.function.SqlFunction2;

/**
 * This interface is responsible for <em>deconstructing</em> a given record, of
 * type {@code T}, to a DB-<em>row</em> ({@link ParamValues}).
 *
 * <pre>{@code
 * final Dctor<Book> dctor = Dctor.of(
 *     Dctor.field("title", Book::title),
 *     Dctor.field("isbn", Book::isbn),
 *     Dctor.field("published_at", Book::publishedAt),
 *     Dctor.field("pages", Book::pages)
 * );
 * }</pre>
 *
 * If the {@code Book} class is a record, you can just write
 * <pre>{@code
 * final Dctor<Book> dctor = Dctor.of(Book.class);
 * }</pre>
 *
 * @apiNote
 * A {@code Dctor} (deconstructor) is responsible for splitting a given record
 * into a set of fields (columns), which can be written into the DB. The
 * counterpart of this interface is the {@link RowParser}, which builds a
 * record of a DB result row.
 *
 * @see RowParser
 * @see Records
 *
 * @param <T> the record type to be deconstructed
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 1.0
 */
@FunctionalInterface
public interface Dctor<T> {

	/**
	 * Represents a <em>deconstructed</em> record field.
	 *
	 * @param <T> the record type
	 */
	interface Field<T> {

		/**
		 * Return the <em>column</em> name of the record field.
		 *
		 * @return the field name
		 */
		String name();

		/**
		 * Return the SQL value for the give {@code row} field.
		 *
		 * @param record the actual record
		 * @param conn the connection used for producing the SQL value, if needed
		 * @return the SQL value for the give {@code row} field
		 */
		ParamValue value(final T record, final Connection conn);

		/**
		 * Create a new record field with the given {@code name} and field
		 * {@code accessor}.
		 *
		 * @param name the field name
		 * @param value the field creation function
		 * @param <T> the record type
		 * @return a new record field
		 * @throws NullPointerException if one of the arguments is {@code null}
		 */
		static <T> Field<T> of(
			final String name,
			final BiFunction<? super T, ? super Connection, ? extends ParamValue> value
		) {
			requireNonNull(name);
			requireNonNull(value);

			return new Field<>() {
				@Override
				public String name() {
					return name;
				}
				@Override
				public ParamValue value(final T record, final Connection conn) {
					return value.apply(record, conn);
				}
				@Override
				public String toString() {
					return format("Field[%s]", name);
				}
			};
		}

	}


	/**
	 * Return the SQL parameter values for the given {@code record}.
	 *
	 * @param record the deconstructed record
	 * @param conn the DB connection used for record deconstruction, if needed
	 * @return a new row preparer
	 */
	ParamValues unapply(final T record, final Connection conn);


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new deconstructor from the given field definitions.
	 *
	 * @see #of(Field[])
	 *
	 * @param fields the fields which describe the deconstruction
	 * @param <T> the type of the record to be deconstructed
	 * @return a new deconstructor from the given field definitions
	 * @throws NullPointerException if the given {@code fields} are {@code null}
	 * @throws IllegalArgumentException if there are duplicate fields
	 */
	static <T> Dctor<T> of(final List<? extends Field<? super T>> fields) {
		final Map<String, Field<? super T>> fieldMap = fields.stream()
			.collect(toMap(
				Field::name,
				f -> f,
				(a, b) -> {
					throw new IllegalArgumentException(
						"Duplicate field detected: %s".formatted(a.name())
					);
				}
			));

		return (record, conn) -> (params, stmt) -> {
			if (!fieldMap.isEmpty()) {
				int index = 0;
				for (String name : params) {
					++index;
					final Field<? super T> field = fieldMap.get(name);
					if (field != null) {
						field.value(record, conn).set(index, stmt);
					}
				}
			}
		};
	}

	/**
	 * Create a new deconstructor from the given field definitions.
	 *
	 * @see #of(List)
	 *
	 * @param fields the fields which describe the deconstruction
	 * @param <T> the type of the record to be deconstructed
	 * @return a new deconstructor from the given field definitions
	 * @throws NullPointerException if the given {@code fields} are {@code null}
	 * @throws IllegalArgumentException if there are duplicate fields
	 */
	@SafeVarargs
	static <T> Dctor<T> of(final Field<? super T>... fields) {
		final List<Field<? super T>> list = asList(fields);
		return Dctor.of(list);
	}

	/**
	 * Create a new deconstructor for the given record type.
	 *
	 * <pre>{@code
	 * // Matching column names, with book columns:
	 * // [title, author, isbn, pages, published_at]
	 * final Dctor<Book> dctor = Dctor.of(Book.class);
	 *
	 * // Handling additional column, with book columns:
	 * // [title, author, isbn, pages, published_at, title_hash]
	 * final Dctor<Book> dctor = Dctor.of(
	 *     Book.class,
	 *     field("title_hash", book -> book.title().hashCode())
	 * );
	 *
	 * // Handling column "transformation", with book columns:
	 * // [title, author, isbn, pages, published_at, title_hash]
	 * final Dctor<Book> dctor = Dctor.of(
	 *     Book.class,
	 *     field("pages", book -> book.pages()*3),
	 *     field("title_hash", book -> book.title().hashCode())
	 * );
	 * }</pre>
	 *
	 * @see Records#dctor(Class, Field[])
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
	static <T extends Record> Dctor<T> of(
		final Class<T> type,
		final Dctor.Field<? super T>... fields
	) {
		return Records.dctor(type, fields);
	}

	/**
	 * Create a new record field with the given {@code name} and field
	 * {@code accessor}. The given {@code mapper} function allows a
	 * transformation to the proper SQL type before inserting the value into
	 * the DB.
	 *
	 * @since 1.2
	 *
	 * @param name the field name
	 * @param value the field accessor
	 * @param mapper the additional mapper function
	 * @param <T> the record type
	 * @param <U> the native field type
	 * @param <V> the mapped field type
	 * @return a new record field
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T, U, V> Field<T> field(
		final String name,
		final SqlFunction2<? super T, ? super Connection, ? extends U> value,
		final Function<? super U, ? extends V> mapper
	) {
		requireNonNull(name);
		requireNonNull(value);
		requireNonNull(mapper);

		return Field.of(
			name,
			(record, conn) -> (index, stmt) -> stmt.setObject(
				index,
				map(mapper.apply(value.apply(record, conn)))
			)
		);
	}

	/**
	 * Create a new record field with the given {@code name} and field
	 * {@code accessor}.
	 *
	 * @see #field(String, SqlFunction)
	 *
	 * @param name the field name
	 * @param value the field accessor
	 * @param <T> the record type
	 * @return a new record field
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T> Field<T> field(
		final String name,
		final SqlFunction2<? super T, ? super Connection, ?> value
	) {
		return field(name, value, a -> a);
	}

	/**
	 * Create a new record field with the given {@code name} and field
	 * {@code accessor}. The given {@code mapper} function allows a
	 * transformation to the proper SQL type before inserting the value into
	 * the DB.
	 *
	 * @since 1.2
	 *
	 * @param name the field name
	 * @param value the field accessor
	 * @param mapper the additional mapper function
	 * @param <T> the record type
	 * @param <U> the native field type
	 * @param <V> the mapped field type
	 * @return a new record field
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T, U, V> Field<T> field(
		final String name,
		final SqlFunction<? super T, ? extends U> value,
		final Function<? super U, ? extends V> mapper
	) {
		return field(name, (record, conn) -> value.apply(record), mapper);
	}

	/**
	 * Create a new record field with the given {@code name} and field
	 * {@code accessor}.
	 *
	 * @see #field(String, SqlFunction2)
	 *
	 * @param name the field name
	 * @param value the field accessor
	 * @param <T> the record type
	 * @return a new record field
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T> Field<T> field(
		final String name,
		final SqlFunction<? super T, ?> value
	) {
		return field(name, value, a -> a);
	}

	/**
	 * Create a new record field with the given {@code name} and field value.
	 *
	 * @see #field(String, SqlFunction2)
	 *
	 * @param name the field name
	 * @param value the field value
	 * @param <T> the record type
	 * @return a new record field
	 * @throws NullPointerException if the {@code name} is {@code null}
	 */
	static <T> Field<T> fieldValue(final String name, final Object value) {
		return field(name, (record, conn) -> value);
	}

}
