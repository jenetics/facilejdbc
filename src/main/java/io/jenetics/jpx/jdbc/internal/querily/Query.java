/*
 * Java GPX Library (@__identifier__@).
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
package io.jenetics.jpx.jdbc.internal.querily;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * A {@code Query} represents an executable piece of SQL text.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public abstract class Query {

	private final String _sql;
	private final List<String> _names;

	Query(final String sql, final List<String> names) {
		_sql = requireNonNull(sql);
		_names = unmodifiableList(names);
	}

	/**
	 * Return the SQL string of {@code this} query class.
	 *
	 * @return the SQL string of {@code this} query class
	 */
	public String sql() {
		return _sql;
	}

	/**
	 * Return the parameter names of this query. The returned list may be empty.
	 *
	 * @return the parameter names of this query
	 */
	public List<String> names() {
		return _names;
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
		try (PreparedStatement stmt = prepare(conn)) {
			return stmt.execute();
		}
	}

	/**
	 * Executes the SQL statement defined by {@code this} query object, which
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
	 * Executes the SQL statement defined by {@code this} query object, which
	 * must be an {@code INSERT} statement.
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return the key generated during the insertion
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given connection is {@code null}
	 */
	public Optional<Long> executeInsert(final Connection conn)
		throws SQLException
	{
		try (PreparedStatement stmt = prepare(conn)) {
			stmt.executeUpdate();
			return readID(stmt);
		}
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
		try (PreparedStatement ps = prepare(conn);
			 ResultSet rs = ps.executeQuery())
		{
			return parser.parse(rs);
		}
	}


	abstract PreparedStatement prepare(final Connection conn) throws SQLException;


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

}
