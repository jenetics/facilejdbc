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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A prepared query has a {@link PreparedStatement} attached and lets you do
 * batch inserts in a convenient way.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.1
 * @since 2.1
 */
public final class PreparedQuery implements AutoCloseable {

	private final PreparedStatement statement;
	private final List<String> paramNames;

	PreparedQuery(final PreparedStatement statement, final List<String> paramNames) {
		this.statement = requireNonNull(statement);
		this.paramNames = requireNonNull(paramNames);
	}

	/**
	 * Executes the given batch on {@code this} query object, which may be any
	 * kind of SQL statement.
	 *
	 * @see PreparedStatement#execute()
	 *
	 * @param batch the batch to execute
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public void execute(final Batch batch)
		throws SQLException
	{
		for (var row : batch) {
			row.apply(statement.getConnection()).set(paramNames, statement);
			statement.execute();
		}
	}

	public void execute(final SingleParam... row) throws SQLException {
		new Params(List.of(row)).set(paramNames, statement);
		statement.execute();
	}

	/**
	 * Executes the given {@code batch} for this query, which must be an SQL
	 * Data Manipulation Language (DML) statement, such as {@code INSERT},
	 * {@code UPDATE} or {@code DELETE}.
	 *
	 * @see PreparedStatement#executeUpdate()
	 *
	 * @param batch the batch to execute
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public int[] executeUpdate(final Batch batch)
		throws SQLException
	{
		final IntStream.Builder counts = IntStream.builder();
		for (var row : batch) {
			row.apply(statement.getConnection()).set(paramNames, statement);
			final int count = statement.executeUpdate();
			counts.add(count);
		}

		return counts.build().toArray();
	}

	public int executeUpdate(final SingleParam... row) throws SQLException {
		new Params(List.of(row)).set(paramNames, statement);
		return statement.executeUpdate();
	}

	/**
	 * Releases this {@code Statement} object's database and JDBC resources
	 * immediately.
	 *
	 * @throws SQLException if a database access error occurs
	 */
	@Override
	public void close() throws SQLException {
		statement.close();
	}

}
