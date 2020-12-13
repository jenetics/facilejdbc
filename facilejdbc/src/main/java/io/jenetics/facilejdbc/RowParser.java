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
import static java.util.Spliterators.spliteratorUnknownSize;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 * @version 1.3
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
	T parse(final Row row, final Connection conn) throws SQLException;


	/* *************************************************************************
	 * Default methods.
	 * ************************************************************************/

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
	default <U> RowParser<U>
	map(final SqlFunction<? super T, ? extends U> mapper) {
		return (row, conn) -> mapper.apply(parse(row, conn));
	}

	/**
	 * Returns a parser that will apply the given {@code mapper} to the result of
	 * {@code this} first parser. If the current parser is not successful, the
	 * new one will return encountered exception.
	 *
	 * @see #map(SqlFunction)
	 *
	 * @param mapper the mapping function to apply to the parsing result
	 * @param <U> the type of the value returned from the mapping function
	 * @return a new row parser with the mapped type
	 */
	default <U> RowParser<U>
	map(final SqlFunction2<? super T, ? super Connection, ? extends U> mapper) {
		return (row, conn) -> mapper.apply(parse(row, conn), conn);
	}

	/**
	 * Returns a parser that will apply the given {@code mapper} to the result
	 * of {@code this} first parser, which will then be used for parsing the
	 * final result. This allows to combine existing row parsers.
	 *
	 * <pre>{@code
	 * static final RowParser<Book> PARSER =
	 * RowParser.string("title").flatMap(title ->
	 *     RowParser.string("isbn").flatMap(isbn ->
	 *         RowParser.int32("pages").map(pages -> new Book(title, isbn, pages))
	 *     )
	 * );
	 * }</pre>
	 *
	 * @since 1.3
	 *
	 * @param mapper the mapping function
	 * @param <U> the type of the value returned from the mapping function
	 * @return the new row parser with the flat-mapped types
	 */
	default <U> RowParser<U>
	flatMap(final SqlFunction<? super T, ? extends RowParser<? extends U>> mapper) {
		return (row, conn) -> mapper.apply(parse(row, conn)).parse(row, conn);
	}

	/**
	 * Return a new parser which expects at least one result. If no result is
	 * available, a {@link NoSuchElementException} is thrown by the parser.  If
	 * more then one result is available, the first one is returned.
	 *
	 * @see #singleNull()
	 * @see #singleOpt()
	 *
	 * @return a new parser which expects at least one result
	 */
	default ResultSetParser<T> single() {
		return (rs, conn) -> {
			if (rs.next()) {
				return parse(ResultSetRow.of(rs), conn);
			}
			throw new NoSuchElementException();
		};
	}

	/**
	 * Return a new parser which parses a single selection result. If no result
	 * is available, {@code null} is returned by the parse.
	 *
	 * @see #single()
	 * @see #singleOpt()
	 *
	 * @return a new parser which parses a single selection result or
	 *         {@code null} if not available
	 */
	default ResultSetParser<T> singleNull() {
		return (rs, conn) -> rs.next()
			? parse(ResultSetRow.of(rs), conn)
			: null;
	}

	/**
	 * Return a new parser which parses a single selection result. If no result
	 * is available, {@link Optional#empty()} is returned by the parse.
	 *
	 * @see #single()
	 * @see #singleNull()
	 *
	 * @return a new parser which parses a single selection result or
	 *         {@link Optional#empty()} if not available
	 */
	default ResultSetParser<Optional<T>> singleOpt() {
		return (rs, conn) -> rs.next()
			? Optional.ofNullable(parse(ResultSetRow.of(rs), conn))
			: Optional.empty();
	}

	/**
	 * Return a new parser witch parses a the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @param factory a supplier providing a new empty {@code Collection} into
	 *        which the results will be inserted
	 * @param <C> the type of the resulting {@code Collection}
	 * @return which collects all the input elements into a {@code Collection},
	 *         in encounter order
	 * @throws NullPointerException if the given {@code factory} is {@code null}
	 */
	default  <C extends Collection<T>>
	ResultSetParser<C> collection(final Supplier<C> factory) {
		return collection(factory, Function.identity());
	}

	/**
	 * Return a new parser witch parses a the whole selection result.
	 *
	 * @see #unmodifiableList()
	 *
	 * @return a new parser witch parses a the whole selection result
	 */
	default ResultSetParser<List<T>> list() {
		return collection(ArrayList::new);
	}

	private <C1 extends Collection<T>, C2 extends Collection<T>>
	ResultSetParser<C2> collection(
		final Supplier<C1> factory,
		final Function<? super C1, ? extends C2> mapper
	) {
		requireNonNull(factory);
		requireNonNull(mapper);

		return (rs, conn) -> {
			final var row = ResultSetRow.of(rs);
			final var result = factory.get();
			while (rs.next()) {
				result.add(parse(row, conn));
			}

			return mapper.apply(result);
		};
	}

	/**
	 * Return a new parser witch parses a the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @see #list()
	 *
	 * @return a new parser witch parses a the whole selection result, as an
	 *         unmodifiable list
	 */
	default ResultSetParser<List<T>> unmodifiableList() {
		return collection(
			ArrayList::new,
			list -> {
				@SuppressWarnings("unchecked")
				final List<T> unmodifiable = (List<T>)List.of(list.toArray());
				return unmodifiable;
			}
		);
	}

	/**
	 * Return a new parser witch parses a the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @see #unmodifiableSet()
	 *
	 * @return a new parser witch parses a the whole selection result
	 */
	default ResultSetParser<Set<T>> set() {
		return collection(HashSet::new);
	}

	/**
	 * Return a new parser witch parses a the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @see #set()
	 *
	 * @return a new parser witch parses a the whole selection result, as an
	 *         unmodifiable list
	 */
	default ResultSetParser<Set<T>> unmodifiableSet() {
		return collection(
			HashSet::new,
			set -> {
				@SuppressWarnings("unchecked")
				final Set<T> unmodifiable = (Set<T>)Set.of(set.toArray());
				return unmodifiable;
			}
		);
	}

	/**
	 * Return a new parser witch <em>lazily</em> parses the selection result.
	 * It is the responsibility of the caller to close the created stream. This
	 * closes the underlying {@link ResultSet} and {@link java.sql.Statement}.
	 * While consuming the result {@link Stream}, possible {@link SQLException}s
	 * are wrapped into {@link UncheckedSQLException}s.
	 *
	 * <pre>{@code
	 * final var select = Query.of("SELECT * FROM book;");
	 * try (var stream = select.as(PARSER.stream(), conn)) {
	 *     stream.forEach(book -> ...);
	 * }
	 * }</pre>
	 *
	 * @see UncheckedSQLException
	 *
	 * @since 1.3
	 *
	 * @return a new parser witch <em>lazily</em> parses the selection result
	 */
	default ResultSetParser<Stream<T>> stream() {
		return (rs, conn) -> {
			final var spliterator = spliteratorUnknownSize(
				new RowIterator(rs),
				Spliterator.ORDERED
			);

			return StreamSupport.stream(spliterator, false)
				.map(r -> {
					try {
						return parse(r, conn);
					} catch (SQLException e) {
						throw new UncheckedSQLException(e);
					}
				});
		};
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Creates a new {@link RowParser} from the given {@code fields}, given as
	 * {@code RowParser}s, and the composition function, {@code composer}.
	 *
	 * @see #compose(BiFunction, RowParser, RowParser)
	 *
	 * @since 1.3
	 *
	 * @param composer the composition function, which creates an object of type
	 *        {@code T}, from the given {@code fields}
	 * @param fields the fields from where to create the object of type {@code T}
	 * @param <T> the created object type
	 * @return a new {@code RowParser}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <T> RowParser<T> compose(
		final Function<? super Object[], ? extends T> composer,
		final RowParser<?>... fields
	) {
		requireNonNull(composer);
		requireNonNull(fields);

		return (row, conn) -> {
			final var params = new Object[fields.length];
			for (int i = 0; i < fields.length; ++i) {
				params[i] = fields[i].parse(row, conn);
			}
			return composer.apply(params);
		};
	}

	/**
	 * Creates a new {@link RowParser} from the given {@code fields}, given as
	 * {@code RowParser}s, and the composition function, {@code composer}.
	 *
	 * @see #compose(Function, RowParser[])
	 *
	 * @since 1.3
	 *
	 * @param composer the composition function, which creates an object of type
	 *        {@code T}, from the given fields
	 * @param field1 the first field
	 * @param field2 the second field
	 * @param <A> the type of the first field
	 * @param <B> the type of the second field
	 * @param <T> the type of the created object
	 * @return a new {@code RowParser}
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	static <A, B, T> RowParser<T> compose(
		final BiFunction<? super A, ? super B, ? extends T> composer,
		final RowParser<? extends A> field1,
		final RowParser<? extends B> field2
	) {
		requireNonNull(composer);
		requireNonNull(field1);
		requireNonNull(field2);

		return (row, conn) -> composer.apply(
			field1.parse(row, conn),
			field2.parse(row, conn)
		);
	}

	/**
	 * Returns a parser for a scalar not-null value.
	 *
	 * <pre>{@code
	 * final String name = Query.of("SELECT name FROM person WHERE id = :id")
	 *     .on(value("id", 23))
	 *     .as(scalar(String.class).single(), conn);
	 * }</pre>
	 *
	 * @see #scalar(int, Class)
	 * @see #scalar(String, Class)
	 *
	 * @param type the type class of the scala
	 * @param <T> the scalar type
	 * @return a parser for a scalar not-null value
	 * @throws NullPointerException if the give {@code type} is {@code null}
	 */
	static <T> RowParser<T> scalar(final Class<T> type) {
		return scalar(1, type);
	}

	/**
	 * Returns a parser for a scalar not-null value.
	 *
	 * <pre>{@code
	 * final String name = Query.of("SELECT id, name FROM person WHERE id = :id")
	 *     .on(value("id", 23))
	 *     .as(scalar(2, String.class).single(), conn);
	 * }</pre>
	 *
	 * @since 1.3
	 *
	 * @see #scalar(Class)
	 * @see #scalar(String, Class)
	 *
	 * @param index the column index
	 * @param type the type class of the scala
	 * @param <T> the scalar type
	 * @return a parser for a scalar not-null value
	 * @throws NullPointerException if the give {@code type} is {@code null}
	 */
	static <T> RowParser<T> scalar(final int index, final Class<T> type) {
		return (row, conn) -> row.getObject(index, type);
	}

	/**
	 * Returns a parser for a scalar not-null value.
	 *
	 * <pre>{@code
	 * final String name = Query.of("SELECT id, name FROM person WHERE id = :id")
	 *     .on(value("id", 23))
	 *     .as(scalar("name", String.class).single(), conn);
	 * }</pre>
	 *
	 * @since 1.3
	 *
	 * @see #scalar(Class)
	 * @see #scalar(int, Class)
	 *
	 * @param name the column name
	 * @param type the type class of the scala
	 * @param <T> the scalar type
	 * @return a parser for a scalar not-null value
	 * @throws NullPointerException if the give {@code type} is {@code null}
	 */
	static <T> RowParser<T> scalar(final String name, final Class<T> type) {
		return (row, conn) -> row.getObject(name, type);
	}

	/**
	 * Return a row parser for int values for the given column name.
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Integer> int32(final String name) {
		return (row, conn) -> row.getInt(name);
	}

	/**
	 * Return a row parser for int values for the given column index.
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Integer> int32(final int index) {
		return (row, conn) -> row.getInt(index);
	}

	/**
	 * Return a row parser for long values for the given column name.
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Long> int64(final String name) {
		return (row, conn) -> row.getLong(name);
	}

	/**
	 * Return a row parser for long values for the given column index.
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Long> int64(final int index) {
		return (row, conn) -> row.getLong(index);
	}

	/**
	 * Return a row parser for float values for the given column name.
	 *
	 * @since 1.3
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Float> float32(final String name) {
		return (row, conn) -> row.getFloat(name);
	}

	/**
	 * Return a row parser for float values for the given column index.
	 *
	 * @since 1.3
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Float> float32(final int index) {
		return (row, conn) -> row.getFloat(index);
	}

	/**
	 * Return a row parser for double values for the given column name.
	 *
	 * @since 1.3
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Double> float64(final String name) {
		return (row, conn) -> row.getDouble(name);
	}

	/**
	 * Return a row parser for double values for the given column index.
	 *
	 * @since 1.3
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Double> float64(final int index) {
		return (row, conn) -> row.getDouble(index);
	}

	/**
	 * Return a row parser for long values for the given column name.
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<String> string(final String name) {
		return (row, conn) -> row.getString(name);
	}

	/**
	 * Return a row parser for string values for the given column index.
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<String> string(final int index) {
		return (row, conn) -> row.getString(index);
	}

	/**
	 * Return a row parser for timestamp values for the given column name.
	 *
	 * @since 1.3
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Timestamp> timestamp(final String name) {
		return (row, conn) -> row.getTimestamp(name);
	}

	/**
	 * Return a row parser for timestamp values for the given column index.
	 *
	 * @since 1.3
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Timestamp> timestamp(final int index) {
		return (row, conn) -> row.getTimestamp(index);
	}

	/**
	 * Return a row parser for instant values for the given column name.
	 *
	 * @since 1.3
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Instant> instant(final String name) {
		return timestamp(name).map(Timestamp::toInstant);
	}

	/**
	 * Return a row parser for instant values for the given column index.
	 *
	 * @since 1.3
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Instant> instant(final int index) {
		return timestamp(index).map(Timestamp::toInstant);
	}

	/**
	 * Return a row parser which converts a DB row into a CSV row. This parser
	 * can be used for exporting a huge amount of data into a file. The
	 * following example shows how to stream a DB result into a file.
	 *
	 * <pre>{@code
	 * final var select = Query.of("SELECT * FROM book ORDER BY id;");
	 * try (var lines = select.as(RowParser.csv().stream(), conn);
	 *     var out = Files.newBufferedWriter(Path.of("out.csv")))
	 * {
	 *     lines.forEach(line -> {
	 *         try {
	 *             out.write(line);
	 *             out.write("\r\n");
	 *         } catch (IOException e) {
	 *             throw new UncheckedIOException(e);
	 *         }
	 *     });
	 * }
	 * }</pre>
	 *
	 * The rows are written without a CSV header and will look like this:
	 * <pre>
	 * "0","1987-02-04","Auf der Suche nach der verlorenen Zeit","978-3518061756","5100"
	 * "1","1945-01-04","Database Design for Mere Mortals","978-0321884497","654"
	 * "2","1887-02-04","Der alte Mann und das Meer","B00JM4RD2S","142"
	 * </pre>
	 *
	 * @since 1.3
	 *
	 * @see ResultSetParser#csv()
	 * @see #ofColumns(Function)
	 *
	 * @return a row parser which converts a DB row into a CSV row
	 */
	static RowParser<String> csv() {
		return ofColumns(CSV::join);
	}

	/**
	 * Return a row parser which converts the columns of one row into an object.
	 * The columns are given as {@code Object[]} array and are arranged in the
	 * order as defined in the <em>SELECT</em> query.
	 *
	 * @since 1.3
	 *
	 * @param <T> the type of the constructed row object
	 * @param ctor the function used for combining the column values to one
	 *        value
	 * @return a row parser which combines the row values into one object
	 */
	static <T> RowParser<T>
	ofColumns(final Function<? super Object[], ? extends T> ctor) {
		return (row, conn) -> {
			final var md = row.getMetaData();
			final var cols = new Object[md.getColumnCount()];
			for (int i = 0; i < cols.length; ++i) {
				cols[i] = row.getObject(i + 1);
			}
			return ctor.apply(cols);
		};
	}

	static <T> RowParser<T> of(final Ctor<? extends T> ctor) {
		return (row, conn) -> {
			final var md = row.getMetaData();
			final var fields = new Ctor.Field[md.getColumnCount()];

			for (int i = 1; i <= fields.length; ++i) {
				final var field = new Ctor.Field(
					md.getColumnLabel(i),
					row.getObject(i)
				);
				fields[i - 1] = field;
			}

			return ctor.apply(fields);
		};
	}

	static <T extends Record> RowParser<T> of(final Class<T> type) {
		return of(Ctor.of(type));
	}

}

class Main {

	final record Foo(String name, int count, long max){}

	static void foo() throws Exception {

		final RowParser<Foo> parser = RowParser.compose(
			params -> new Foo(
				(String)params[0],
				(int)params[1],
				(long)params[2]
			),
			RowParser.string("name"),
			RowParser.int32("count"),
			RowParser.int64("max")
		);
	}
}
