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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a query parameter with <em>name</em> and one or more <em>values</em>.
 * The parameter values are evaluated lazily. But it is also possible to create
 * {@code MultiParam} objects with eagerly evaluated values.
 *
 * <pre>{@code
 * SELECT_QUERY.on(Param.values("ids", 1, 2, 3, 4))
 * }</pre>
 *
 * @see Param
 * @see Param#values(String, Iterable)
 * @see Param#lazyValues(String, Iterable)
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.3
 * @since 1.3
 */
public /*non-sealed*/ interface MultiParam extends BaseParam {

	/**
	 * Return the parameter values.
	 *
	 * @return the parameter values
	 */
	List<ParamValue> values();


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new multi parameter object.
	 *
	 * @param name the parameter name
	 * @param values the values for the given parameter
	 * @return  a new multi parameter object
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the given {@code values} collection
	 *         is empty
	 */
	static MultiParam of(
		final String name,
		final Iterable<? extends ParamValue> values
	) {
		requireNonNull(name);
		final var it = values.iterator();
		if (!it.hasNext()) {
			throw new IllegalArgumentException("Values must not be empty.");
		}

		final List<ParamValue> list = new ArrayList<>();
		values.forEach(list::add);
		final var vals = List.copyOf(list);

		return new MultiParam() {
			@Override
			public String name() {
				return name;
			}
			@Override
			public List<ParamValue> values() {
				return vals;
			}
		};
	}

}
