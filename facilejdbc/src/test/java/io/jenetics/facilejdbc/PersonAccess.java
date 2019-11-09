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

import static io.jenetics.facilejdbc.Dctor.field;
import static io.jenetics.facilejdbc.Param.value;
import static io.jenetics.facilejdbc.util.Db.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class PersonAccess {
	private PersonAccess() {}

	public static final class Person {
		private final String _name;
		private final String _email;
		private final String _link;

		public Person(final String name, final String email, final String link) {
			_name = name;
			_email = email;
			_link = link;
		}

		public String name() {
			return _name;
		}

		public String email() {
			return _email;
		}

		public String link() {
			return _link;
		}

	}

	private static final RowParser<Person> PARSER = (row, conn) -> new Person(
		row.getString("name"),
		row.getString("email"),
		row.getString("link")
	);

	private static final Dctor<Person> DCTOR = Dctor.of(
		field("name", Person::name),
		field("email", Person::email),
		field("link_id", (p, c) -> insertLink(p.link(), c))
	);

	private static final Query INSERT_PERSON = Query.of(
		"INSERT INTO person(name, email, link) " +
		"VALUES(:name, :email, :link);"
	);

	private static final Query SELECT_PERSON = Query.of(
		"SELECT name, email, link " +
		"FROM person " +
		"WHERE name = :name"
	);

	public static void main(final String[] args) throws SQLException {
		final DataSource ds = null;

		// SELECT
		final List<Person> persons = transaction(ds, conn ->
			SELECT_PERSON
				.on(value("name", "Franz"))
				.as(PARSER.list(), conn)
		);

		// INSERT
		final boolean inserted = transaction(ds, conn ->
			INSERT_PERSON
				.on(
					value("name", "foo"),
					value("email", "foo@gmail.com"),
					value("link", "http://google.com"))
				.execute(conn)
		);

		// BATCH execution
		final Batch batch = Batch.of(persons, DCTOR);
		final int[] counts = transaction(ds, conn ->
			INSERT_PERSON.executeUpdate(batch, conn)
		);

	}




	static Long insertLink(final String link, final Connection conn)
		throws SQLException
	{
		// Doing some sub-inserts.
		return null;
	}

}
