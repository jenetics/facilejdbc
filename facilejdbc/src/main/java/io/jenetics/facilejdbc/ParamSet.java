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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public final class ParamSet implements Preparer {

	private final List<Param> _params;

	ParamSet(final List<Param> params) {
		_params = requireNonNull(params);
	}

	@Override
	public void prepare(final PreparedStatement stmt) throws SQLException {
	}

}
