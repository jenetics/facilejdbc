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
package io.jenetics.facilejdbc.function;

import java.sql.SQLException;

/**
 * Represents a function that accepts three argument and produces a result. In
 * contrast to the Java {@link java.util.function.Function} interface, a
 * SQL-function is allowed to throw a {@link SQLException}.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface SqlFunction3<A, B, C, R> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param a the first argument
	 * @param b the second argument
	 * @param c the third argument
	 * @return the function result
	 * @throws SQLException if the execution of the SQL-function fails
	 */
	public R apply(final A a, final B b, final C c) throws SQLException;

}