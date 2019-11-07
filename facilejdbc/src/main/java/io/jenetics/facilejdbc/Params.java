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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Collects a list of {@link Param} object into a {@link ParamValues} object.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class Params implements ParamValues {

	private final List<Param> _params;

	Params(final List<Param> params) {
		_params = requireNonNull(params);
	}

	@Override
	public void set(final List<String> paramNames, final PreparedStatement stmt)
		throws SQLException
	{
		int index = 0;
		for (String name : paramNames) {
			++index;
			final Param param = get(name);
			if (param != null) {
				param.value().set(index, stmt);
			}
		}
	}

	private Param get(final String name) {
		for (Param param : _params) {
			if (param.name().equals(name)) {
				return param;
			}
		}
		return null;
	}

}
