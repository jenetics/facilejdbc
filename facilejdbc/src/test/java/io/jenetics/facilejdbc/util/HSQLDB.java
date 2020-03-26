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
import java.sql.DriverManager;
import java.sql.SQLException;

import io.jenetics.facilejdbc.function.SqlFunction;
import io.jenetics.facilejdbc.function.SqlFunction0;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public final class HSQLDB {
	private HSQLDB() {
	}

	private static Connection conn() throws SQLException {
		return DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
	}

	public static <T> T
	execute(final SqlFunction<? super Connection, ? extends T> block)
		throws SQLException
	{
		return Transactions.execute(conn(), block);
	}

	public static void
	run(final SqlFunction0<? super Connection> block)
		throws SQLException
	{
		Transactions.run(conn(), block);
	}

}
