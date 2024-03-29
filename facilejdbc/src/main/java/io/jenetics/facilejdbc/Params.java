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
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects a list of {@link SingleParam} object into a {@link ParamValues} object.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
final class Params implements ParamValues {

	private final Map<String, SingleParam> params;

	private Params(final Map<String, SingleParam> params) {
		this.params = requireNonNull(params);
	}

	Params(final List<? extends SingleParam> params) {
		this(
			params.isEmpty()
				? Map.of()
				: params.stream().collect(
					groupingBy(SingleParam::name, reducing(null, (a, b) -> b)))
		);
	}

	@Override
	public void set(final List<String> paramNames, final PreparedStatement stmt)
		throws SQLException
	{
		int index = 0;
		for (String name : paramNames) {
			++index;
			final SingleParam param = params.get(name);
			if (param != null) {
				param.value().set(index, stmt);
			}
		}
	}

	@Override
	public ParamValues andThen(final ParamValues after) {
		return after instanceof Params p
			? andThen(p)
			: ParamValues.super.andThen(after);
	}

	private Params andThen(final Params after) {
		final Map<String, SingleParam> params = new HashMap<>(this.params);
		params.putAll(after.params);
		return new Params(params);
	}

}
