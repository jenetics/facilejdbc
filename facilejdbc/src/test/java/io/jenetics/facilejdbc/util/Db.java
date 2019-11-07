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
package io.jenetics.facilejdbc.util;

import io.jenetics.facilejdbc.function.SqlFunction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class contains some helper functions for DB transaction handling.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public final class Db {
	private Db() {
	}

	/**
	 * Open a new <i>transactional</i> context with the given connection. The
	 * caller is responsible for closing the connection.
	 *
	 * @param connection the connection used in this transaction
	 * @param block the code block to execute with the given connection
	 * @param <T> the result type
	 * @return the result of the connection block
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws SQLException if the transaction fails
	 */
	public static <T> T transaction(
		final Connection connection,
		final SqlFunction<? super Connection, ? extends T> block
	)
		throws SQLException
	{
		try {
			if (connection.getAutoCommit()) {
				connection.setAutoCommit(false);
			}
			var result = block.apply(connection);
			connection.commit();
			return result;
		} catch (Throwable e) {
			try {
				connection.rollback();
			} catch (Exception suppressed) {
				e.addSuppressed(suppressed);
			}
			throw e;
		}
	}

	/**
	 * Open a new <i>transactional</i> context with the given connection. The
	 * caller is responsible for closing the connection.
	 *
	 * @param ds the data source where the connection for the transaction is
	 *           created.
	 * @param block the code block to execute with the given connection
	 * @param <T> the result type
	 * @return the result of the connection block
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws SQLException if the transaction fails
	 */
	public static <T> T transaction(
		final DataSource ds,
		final SqlFunction<? super Connection, ? extends T> block
	)
		throws SQLException
	{
		try (var conn = ds.getConnection()) {
			return transaction(conn, block);
		}
	}

}
