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
 * Represents a supplier of results. There is no requirement that a new or
 * distinct result be returned each time the supplier is invoked.
 *
 * This is a functional interface whose functional method is {@link #get()}.
 *
 * @param <T> the result type
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface SqlSupplier<T> {

	/**
	 * Return a result
	 *
	 * @return a result
	 * @throws SQLException if fetching the result fails
	 */
	T get() throws SQLException;

}
