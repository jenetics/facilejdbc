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

import io.jenetics.facilejdbc.function.SqlConsumer;
import io.jenetics.facilejdbc.function.SqlFunction;

/**
 * This interface defines methods for executing a SQL query in a transactional
 * context.
 *
 * <pre>{@code
 * final Transaction transaction = ...;
 * final Optional<Long> id = transaction.apply(conn ->
 *     Query.of("SELECT id FROM author WHERE name = :name")
 *         .on(value("name", "Hemingway"))
 *         .as(RowParser.int64("id").singleOpt(), conn);
 * );
 * }</pre>
 *
 * All queries within the {@code apply} block are executed within the same
 * transaction. If any exception is thrown within this block, a rollback on the
 * given connection is performed. For code {@code blocks} without return values,
 * the {@link #accept(SqlConsumer)} method is used.
 *
 * @see Transactional
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public interface Transaction {

	/**
	 * Executes the given {@code block} with a DB connection.
	 *
	 * @param block the SQL function which is executed within a DB transaction
	 * @param <T> the returned data type
	 * @return the result of the given SQL {@code block}
	 * @throws SQLException it the execution of the SQL block fails. In this
	 *         case a rollback is performed.
	 */
	<T> T apply(final SqlFunction<? super Connection, ? extends T> block)
		throws SQLException;

	/**
	 * Executes the given {@code block} with a DB connection.
	 *
	 * @param block the SQL function which is executed within a DB transaction
	 * @throws SQLException it the execution of the SQL block fails. In this
	 *         case a rollback is performed.
	 */
	default void accept(final SqlConsumer<? super Connection> block)
		throws SQLException
	{
		apply(conn -> { block.accept(conn); return null; });
	}


	/**
	 * Open a new <i>transactional</i> context with the given connection. The
	 * caller is responsible for closing the connection. This method is
	 * <em>only</em> responsible for the correct transaction handling:
	 * <ul>
	 *     <li>Setting the autocommit flag of the connection to false.</li>
	 *     <li>Committing the connection on success.</li>
	 *     <li>Perform a rollback if an exception is thrown. The thrown
	 *     exception is then propagated to the caller.</li>
	 * </ul>
	 *
	 * @apiNote
	 * This method implements the transactional default behaviour ot the
	 * {@link Transaction} implementation, returned by the
	 * {@link Transactional#transaction()} interface.
	 *
	 * @param conn the connection used in this transaction
	 * @param block the code block to execute with the given connection
	 * @param <T> the result type
	 * @return the result of the connection block
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws SQLException if the transaction fails
	 */
	static <T> T apply(
		final Connection conn,
		final SqlFunction<? super Connection, ? extends T> block
	)
		throws SQLException
	{
		requireNonNull(conn);
		requireNonNull(block);

		try {
			if (conn.getAutoCommit()) {
				conn.setAutoCommit(false);
			}
			var result = block.apply(conn);
			conn.commit();
			return result;
		} catch (Throwable e) {
			try {
				conn.rollback();
			} catch (Exception suppressed) {
				e.addSuppressed(suppressed);
			}
			throw e;
		}
	}

}
