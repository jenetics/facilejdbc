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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
 * {@snippet lang="java":
 * final RowParser<Person> parser = (row, conn) -> new Person(
 *     row.getString("name"),
 *     row.getString("email"),
 *     row.getString("link")
 * );
 * }
 * <p>
 * If you are using <em>records</em> as entity objects, the creation of
 * row-parser instances is even simpler.
 * {@snippet lang="java":
 * // Handling different column names and column types:
 * // [title, author, isbn, pages, published_at]
 * final RowParser<Book> parser = RowParser.record(Book.class);
 * }
 *
 * @see ResultSetParser
 * @see Dctor
 * @see Records
 *
 * @apiNote
 * The {@code RowParser} is the counterpart of the {@link Dctor} interface. In
 * contrast, of splitting a record into a set of <em>fields</em>, it creates a
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
	 * final result. This allows combining existing row parsers.
	 * {@snippet lang="java":
	 * static final RowParser<Book> PARSER =
	 * RowParser.string("title").flatMap(title ->
	 *     RowParser.string("isbn").flatMap(isbn ->
	 *         RowParser.int32("pages").map(pages -> new Book(title, isbn, pages))
	 *     )
	 * );
	 * }
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
	 * Return a row-parser which wraps the result object into a {@link Stored}
	 * record, which contains the primary key of the parsed record. The primary
	 * key is parsed with the given {@code keyParser}.
	 *
	 * @since 2.1
	 *
	 * @param keyParser the key parser used for parsing the primary key of the
	 *        parsed records/rows
	 * @return the row parser for <em>stored</em> records
	 * @param <K> the key type
	 */
	default <K> RowParser<Stored<K, T>> stored(final RowParser<? extends K> keyParser) {
		return RowParser.compose(Stored::new, keyParser, this);
	}

	/**
	 * Return a row-parser which wraps the result object into a {@link Stored}
	 * record, which contains the primary key of the parsed record.
	 *
	 * @since 2.1
	 *
	 * @return the row parser for <em>stored</em> records
	 */
	default RowParser<Stored<Long, T>> stored(final String name) {
		return stored(int64(name));
	}

	/**
	 * Return a new parser which expects at least one result. If no result is
	 * available, a {@link NoSuchElementException} is thrown by the parser.  If
	 * more than one result is available, the first one is returned.
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
	 * Return a new parser witch parses the whole selection result.
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
	ResultSetParser<C> collection(final Supplier<? extends C> factory) {
		return collection(factory, Function.identity());
	}

	/**
	 * Return a new parser witch parses the whole selection result.
	 *
	 * @see #unmodifiableList()
	 *
	 * @return a new parser witch parses a whole selection result
	 */
	default ResultSetParser<List<T>> list() {
		return collection(ArrayList::new);
	}

	/**
	 * Return a new parser witch parses the whole selection result.
	 *
	 * @since 2.1
	 *
	 * @return a new parser witch parses a whole selection result
	 */
	default ResultSetParser<T[]> array(final Class<? extends T> type) {
		return (rs, conn) -> list().parse(rs, conn).toArray(length -> {
				@SuppressWarnings("unchecked")
				final var array = (T[])Array.newInstance(type, length);
				return array;
			});
	}

	private <C1 extends Collection<T>, C2 extends Collection<T>>
	ResultSetParser<C2> collection(
		final Supplier<? extends C1> factory,
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
	 * Return a new parser witch parses the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @see #list()
	 *
	 * @return a new parser witch parses the whole selection result, as an
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
	 * Return a new parser witch parses the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @see #unmodifiableSet()
	 *
	 * @return a new parser witch parses the whole selection result
	 */
	default ResultSetParser<Set<T>> set() {
		return collection(HashSet::new);
	}

	/**
	 * Return a new parser witch parses the whole selection result.
	 *
	 * @since 1.1
	 *
	 * @see #set()
	 *
	 * @return a new parser witch parses the whole selection result, as an
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
	 * {@snippet lang="java":
	 * final var select = Query.of("SELECT * FROM book;");
	 * try (var stream = select.as(PARSER.stream(), conn)) {
	 *     stream.forEach(book -> null); // @replace substring='null' replacement="..."
	 * }
	 * }
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
	 * {@snippet lang="java":
	 * final String name = Query.of("SELECT name FROM person WHERE id = :id")
	 *     .on(Param.value("id", 23))
	 *     .as(scalar(String.class).single(), conn);
	 * }
	 *
	 * @see #scalar(int, Class)
	 * @see #scalar(String, Class)
	 *
	 * @param type the type class of the scala
	 * @param <T> the scalar type
	 * @return a parser for a scalar not-null value
	 * @throws NullPointerException if the give {@code type} is {@code null}
	 */
	static <T> RowParser<T> scalar(final Class<? extends T> type) {
		return scalar(1, type);
	}

	/**
	 * Returns a parser for a scalar not-null value.
	 * {@snippet lang="java":
	 * final String name = Query.of("SELECT id, name FROM person WHERE id = :id")
	 *     .on(Param.value("id", 23))
	 *     .as(scalar(2, String.class).single(), conn);
	 * }
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
	static <T> RowParser<T> scalar(final int index, final Class<? extends T> type) {
		return (row, conn) -> row.getObject(index, type);
	}

	/**
	 * Returns a parser for a scalar not-null value.
	 * {@snippet lang="java":
	 * final String name = Query.of("SELECT id, name FROM person WHERE id = :id")
	 *     .on(Param.value("id", 23))
	 *     .as(scalar("name", String.class).single(), conn);
	 * }
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
	static <T> RowParser<T> scalar(final String name, final Class<? extends T> type) {
		return (row, conn) -> row.getObject(name, type);
	}

	/**
	 * Return a row parser for byte values for the given column name.
	 *
	 * @since 2.1
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Byte> int8(final String name) {
		return (row, conn) -> row.getByte(name);
	}

	/**
	 * Return a row parser for byte values for the given column index.
	 *
	 * @since 2.1
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Byte> int8(final int index) {
		return (row, conn) -> row.getByte(index);
	}

	/**
	 * Return a row parser for short values for the given column name.
	 *
	 * @since 2.0
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Short> int16(final String name) {
		return (row, conn) -> row.getShort(name);
	}

	/**
	 * Return a row parser for short values for the given column index.
	 *
	 * @since 2.0
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Short> int16(final int index) {
		return (row, conn) -> row.getShort(index);
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
	 * Return a row parser for big-decimal values for the given column name.
	 *
	 * @since 2.0
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<BigDecimal> decimal(final String name) {
		return (row, conn) -> row.getBigDecimal(name);
	}

	/**
	 * Return a row parser for big-decimal values for the given column index.
	 *
	 * @since 2.0
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<BigDecimal> decimal(final int index) {
		return (row, conn) -> row.getBigDecimal(index);
	}

	/**
	 * Return a row parser for boolean values for the given column name.
	 *
	 * @since 2.0
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Boolean> bool(final String name) {
		return (row, conn) -> row.getBoolean(name);
	}

	/**
	 * Return a row parser for boolean values for the given column index.
	 *
	 * @since 2.0
	 *
	 * @param index the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Boolean> bool(final int index) {
		return (row, conn) -> row.getBoolean(index);
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
	 * Return a row parser for instant (timestamp) values for the given column
	 * name.
	 *
	 * @since 1.3
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<Instant> instant(final String name) {
		return timestamp(name).map(ts -> ts != null ? ts.toInstant() : null);
	}

	/**
	 * Return a row parser for instant (timestamp) values for the given column
	 * index.
	 *
	 * @since 1.3
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<Instant> instant(final int index) {
		return timestamp(index).map(ts -> ts != null ? ts.toInstant() : null);
	}

	/**
	 * Return a row parser for local date (date) values for the given column
	 * name.
	 *
	 * @since 2.1
	 *
	 * @param name the column name
	 * @return the row-parser for the given column
	 */
	static RowParser<LocalDate> date(final String name) {
		return (row, conn) -> {
			final var date = row.getDate(name);
			return date != null ? date.toLocalDate() : null;
		};
	}

	/**
	 * Return a row parser for local date (date) values for the given column
	 * index.
	 *
	 * @since 2.1
	 *
	 * @param index the column index
	 * @return the row-parser for the given column
	 */
	static RowParser<LocalDate> date(final int index) {
		return (row, conn) -> {
			final var date = row.getDate(index);
			return date != null ? date.toLocalDate() : null;
		};
	}

	/**
	 * Return a row parser which converts a DB row into a CSV row. This parser
	 * can be used for exporting a huge amount of data into a file. The
	 * following example shows how to stream a DB result into a file.
	 * {@snippet lang="java":
	 * final var select = Query.of("SELECT * FROM book ORDER BY id;");
	 * try (var lines = select.as(RowParser.csvLine().stream(), conn);
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
	 * }
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
	 * @see ResultSetParser#csvLine()
	 * @see #ofColumns(Function)
	 *
	 * @return a row parser which converts a DB row into a CSV row
	 */
	static RowParser<String> csvLine() {
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

	/**
	 * Returns a parser for the given record {@code type}.
	 * {@snippet lang="java":
	 * final Book book = Query.of("SELECT * FROM book WHERE id = :id")
	 *     .on(value("id", 23))
	 *     .as(record(Book.class).singleNull(), conn);
	 * }
	 *
	 * @since 2.1
	 *
	 * @param type the record type
	 * @return the record type parser
	 * @param <T> the record type
	 * @throws NullPointerException if the give {@code type} is {@code null}
	 */
	static <T extends Record> RowParser<T> record(final Class<? extends T> type) {
		return Records.parser(type);
	}

}
