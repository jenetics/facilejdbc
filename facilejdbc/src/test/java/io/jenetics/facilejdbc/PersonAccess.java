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

import static io.jenetics.facilejdbc.Db.transaction;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import io.jenetics.facilejdbc.Dctor.Field;

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

	private static final RowParser<Person> PARSER = row -> new Person(
		row.getString("name"),
		row.getString("email"),
		row.getString("link")
	);

	private static final Query INSERT_QUERY = Query.of(
		"INSERT INTO person(name, email, link) " +
		"VALUES({name}, {email}, {link});"
	);

	private static final Query SELECT_QUERY = Query.of(
		"SELECT name, email, link " +
		"FROM person " +
		"WHERE name = :name"
	);

	public static void main(final String[] args) throws SQLException {
		final DataSource ds = null;

		final List<Person> persons = transaction(ds, conn ->
			SELECT_QUERY
				.on(Param.of("name", "Franz"))
				.as(PARSER.list(), conn)
		);
//
//		SELECT_QUERY
//			.with(ds.getConnection())
//			.select();

		INSERT_QUERY
			.on(
				Param.of("name", "foo"),
				Param.of("email", "foo@gmail.com"),
				Param.of("link", "http://google.com"))
			.execute(ds.getConnection());

//		INSERT_QUERY
//			.with(ds.getConnection())
//			.insert(persons, DCTOR);
	}

	private static final Dctor<Person> DCTOR = Dctor.of(
		Field.of("name", Person::name),
		Field.of("email", Person::email),
		Field.of("link", Person::email)
		//Field.of("link_id", (p, c) -> LinkAccess.insert(p.getLink().orElse(null), c))
	);
//
//	public static Long insert(final Person person, final Connection conn)
//		throws SQLException
//	{
//		return person != null && !person.isEmpty()
//			? INSERT_QUERY.insert(person, DCTOR, conn)
//			: null;
//	}

}
