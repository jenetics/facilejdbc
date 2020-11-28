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
import static io.jenetics.facilejdbc.spi.SqlTypeMapper.map;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.jenetics.facilejdbc.function.SqlSupplier;

/**
 * Represents a query parameter with <em>name</em> and <em>value</em>. The
 * parameter value is evaluated lazily. But it is also possible to create
 * {@code Param} objects with eagerly evaluated values.
 *
 * <pre>{@code
 * INSERT_QUERY
 *     .on(
 *         Param.value("forename", "Werner"),
 *         Param.value("birthday", LocalDate.now()),
 *         Param.value("email", "some.email@gmail.com"))
 * }</pre>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
public interface Param extends BaseParam {

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
	 * @see #lazy(String, SqlSupplier)
	 *
	 * @param name the parameter name
	 * @param value the parameter values
	 * @return a new query parameter object
	 * @throws NullPointerException if the given parameter {@code name} is
	 *         {@code null}
	 */
	static Param of(final String name, final ParamValue value) {
		requireNonNull(name);
		requireNonNull(value);

		return new Param() {
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

	/**
	 * Create a new query parameter object from the given {@code name} and
	 * {@code value}.
	 *
	 * @param name the parameter name
	 * @param value the parameter values, which may be {@code null}
	 * @return a new query parameter object
	 * @throws NullPointerException if the given parameter {@code name} is
	 *         {@code null}
	 */
	static Param value(final String name, final Object value) {
		requireNonNull(value);
		return Param.of(
			name,
			(index, stmt) -> stmt.setObject(index, map(value))
		);
	}

	static MultiParam values(final String name, final Object... values) {
		return MultiParam.of(
			name,
			Stream.of(values)
				.map(v -> (ParamValue)(index, stmt) -> stmt.setObject(index, map(v)))
				.collect(Collectors.toUnmodifiableList())
		);
	}

	/**
	 * Create a new query parameter object from the given {@code name} and
	 * lazily evaluated {@code value}.
	 *
	 * @param name the parameter name
	 * @param value the lazily evaluated parameter values
	 * @return a new query parameter object
	 * @throws NullPointerException if the given parameter {@code name} is
	 *         {@code null}
	 */
	static Param lazy(final String name, final SqlSupplier<?> value) {
		requireNonNull(value);
		return Param.of(
			name,
			(index, stmt) -> stmt.setObject(index, map(value.get()))
		);
	}

}
