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
import java.util.stream.IntStream;

/**
 * A {@code Query} represents an executable piece of SQL text.
 *
 * <pre>{@code
 * private static final Query SELECT = Query.of(
 *     "SELECT * FROM person " +
 *     "WHERE forename like :forename " +
 *     "ORDER BY surname;"
 * );
 *
 * private static final Query INSERT = Query.of(
 *     "INSERT INTO person(forename, surname, birthday, email) " +
 *     "VALUES(:forename, :surname, :birthday, :email);"
 * );
 * }</pre>
 *
 * @apiNote
 * This class is immutable and thread-safe.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
public final class Query {

	private final Sql _sql;
	private final ParamValues _values;

	private Query(final Sql sql, final ParamValues values) {
		_sql = requireNonNull(sql);
		_values = requireNonNull(values);
	}

	/**
	 * Return the prepared SQL string. All parameter names has been replaced
	 * with '?' placeholders.
	 *
	 * @return the prepared SQL string
	 */
	public String sql() {
		return _sql.string();
	}

	/**
	 * Return the list of parsed parameter names. The list may be empty or
	 * contain duplicate entries, depending on the input string. The list are
	 * in exactly the order they appeared in the SQL string and can be used for
	 * determining the parameter index for the {@link PreparedStatement}.
	 *
	 * @return the parsed parameter names
	 */
	public List<String> paramNames() {
		return _sql.paramNames();
	}


	/* *************************************************************************
	 * Query parameter setting.
	 * ************************************************************************/

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * @see #on(Param...)
	 * @see #on(Map)
	 * @see #on(Object, Dctor)
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 */
	public Query on(final List<? extends Param> params) {
		return params.isEmpty()
			? this
			: new Query(_sql, _values.andThen(new Params(params)));
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
	 * @param record the query parameters
	 * @param dctor the deconstructor used to <em>split</em> the parameters
	 * @param <T> the parameter record type
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public <T> Query on(final T record, final Dctor<T> dctor) {
		requireNonNull(record);
		requireNonNull(dctor);

		final ParamValues values = (params, stmt) -> dctor
			.deconstruct(record, stmt.getConnection())
			.set(params, stmt);

		return new Query(_sql, _values.andThen(values));
	}


	/* *************************************************************************
	 * Executing query.
	 * ************************************************************************/

	/**
	 * Executes {@code this} query and parses the result with the given
	 * result-set parser.
	 *
	 * @see PreparedStatement#executeQuery()
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
		try (var stmt = prepare(conn); var rs = stmt.executeQuery()) {
			return parser.parse(rs, conn);
		}
	}

	private PreparedStatement prepare(final Connection conn)
		throws SQLException
	{
		final PreparedStatement stmt = conn.prepareStatement(sql());
		_values.set(paramNames(), stmt);
		return stmt;
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
	public boolean execute(final Connection conn) throws SQLException {
		try (PreparedStatement stmt = prepare(conn)) {
			return stmt.execute();
		}
	}

	/**
	 * Executes the SQL statement in a {@link PreparedStatement} object, which
	 * must be an SQL Data Manipulation Language (DML) statement, such as
	 * {@code INSERT}, {@code UPDATE} or {@code DELETE}; or an SQL statement
	 * that returns nothing, such as a DDL statement.
	 *
	 * @see PreparedStatement#executeUpdate()
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return either (1) the row count for SQL Data Manipulation Language (DML)
	 *         statements or (2) 0 for SQL statements that return nothing
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given connection is {@code null}
	 */
	public int executeUpdate(final Connection conn) throws SQLException {
		try (PreparedStatement stmt = prepare(conn)) {
			return stmt.executeUpdate();
		}
	}

	/**
	 * Executes the SQL statement in a {@link PreparedStatement} object, which
	 * must be an SQL {@code INSERT}. It returns, the optionally generated, key
	 * for the inserted row.
	 *
	 * @see PreparedStatement#executeUpdate()
	 * @see #executeInsert(Connection)
	 *
	 * @param keyParser the row parser for the key to return
	 * @param conn the DB connection where {@code this} query is executed on
	 * @param <K> the generated key type
	 * @return the generated key, if available
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public <K> Optional<K> executeInsert(
		final RowParser<K> keyParser,
		final Connection conn
	)
		throws SQLException
	{
		try (PreparedStatement stmt = prepareInsert(conn)) {
			stmt.executeUpdate();
			return readId(keyParser, stmt, conn);
		}
	}

	private PreparedStatement prepareInsert(final Connection conn)
		throws SQLException
	{
		final PreparedStatement stmt = conn.prepareStatement(
			_sql.string(),
			RETURN_GENERATED_KEYS
		);

		_values.set(paramNames(), stmt);
		return stmt;
	}

	private static <K> Optional<K> readId(
		final RowParser<K> keyParser,
		final Statement stmt,
		final Connection conn
	)
		throws SQLException
	{
		try (ResultSet keys = stmt.getGeneratedKeys()) {
			return keyParser.singleOpt().parse(keys, conn);
		}
	}

	/**
	 * Executes the SQL statement in a {@link PreparedStatement} object, which
	 * must be an SQL {@code INSERT}. It returns, the optionally generated, key
	 * for the inserted row.
	 *
	 * @see PreparedStatement#executeUpdate()
	 * @see #executeInsert(RowParser, Connection)
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return the generated key, if available
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public Optional<Long> executeInsert(final Connection conn)
		throws SQLException
	{
		return executeInsert(RowParser.int64(1), conn);
	}


	/* *************************************************************************
	 * Batch query.
	 * ************************************************************************/

	/**
	 * Executes the given batch on {@code this} query object, which may be any
	 * kind of SQL statement.
	 *
	 * @see PreparedStatement#execute()
	 * @see #execute(Connection)
	 * @see #executeUpdate(Batch, Connection)
	 *
	 * @param batch the batch to execute
	 * @param conn the DB connection where {@code this} query is executed on
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public void execute(final Batch batch, final Connection conn)
		throws SQLException
	{
		try (PreparedStatement stmt = prepare(conn)) {
			for (var row : batch) {
				row.apply(conn).set(paramNames(), stmt);
				stmt.execute();
			}
		}
	}

	/**
	 * Executes the given {@code batch} for this query, which must be an SQL
	 * Data Manipulation Language (DML) statement, such as {@code INSERT},
	 * {@code UPDATE} or {@code DELETE}.
	 *
	 * @see PreparedStatement#executeUpdate()
	 * @see #executeUpdate(Connection)
	 * @see #execute(Batch, Connection)
	 *
	 * @param batch the batch to execute
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return an int[] with the update counts
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public int[] executeUpdate(final Batch batch, final Connection conn)
		throws SQLException
	{
		final IntStream.Builder counts = IntStream.builder();
		try (PreparedStatement stmt = prepare(conn)) {
			for (var row : batch) {
				row.apply(conn).set(paramNames(), stmt);
				final int count = stmt.executeUpdate();
				counts.add(count);
			}
		}

		return counts.build().toArray();
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new query object from the given SQL string.
	 * <pre>{@code
	 * private static final Query SELECT = Query.of(
	 *     "SELECT * FROM person " +
	 *     "WHERE forename like :forename " +
	 *     "ORDER BY surname;"
	 * );
	 *
	 * private static final Query INSERT = Query.of(
	 *     "INSERT INTO person(forename, surname, birthday, email) " +
	 *     "VALUES(:forename, :surname, :birthday, :email);"
	 * );
	 * }</pre>
	 *
	 * @param sql the SQL string of the created query
	 * @return a new query object from the given SQL string
	 * @throws NullPointerException if the given SQL string is {@code null}
	 */
	public static Query of(final String sql) {
		return new Query(Sql.of(sql), ParamValues.EMPTY);
	}

}
