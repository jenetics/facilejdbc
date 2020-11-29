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
import static io.jenetics.facilejdbc.Param.lazyValues;
import static io.jenetics.facilejdbc.Param.value;
import static io.jenetics.facilejdbc.Param.values;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class QueryTest {

	@Test
	public void create() {
		final Query query = Query
			.of("SELECT * FROM table WHERE id = :id AND name = :name");

		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM table WHERE id = ? AND name = ?"
		);
		Assert.assertEquals(query.paramNames(), asList("id", "name"));
	}

	@Test
	public void paramSql() {
		final String sql = "SELECT * FROM table WHERE id = :id AND name = :name";
		final Query query = Query.of(sql);
		Assert.assertEquals(query.rawSql(), sql);
	}

	@Test
	public void singleParam() throws SQLException {
		final var query = Query.of(
			"SELECT * FROM table WHERE id IN(:ids) " +
				"AND name LIKE :name OR key IN(:keys);"
		);

		final var conn = new MockConnection();
		query
			.on(
				values("ids", 10, 20, 30, 40),
				value("name", "some_name"),
				values("keys", "k1", "k2"))
			.execute(conn);

		System.out.println(conn.stmt.data);
	}

	@Test
	public void multiSelect() throws SQLException {
		final var query = Query.of("SELECT * FROM book WHERE id IN(:ids);")
			.on(values("ids", 1, 2, 3, 4));

		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids[0],:ids[1],:ids[2],:ids[3]);"
		);
		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?,?,?,?);"
		);

		final var conn = new MockConnection();
		query.execute(conn);
		Assert.assertEquals(conn.stmt.data, Map.of(1, 1, 2, 2, 3, 3, 4, 4));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void multiSelectZero() {
		Query.of("SELECT * FROM book WHERE id IN(:ids);")
			.on(values("ids"));
	}

	@Test
	public void multiSelectOne() throws SQLException {
		final var query = Query.of("SELECT * FROM book WHERE id IN(:ids);")
			.on(values("ids", 1));

		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids[0]);"
		);
		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?);"
		);

		final var conn = new MockConnection();
		query.execute(conn);
		Assert.assertEquals(conn.stmt.data, Map.of(1, 1));
	}

	@Test
	public void lazyMultiSelect() throws SQLException {
		final var query = Query.of("SELECT * FROM book WHERE id IN(:ids);")
			.on(lazyValues("ids", () -> 1, () -> 2, () -> 3, () -> 4));

		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids[0],:ids[1],:ids[2],:ids[3]);"
		);
		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?,?,?,?);"
		);

		final var conn = new MockConnection();
		query.execute(conn);
		Assert.assertEquals(conn.stmt.data, Map.of(1, 1, 2, 2, 3, 3, 4, 4));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void lazyMultiSelectZero() {
		Query.of("SELECT * FROM book WHERE id IN(:ids);")
			.on(lazyValues("ids"));
	}

	@Test
	public void lazyMultiSelectOne() throws SQLException {
		final var query = Query.of("SELECT * FROM book WHERE id IN(:ids);")
			.on(lazyValues("ids", () -> 1));

		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids[0]);"
		);
		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?);"
		);

		final var conn = new MockConnection();
		query.execute(conn);
		Assert.assertEquals(conn.stmt.data, Map.of(1, 1));
	}

	@Test
	public void multipleMultiples() {
		var query = Query
			.of("SELECT * FROM book WHERE id IN(:ids) AND name LIKE :name;");

		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?) AND name LIKE ?;"
		);
		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids) AND name LIKE :name;"
		);

		query = query.on(values("ids", 1, 2, 3));
		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?,?,?) AND name LIKE ?;"
		);
		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids[0],:ids[1],:ids[2]) AND name LIKE :name;"
		);

		query = query.on(values("ids[1]", 1, 2, 3));
		Assert.assertEquals(
			query.sql(),
			"SELECT * FROM book WHERE id IN(?,?,?,?,?) AND name LIKE ?;"
		);
		Assert.assertEquals(
			query.rawSql(),
			"SELECT * FROM book WHERE id IN(:ids[0],:ids[1][0],:ids[1][1],:ids[1][2],:ids[2]) AND name LIKE :name;"
		);
	}

	@Test
	public void serialize() throws IOException, ClassNotFoundException {
		final Query query = Query
			.of("SELECT * FROM table WHERE id = :id AND name = :name");

		final var bout = new ByteArrayOutputStream();
		final var oout = new ObjectOutputStream(bout);

		oout.writeObject(query);
		final byte[] bytes = bout.toByteArray();

		final var bin = new ByteArrayInputStream(bytes);
		final var oin = new ObjectInputStream(bin);

		Assert.assertEquals(
			oin.readObject().toString(),
			query.toString()
		);
	}

}
