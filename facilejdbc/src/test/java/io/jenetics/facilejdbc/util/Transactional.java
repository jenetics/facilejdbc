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

import java.sql.Connection;
import java.sql.SQLException;

import io.jenetics.facilejdbc.function.SqlFunction;
import io.jenetics.facilejdbc.function.SqlFunction0;

/**
 * This interface represents transactional capability.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public interface Transactional {

	/**
	 * Return the DB connection.
	 *
	 * @return the DB connection
	 * @throws if obtaining a DB connection fails
	 */
	Connection conn() throws SQLException;

	/**
	 * Executes the given {@code block} with the connection returned by the
	 * {@link #conn()} method.
	 *
	 * @param block the SQL function which is executed within a DB transaction
	 * @param <T> the returned data type
	 * @return the result of the given SQL {@code block}
	 * @throws SQLException it the execution of the SQL block fails. In this
	 *         case a rollback is performed.
	 */
	default <T> T execute(final SqlFunction<? super Connection, ? extends T> block)
		throws SQLException
	{
		return Transactions.execute(conn(), block);
	}

	/**
	 * Executes the given {@code block} with the connection returned by the
	 * {@link #conn()} method.
	 *
	 * @param block the SQL function which is executed within a DB transaction
	 * @throws SQLException it the execution of the SQL block fails. In this
	 *         case a rollback is performed.
	 */
	default void run(final SqlFunction0<? super Connection> block)
		throws SQLException
	{
		Transactions.run(conn(), block);
	}

}
