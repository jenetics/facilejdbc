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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.jenetics.facilejdbc.Param.Value;
import io.jenetics.facilejdbc.function.SqlFunction3;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class PreparedQuery extends Query {

	private final Map<String, Param> _params;

	private PreparedQuery(
		final Sql sql,
		final Map<String, Param> params
	) {
		super(sql);
		_params = params;
	}

	@Override
	public PreparedQuery on(final Param... params) {
		if (params.length == 0) return this;

		final Map<String, Param> map = new HashMap<>(_params);
		for (Param param : params) {
			map.put(param.name(), param);
		}

		return new PreparedQuery(sql(), map);
	}

	private void fill(final PreparedStatement stmt) throws SQLException {
		int index = 1;
		for (String name : sql().paramNames()) {
			if (_params.containsKey(name)) {
				_params.get(name).value().set(stmt, index);
			}

			++index;
		}
	}

	public <T> void insert(
		final Iterable<T> rows,
		final Function<? super T, ? extends Preparer> f,
		final Connection conn
	)
		throws SQLException
	{
		try (PreparedStatement stmt = prepare(conn)) {
			for (T row : rows) {
				f.apply(row).prepare(stmt);
				stmt.executeUpdate();
			}
		}
	}

	@Override
	public <T> void inserts(
		final Collection<T> rows,
		final SqlFunction3<? super T, String, Connection, Value> dctor,
		final Connection conn
	)
		throws SQLException
	{
		try (PreparedStatement stmt = prepare(conn)) {
			fill(stmt);

			for (T row : rows) {
				int index = 0;
				for (String name : sql().paramNames()) {
					final Value value = dctor.apply(row, name, conn);
					if (value != null) {
						value.set(stmt, ++index);
					} else if (_params.containsKey(name)) {
						_params.get(name).value().set(stmt, ++index);
					} else {
						throw new NoSuchElementException();
					}
				}

				stmt.executeUpdate();
			}
		}
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	static PreparedQuery of(final Query query, final Param... params) {
		final Map<String, Param> map = Stream.of(params)
			.collect(Collectors.toMap(
				Param::name,
				Function.identity(),
				(a, b) -> b));

		return new PreparedQuery(query.sql(), map);
	}

}
