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
 * parameter value is evaluated lazily. But it is also possible to create
 * {@code Param} objects with eagerly evaluated values.
 * {@snippet lang="java":
 * INSERT_QUERY.on(
 *     Param.value("forename", "Werner"),
 *     Param.value("birthday", LocalDate.now()),
 *     Param.value("email", "some.email@gmail.com"))
 * }
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 1.0
 */
public non-sealed interface SingleParam extends Param {

	/**
	 * Return the parameter value.
	 *
	 * @return the parameter value
	 */
	ParamValue value();

	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new query parameter object from the given {@code name} and
	 * {@code value}.
	 *
	 * @see #value(String, Object)
	 * @see #lazyValue(String, SqlSupplier)
	 *
	 * @param name the parameter name
	 * @param value the parameter values
	 * @return a new query parameter object
	 * @throws NullPointerException if the given parameter {@code name} is
	 *         {@code null}
	 */
	static SingleParam of(final String name, final ParamValue value) {
		requireNonNull(name);
		requireNonNull(value);

		return new SingleParam() {
			@Override
			public String name() {
				return name;
			}
			@Override
			public ParamValue value() {
				return value;
			}
			@Override
			public String toString() {
				return ":" + name;
			}
		};
	}

}
