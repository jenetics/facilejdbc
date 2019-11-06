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

import io.jenetics.facilejdbc.function.SqlSupplier;

/**
 * Represents a query parameter with <em>name</em> and <em>value</em>. The
 * parameter value is evaluated lazily.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public interface SqlParam {

	/**
	 * Return the parameter name.
	 *
	 * @return the parameter name
	 */
	public String name();

	/**
	 * Return the parameter values.
	 *
	 * @return the parameter values
	 */
	public SqlParamValue value();


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	public static SqlParam of(final String name, final SqlParamValue value) {
		requireNonNull(name);
		requireNonNull(value);

		return new SqlParam() {
			@Override
			public String name() {
				return name;
			}
			@Override
			public SqlParamValue value() {
				return value;
			}
			public String toString() {
				return ":" + name;
			}
		};
	}

	/**
	 * Create a new query parameter object from the given {@code name} and
	 * {@code value}.
	 *
	 * @param name the parameter name
	 * @param value the parameter values
	 * @return a new query parameter object
	 * @throws NullPointerException if the given parameter {@code name} is
	 *         {@code null}
	 */
	public static SqlParam value(final String name, final Object value) {
		return SqlParam.of(name, (stmt, index) -> stmt.setObject(index, value));
	}

	public static SqlParam lazy(final String name, final SqlSupplier<?> value) {
		requireNonNull(value);
		return SqlParam.of(name, (stmt, index) -> stmt.setObject(index, value.get()));
	}

}
