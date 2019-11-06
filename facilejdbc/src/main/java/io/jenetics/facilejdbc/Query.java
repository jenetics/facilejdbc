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

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@code Query} represents an executable piece of SQL text.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public class Query {

	private final Sql _sql;
	private final ParamValues _values;

	Query(final Sql sql, final ParamValues values) {
		_sql = requireNonNull(sql);
		_values = requireNonNull(values);
	}

	/**
	 * Return the SQL string of {@code this} query class.
	 *
	 * @return the SQL string of {@code this} query class
	 */
	Sql sql() {
		return _sql;
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 */
	public Query on(final Param... params) {
		return on(asList(params));
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 */
	public Query on(final List<Param> params) {
		final Query query;
		if (params.isEmpty()) {
			query = this;
		} else {
			final ParamValues values = new Params(params);
			query = new Query(_sql, _values.andThen(values));
		}

		return query;
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 */
	public Query on(final Map<String, ?> params) {
		return on(
			params.entrySet().stream()
				.map(e -> Param.value(e.getKey(), e.getValue()))
				.collect(Collectors.toList())
		);
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * @param params the query parameters
	 * @param dctor the deconstructor used to <em>split</em> the parameters
	 * @param <T> the parameter record type
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public <T> Query on(final T params, final Dctor<T> dctor) {
		requireNonNull(params);
		requireNonNull(dctor);

		final ParamValues values = (stmt, indexes) -> dctor
			.apply(params, stmt.getConnection())
			.set(stmt, indexes);

		return new Query(_sql, _values.andThen(values));
	}

	/**
	 * Executes {@code this} query and parses the result with the given
	 * result-set parser.
	 *
	 * @param parser the parser which converts the query result to the desired
	 *        type
	 * @param conn the DB connection where {@code this} query is executed on
	 * @param <T> the result type
	 * @return the query result, parsed to the desired type
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given result parser or connection is
	 *         {@code null}
	 */
	public <T> T as(final ResultSetParser<T> parser, final Connection conn)
		throws SQLException
	{
		try (PreparedStatement stmt = statement(conn)) {
			_values.set(stmt, _sql.paramIndexes());

			try (ResultSet rs = stmt.executeQuery()) {
				return parser.parse(rs);
			}
		}
	}

	/**
	 * Executes the SQL statement defined by {@code this} query object, which
	 * may be any kind of SQL statement.
	 *
	 * @see PreparedStatement#execute()
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return {@code true} if the first result is a {@link java.sql.ResultSet}
	 *         object; {@code false} if the first result is an update count or
	 *         there is no result
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given connection is {@code null}
	 */
	public boolean execute(final Connection conn) throws SQLException  {
		try (PreparedStatement stmt = statement(conn)) {
			_values.set(stmt, _sql.paramIndexes());
			return stmt.execute();
		}
	}

	PreparedStatement statement(final Connection conn) throws SQLException {
		return conn.prepareStatement(_sql.string(), RETURN_GENERATED_KEYS);
	}

	/**
	 * Executes the given {@code batch} for this query.
	 *
	 * @param batch the batch to execute
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return 1
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public int execute(
		final Batch<?> batch,
		final Connection conn
	)
		throws SQLException
	{
		try (PreparedStatement stmt = statement(conn)) {
			_values.set(stmt, _sql.paramIndexes());

			for (var preparer : batch) {
				preparer.apply(conn).set(stmt, _sql.paramIndexes());
				stmt.executeUpdate();
			}
		}
		return 0;
	}

	private static Optional<Long> readID(final Statement stmt)
		throws SQLException
	{
		try (ResultSet keys = stmt.getGeneratedKeys()) {
			if (keys.next()) {
				return Optional.of(keys.getLong(1));
			} else {
				return Optional.empty();
			}
		}
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	public static Query of(final String sql) {
		return new Query(Sql.of(sql), ParamValues.EMPTY);
	}

}




//	/**
//	 * Executes the SQL statement defined by {@code this} query object, which
//	 * must be an SQL Data Manipulation Language (DML) statement, such as
//	 * {@code INSERT}, {@code UPDATE} or {@code DELETE}; or an SQL statement
//	 * that returns nothing, such as a DDL statement.
//	 *
//	 * @see PreparedStatement#executeUpdate()
//	 *
//	 * @param conn the DB connection where {@code this} query is executed on
//	 * @return either (1) the row count for SQL Data Manipulation Language (DML)
//	 *         statements or (2) 0 for SQL statements that return nothing
//	 * @throws SQLException if a database access error occurs
//	 * @throws java.sql.SQLTimeoutException when the driver has determined that
//	 *         the timeout value has been exceeded
//	 * @throws NullPointerException if the given connection is {@code null}
//	 */
//	public int update(final Connection conn) throws SQLException {
//		try (PreparedStatement stmt = statement(conn)) {
//			return stmt.executeUpdate();
//		}
//	}
//
//	/**
//	 * Executes the SQL statement defined by {@code this} query object, which
//	 * must be an {@code INSERT} statement.
//	 *
//	 * @param conn the DB connection where {@code this} query is executed on
//	 * @return the key generated during the insertion
//	 * @throws SQLException if a database access error occurs
//	 * @throws java.sql.SQLTimeoutException when the driver has determined that
//	 *         the timeout value has been exceeded
//	 * @throws NullPointerException if the given connection is {@code null}
//	 */
//	public Optional<Long> insert(final Connection conn)
//		throws SQLException
//	{
//		try (PreparedStatement stmt = statement(conn)) {
//			stmt.executeUpdate();
//			return readID(stmt);
//		}
//	}
//
//	public <T> Long insert(
//		final T row,
//		final SqlFunction2<? super T, Connection, ? extends SqlParamValues> f,
//		final Connection conn
//	)
//		throws SQLException
//	{
//		return null;
//	}
//
//	public <T> Long insert(
//		final T row,
//		final SqlFunction3<? super T, String, Connection, Value> dctor,
//		final Connection conn
//	)
//		throws SQLException
//	{
//		try (PreparedStatement stmt = statement(conn)) {
//			fill(row, dctor, stmt, conn);
//			stmt.executeUpdate();
//			return readID(stmt).orElse(null);
//		}
//	}
//
//	private <T> void fill(
//		final T row,
//		final SqlFunction3<? super T, String, Connection, Value> dctor,
//		final PreparedStatement stmt,
//		final Connection conn
//	)
//		throws SQLException
//	{
//		int index = 0;
//		for (String name : _sql.paramNames()) {
//			final Value value = dctor.apply(row, name, conn);
//			if (value != null) {
//				value.set(stmt, ++index);
//			} else {
//				throw new NoSuchElementException(format(
//					"Value for column '%s' not found.", name
//				));
//			}
//		}
//	}
//
//	public <T> SqlFunction2<T, Connection, Long>
//	insert(final SqlFunction3<? super T, String, Connection, Value> dctor) {
//		throw new UnsupportedOperationException();
//	}
//
//	static Object toSQLValue(final Object value) {
//		Object result = value;
//
//		while (result instanceof Optional) {
//			result = ((Optional<?>)result).orElse(null);
//		}
//
//		if (result instanceof URI) {
//			result = result.toString();
//		} else if (result instanceof URL) {
//			result = result.toString();
//		} else if (result instanceof ZonedDateTime) {
//			result = ((ZonedDateTime)result).toOffsetDateTime();
//		}
//
//		return result;
//	}
//
//	/**
//	 * Inserts the given rows in one transaction and with the same prepared
//	 * statement.
//	 *
//	 * @param rows the rows to insert
//	 * @param dctor the deconstruction function, which splits a given row into
//	 *        its components. This components can than be used setting the
//	 *        parameter values of the query.
//	 * @param conn the DB connection where {@code this} query is executed on
//	 * @param <T> the row type
//	 * @throws SQLException if a database access error occurs
//	 * @throws java.sql.SQLTimeoutException when the driver has determined that
//	 *         the timeout value has been exceeded
//	 * @throws NullPointerException if one of the parameters is {@code null}
//	 */
//	public <T> void inserts(
//		final Collection<T> rows,
//		final SqlFunction3<? super T, String, Connection, Value> dctor,
//		final Connection conn
//	)
//		throws SQLException
//	{
//		if (!rows.isEmpty()) {
//			try (PreparedStatement stmt = statement(conn)) {
//				for (T row : rows) {
//					fill(row, dctor, stmt, conn);
//					stmt.executeUpdate();
//				}
//			}
//		}
//	}
//
