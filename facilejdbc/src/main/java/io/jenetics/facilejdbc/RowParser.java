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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.jenetics.facilejdbc.function.SqlFunction;
import io.jenetics.facilejdbc.function.SqlFunction2;

/**
 * Converts one row from the given {@link ResultSet} into a data object from
 * the given type.
 *
 * <pre>{@code
 * final RowParser<Person> parser = (row, conn) -> new Person(
 *     row.getString("name"),
 *     row.getString("email"),
 *     row.getString("link")
 * );
 * }</pre>
 *
 * @see ResultSetParser
 * @see Dctor
 *
 * @apiNote
 * The {@code RowParser} is the counterpart of the {@link Dctor} interface. In
 * contrast of splitting a record into a set of <em>fields</em>, it creates a
 * record from a selected DB row.
 *
 * @param <T> the row type
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface RowParser<T> {

	/**
	 * Converts the row on the current cursor position into a data object.
	 *
	 * @param row the data source
	 * @param conn the connection used for producing the record, if needed
	 * @return the stored data object
	 * @throws SQLException if reading of the current row fails
	 */
	public T parse(final Row row, final Connection conn) throws SQLException;

	/**
	 * Returns a parser that will apply given {@code mapper} to the result of
	 * {@code this} first parser. If the current parser is not successful, the
	 * new one will return encountered exception.
	 *
	 * @see #map(SqlFunction2)
	 *
	 * @param mapper the mapping function to apply to the parsing result
	 * @param <U> the type of the value returned from the mapping function
	 * @return a new row parser with the mapped type
	 */
	public default <U> RowParser<U>
	map(final SqlFunction<? super T, ? extends U> mapper) {
		return (row, conn) -> mapper.apply(parse(row, conn));
	}

	/**
	 * Returns a parser that will apply given {@code mapper} to the result of
	 * {@code this} first parser. If the current parser is not successful, the
	 * new one will return encountered exception.
	 *
	 * @see #map(SqlFunction)
	 *
	 * @param mapper the mapping function to apply to the parsing result
	 * @param <U> the type of the value returned from the mapping function
	 * @return a new row parser with the mapped type
	 */
	public default <U> RowParser<U>
	map(final SqlFunction2<? super T, ? super Connection, ? extends U> mapper) {
		return (row, conn) -> mapper.apply(parse(row, conn), conn);
	}

	/**
	 * Return a new parser which expects at least one result.
	 *
	 * @return a new parser which expects at least one result
	 */
	public default ResultSetParser<T> single() {
		return (rs, conn) -> {
			if (rs.next()) {
				return parse(ResultSetRow.of(rs), conn);
			}
			throw new NoSuchElementException();
		};
	}

	/**
	 * Return a new parser which parses a single selection result.
	 *
	 * @return a new parser which parses a single selection result or
	 *         {@code null} if not available
	 */
	public default ResultSetParser<T> singleNullable() {
		return (rs, conn) -> rs.next()
			? parse(ResultSetRow.of(rs), conn)
			: null;
	}

	/**
	 * Return a new parser which parses a single selection result.
	 *
	 * @return a new parser which parses a single selection result or
	 *         {@link Optional#empty()} if not available
	 */
	public default ResultSetParser<Optional<T>> singleOpt() {
		return (rs, conn) -> rs.next()
			? Optional.ofNullable(parse(ResultSetRow.of(rs), conn))
			: Optional.empty();
	}

	/**
	 * Return a new parser witch parses a the whole selection result.
	 *
	 * @return a new parser witch parses a the whole selection result
	 */
	public default ResultSetParser<List<T>> list() {
		return (rs, conn) -> {
			final ResultSetRow row = ResultSetRow.of(rs);
			final List<T> result = new ArrayList<>();
			while (rs.next()) {
				result.add(parse(row, conn));
			}

			return result;
		};
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Returns a parser for a scalar not-null value.
	 *
	 * <pre>{@code
	 * final String name = Query.of("SELECT name FROM person WHERE id = :id")
	 *     .on(value("id", 23))
	 *     .as(scalar(String.class).single(), conn);
	 * }</pre>
	 *
	 * @param type the type class of the scala
	 * @param <T> the scalar type
	 * @return a parser for a scalar not-null value
	 * @throws NullPointerException if the give {@code type} is {@code null}
	 */
	public static <T> RowParser<T> scalar(final Class<T> type) {
		return (row, conn) -> row.getObject(1, type);
	}

	/**
	 * Return a row parser for long values for the given column name.
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	public static RowParser<Long> int64(final String name) {
		return (row, conn) -> row.getLong(name);
	}

	/**
	 * Return a row parser for long values for the given column index.
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	public static RowParser<Long> int64(final int index) {
		return (row, conn) -> row.getLong(index);
	}

	/**
	 * Return a row parser for int values for the given column name.
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	public static RowParser<Integer> int32(final String name) {
		return (row, conn) -> row.getInt(name);
	}

	/**
	 * Return a row parser for int values for the given column index.
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	public static RowParser<Integer> int32(final int index) {
		return (row, conn) -> row.getInt(index);
	}

	/**
	 * Return a row parser for long values for the given column name.
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	public static RowParser<String> string(final String name) {
		return (row, conn) -> row.getString(name);
	}

	/**
	 * Return a row parser for string values for the given column index.
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	public static RowParser<String> string(final int index) {
		return (row, conn) -> row.getString(index);
	}

}
