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

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static io.jenetics.facilejdbc.spi.SqlTypeMapper.map;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.jenetics.facilejdbc.function.SqlSupplier;

/**
 * This is the  base interface of the {@link SingleParam} and {@link MultiParam}
 * class.
 * <p>
 * Creating single-valued parameters:
 * <pre>{@code
 * INSERT_QUERY.on(
 *     Param.value("forename", "Werner"),
 *     Param.value("birthday", LocalDate.now()),
 *     Param.value("email", "some.email@gmail.com"))
 * }</pre>
 *
 * Creating multivalued parameters:
 * <pre>{@code
 * Query.of("SELECT * FROM table WHERE id = IN(:ids);")
 *     .on(Param.values("ids", 1, 2, 3, 4))
 * }</pre>
 *
 * @see SingleParam
 * @see MultiParam
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 1.3
 */
public sealed interface Param permits SingleParam, MultiParam {

	/**
	 * Return the parameter name.
	 *
	 * @return the parameter name
	 */
	String name();

	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new query parameter object for the given {@code name} and
	 * {@code value}.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = :id;")
	 *     .on(Param.value("id", 43245)
	 *     .as(PARSER.singleOpt(), conn);
	 * }</pre>
	 *
	 * @param name the parameter name
	 * @param value the parameter values, which may be {@code null}
	 * @return a new query parameter object
	 * @throws NullPointerException if the given parameter {@code name} is
	 *         {@code null}
	 */
	static SingleParam value(final String name, final Object value) {
		return SingleParam.of(
			name,
			(idx, stmt) -> stmt.setObject(idx, map(value))
		);
	}

	/**
	 * Create a new (multi) query parameter object for the given {@code name}
	 * and the given {@code values}.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = IN(:ids);")
	 *     .on(Param.values("ids", List.of(43245, 434, 23, 987, 1239))
	 *     .as(PARSER.list(), conn);
	 * }</pre>
	 *
	 * @since 1.3
	 *
	 * @see #values(String, Object...)
	 *
	 * @param name the parameter name
	 * @param values the query parameters
	 * @return a new query parameter object
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the given {@code values} collection
	 *         is empty
	 */
	static MultiParam values(final String name, final Iterable<?> values) {
		return MultiParam.of(
			name,
			stream(values)
				.map(v -> (ParamValue)(idx, stmt) -> stmt.setObject(idx, map(v)))
				.toList()
		);
	}

	@SuppressWarnings("unchecked")
	private static <T> Stream<T> stream(final Iterable<? extends T> values) {
		return values instanceof Collection<?>
			? ((Collection<T>)values).stream()
			: StreamSupport.stream(((Iterable<T>)values).spliterator(), false);
	}

	/**
	 * Create a new (multi) query parameter object for the given {@code name}
	 * and the given {@code values}.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = IN(:ids);")
	 *     .on(Param.values("ids", 43245, 434, 23, 987, 1239)
	 *     .as(PARSER.list(), conn);
	 * }</pre>
	 *
	 * @since 1.3
	 *
	 * @see #values(String, Iterable)
	 *
	 * @param name the parameter name
	 * @param values the query parameters
	 * @return a new query parameter object
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the length of the given {@code values}
	 *         array is zero
	 */
	static MultiParam values(final String name, final Object... values) {
		return values(name, asList(values));
	}

	/**
	 * Create a new query parameter object from the given {@code name} and
	 * lazily evaluated {@code value}.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE date < :date;")
	 *     .on(Param.lazyValue("date", LocalDate::now)
	 *     .as(PARSER.singleOpt(), conn);
	 * }</pre>
	 *
	 * @param name the parameter name
	 * @param value the lazily evaluated parameter value
	 * @return a new query parameter object
	 * @throws NullPointerException if one the arguments is {@code null}
	 */
	static SingleParam lazyValue(final String name, final SqlSupplier<?> value) {
		requireNonNull(value);
		return SingleParam.of(
			name,
			(idx, stmt) -> stmt.setObject(idx, map(value.get()))
		);
	}

	/**
	 * Create a new query parameter object for the given {@code name} and
	 * lazily evaluated {@code values}.
	 *
	 * <pre>{@code
	 * final SqlSupplier<Integer> id1 = ...;
	 * final SqlSupplier<Integer> id2 = ...;
	 * final var result = Query.of("SELECT * FROM table WHERE id = IN(:ids);")
	 *     .on(Param.lazyValues("id", List.of(id1, id2))
	 *     .as(PARSER.list(), conn);
	 * }</pre>
	 *
	 * @param name the parameter name
	 * @param values the parameter values
	 * @return a new query parameter object
	 * @throws NullPointerException if one the arguments is {@code null}
	 * @throws IllegalArgumentException if the given {@code values} collection
	 *         is empty
	 */
	static MultiParam lazyValues(
		final String name,
		final Iterable<? extends SqlSupplier<?>> values
	) {
		return MultiParam.of(
			name,
			stream(values)
				.map(v -> (ParamValue)(i, stmt) -> stmt.setObject(i, map(v.get())))
				.toList()
		);
	}

	/**
	 * Create a new query parameter object for the given {@code name} and
	 * lazily evaluated {@code values}.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = IN(:ids);")
	 *     .on(Param.lazyValues("id", () -> 324, () -> 9967))
	 *     .as(PARSER.list(), conn);
	 * }</pre>
	 *
	 * @param name the parameter name
	 * @param values the parameter values
	 * @return a new query parameter object
	 * @throws NullPointerException if one the arguments is {@code null}
	 * @throws IllegalArgumentException if the length of the given {@code values}
	 *         array is zero
	 */
	static MultiParam lazyValues(
		final String name,
		final SqlSupplier<?>... values
	) {
		return lazyValues(name, asList(values));
	}

}
