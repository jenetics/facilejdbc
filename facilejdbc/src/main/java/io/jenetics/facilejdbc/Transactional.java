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

import java.sql.Connection;
import java.sql.SQLException;

import io.jenetics.facilejdbc.function.SqlFunction;
import io.jenetics.facilejdbc.function.SqlSupplier;

/**
 * This interface represents the <em>transactional</em> capability, typically
 * exposed by a database. In this sense, it can be seen as a minimal database
 * interface, just by exposing a {@link Connection} factory method,
 * {@link #connection()}.
 * {@snippet lang="java":
 * final Transactional db = () -> DriverManager.getConnection(
 *     "jdbc:hsqldb:mem:testdb",
 *     "SA",
 *     ""
 * );
 * }
 *
 * The code example shows how easy it is to create an in-memory HSQLDB
 * {@code Transactional} instance. If you already have a
 * {@link javax.sql.DataSource} instance, the creation of a <em>transactional</em>
 * object is even easier.
 * {@snippet lang="java":
 * final DataSource ds = null; // @replace substring='null' replacement="..."
 * final Transactional db = ds::getConnection;
 * }
 *
 * If you want to implement a different transaction strategy, you have also to
 * implement the {@link #txm(Connection, SqlSupplier)} method of {@code this}
 * interface.
 * {@snippet lang="java":
 * final var db = new Transactional() {
 *     public Connection connection() throws SQLException {
 *         return DriverManager.getConnection(
 *             "jdbc:hsqldb:mem:testdb",
 *             "SA",
 *             ""
 *         );
 *     }
 *     public <T> T txm(final Connection conn, final SqlSupplier<? extends T> block)
 *         throws SQLException
 *     {
 *         // Implement your transaction handling.
 *         return null; // @replace substring='null' replacement="..."
 *     }
 * };
 * }
 *
 *
 * The usage of the <em>db</em> is then also very straight forward.
 * {@snippet lang="java":
 * final long id = db.transaction().apply(conn ->
 *     INSERT_QUERY
 *         .on(author, DCTOR)
 *         .executeInsert(conn)
 *         .orElseThrow()
 * );
 * }
 *
 * Using a transaction for batch update.
 * {@snippet lang="java":
 * db.transaction().accept(conn -> {
 *     final Batch batch = Batch.of(
 *         authors,
 *         Dctor.of(
 *             field("name", Author::name),
 *             field("age", Author::age)
 *         )
 *     );
 *
 *     INSERT_BOOK_AUTHOR.executeUpdate(batch, conn);
 * });
 * }
 *
 * @apiNote
 * The transactional default behavior
 *
 * @see Transaction
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.1
 * @since 1.1
 */
@FunctionalInterface
public interface Transactional {

	/**
	 * Return the DB connection. If you get a new connection, you are
	 * responsible for closing it after usage. This is done ideally in a
	 * resource-try block.
	 * {@snippet lang="java":
	 * final Transactional db = null; // @replace substring='null' replacement="..."
	 * try (var conn = db.connection()) {
	 *     // Using the connection.
	 *     // ...
	 * }
	 * }
	 *
	 * You are usually using the {@link #transaction()} method for getting an
	 * instance of the {@link Transaction} interface, which is then using this
	 * method for getting the necessary connections.
	 *
	 * @see #transaction()
	 *
	 * @return the DB connection
	 * @throws SQLException if getting a DB connection fails
	 */
	Connection connection() throws SQLException;

	/**
	 * Return a <em>Transaction</em> object, which gets the connection,
	 * needed for executing a query, from the {@link #connection()} factory
	 * method. The transactional behavior is defined by the
	 * {@link #txm(Connection, SqlSupplier)} method of {@code this} interface.
	 * {@snippet lang="java":
	 * final Transaction db = null; // @replace substring='null' replacement="..."
	 * final long id = db.transaction().apply(conn ->
	 *     INSERT_QUERY
	 *         .on(author, DCTOR)
	 *         .executeInsert(conn)
	 *         .orElseThrow()
	 * );
	 * }
	 *
	 * @implNote
	 * It is possible to store the {@code Transaction} instance, returned by
	 * this method, in a variable and use it for more than one call.
	 *
	 * @see #txm(Connection, SqlSupplier)
	 *
	 * @return a new <em>Transaction</em> object
	 */
	default Transaction transaction() {
		return new Transaction() {
			@Override
			public <T> T apply(final SqlFunction<? super Connection, ? extends T> block)
				throws SQLException
			{
				requireNonNull(block);

				try (var conn = connection()) {
					return Transactional.this.txm(conn, () -> block.apply(conn));
				}
			}
		};
	}

	/**
	 * This method defines the transactional behavior of the {@link Transaction}
	 * interface, returned by the {@link #transaction()} method. The default
	 * implementation is given by the {@link Transaction#txm(Connection, SqlSupplier)}
	 * method. If a different behavior is needed, override this method.
	 *
	 * @see Transaction#txm(Connection, SqlSupplier)
	 *
	 * @param conn the connection used in this transaction. The connection is
	 *        not closed by this method. Only <em>committed</em> or
	 *        <em>rolled back</em>.
	 * @param block the code block to execute with the given connection
	 * @param <T> the result type
	 * @return the result of the connection block
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws SQLException if the transaction fails
	 */
	default <T> T txm(final Connection conn, final SqlSupplier<? extends T> block)
		throws SQLException
	{
		return Transaction.txm(conn, block);
	}

}
