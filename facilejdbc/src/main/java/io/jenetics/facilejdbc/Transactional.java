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
import java.sql.SQLException;

import io.jenetics.facilejdbc.function.SqlFunction;

/**
 * This interface represents the <em>transactional</em> capability, typically
 * exposed by a database. In this sense, it can be seen as a minimal database
 * interface, just by exposing a {@link Connection} factory method,
 * {@link #connection()}.
 *
 * <pre>{@code
 * final Transactional db = () -> DriverManager.getConnection(
 *     "jdbc:hsqldb:mem:testdb",
 *     "SA",
 *     ""
 * );
 * }</pre>
 *
 * The code example shows how easy it is to create an in-memory HSQLDB
 * {@code Transactional} instance. If you already have a
 * {@link javax.sql.DataSource} instance, the creation of a <em>transactional</em>
 * object is even easier.
 *
 * <pre>{@code
 * final DataSource ds = ...;
 * final Transactional db = ds::getConnection;
 * }</pre>
 *
 * The usage of the <em>db</em> is then also very straight forward.
 *
 * <pre>{@code
 * final long id = db.transaction().apply(conn ->
 *     INSERT_QUERY
 *         .on(author, DCTOR)
 *         .executeInsert(conn)
 *         .orElseThrow()
 * );
 * }</pre>
 *
 * Using a transaction for batch update.
 *
 * <pre>{@code
 * db.transaction().accept(conn ->
 *     final Batch batch = Batch.of(
 *         authors,
 *         Dctor.of(
 *             field("name", Author::name),
 *             field("age", Author::age)
 *         )
 *     );
 *
 *     INSERT_BOOK_AUTHOR.executeUpdate(batch, conn);
 * );
 * }</pre>
 *
 * @apiNote
 * The transactional default behaviour
 *
 *  @see Transaction
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
@FunctionalInterface
public interface Transactional {

	/**
	 * Return the DB connection. If you obtain a new connection you are
	 * responsible for closing it after usage. This is done ideally in a
	 * resource-try block.
	 *
	 * <pre>{@code
	 * final Transactional db = ...;
	 * try (var conn = db.connection()) {
	 *     // Using the connection.
	 *     ...;
	 * }
	 * }</pre>
	 *
	 * You are usually using the {@link #transaction()} method for getting an
	 * instance of the {@link Transaction} interface, which is then using this
	 * method for obtaining the needed connections.
	 *
	 * @see #transaction()
	 *
	 * @return the DB connection
	 * @throws SQLException if obtaining a DB connection fails
	 */
	Connection connection() throws SQLException;

	/**
	 * Return a <em>Transaction</em> object, which obtains the connection,
	 * needed for executing a query, from the {@link #connection()} factory
	 * method. The transactional behaviour is defined by the
	 * {@link #txm(Connection, SqlFunction)} method of {@code this} interface.
	 *
	 * <pre>{@code
	 * final Transaction db = ...;
	 * final long id = db.transaction().apply(conn ->
	 *     INSERT_QUERY
	 *         .on(author, DCTOR)
	 *         .executeInsert(conn)
	 *         .orElseThrow()
	 * );
	 * }</pre>
	 *
	 * @see #txm(Connection, SqlFunction)
	 *
	 * @return a new <em>Transaction</em> object
	 */
	default Transaction transaction() {
		return new Transaction() {
			@Override
			public <T> T apply(final SqlFunction<? super Connection, ? extends T> block)
				throws SQLException
			{
				try (var conn = connection()) {
					return Transactional.this.txm(conn, block);
				}
			}
		};
	}

	/**
	 * This method defines the transactional behaviour of the {@link Transaction}
	 * interface, returned by the {@link #transaction()} method. The default
	 * implementation is given by the {@link Transaction#apply(Connection, SqlFunction)}
	 * method. If a different behaviour is needed, override this method.
	 *
	 * @see Transaction#apply(Connection, SqlFunction)
	 *
	 * @param conn the connection used in this transaction
	 * @param block the code block to execute with the given connection
	 * @param <T> the result type
	 * @return the result of the connection block
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws SQLException if the transaction fails
	 */
	default <T> T txm(
		final Connection conn,
		final SqlFunction<? super Connection, ? extends T> block
	)
		throws SQLException
	{
		return Transaction.apply(conn, block);
	}

}
