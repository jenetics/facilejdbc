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
package io.jenetics.facilejdbc.util;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public final class H2DB {

	public static final H2DB INSTANCE = new H2DB("jdbc:h2:mem:testdb-gpx;MODE=MySQL");

	private final DataSource _dataSource;

	private Connection _connection;

	private H2DB(final DataSource dataSource) {
		_dataSource = requireNonNull(dataSource);
	}

	private H2DB(final String url) {
		this(ds(url));
	}

	private static DataSource ds(final String url) {
		final JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(url);
		return ds;
	}

	public DataSource ds() {
		return _dataSource;
	}

	public Connection conn() throws SQLException  {
		if (_connection == null) {
			_connection = ds().getConnection();
		}
		return _connection;
	}

}
