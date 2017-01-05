/*
 * Java GPX Library (@__identifier__@).
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
package io.jenetics.jpx.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a select SQL query.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public final class SQLQuery extends AbstractQuery {
	private final List<Param> _params = new ArrayList<>();

	public SQLQuery(final Connection conn, final PreparedQuery query) {
		super(conn, query);
	}

	public SQLQuery(final Connection conn, final String query) {
		super(conn, query);
	}

	public SQLQuery on(final String name, final Object value) {
		_params.add(Param.value(name, value));
		return this;
	}

	public <T> T as(final RowParser<T> parser) throws SQLException {
		for (Param param : _params) {
			param.eval();
		}

		try (PreparedStatement stmt = _conn.prepareStatement(_query.getQuery())) {
			_query.fill(stmt, _params);
			try (final ResultSet rs = stmt.executeQuery()) {
				return parser.parse(rs);
			}
		}
	}

}
