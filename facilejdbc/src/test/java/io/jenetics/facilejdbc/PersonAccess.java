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

	private static final Query INSERT_QUERY = Query.of(
		"INSERT INTO person(name, email, link) " +
		"VALUES({name}, {email}, {link});"
	);

	private static final Query SELECT_QUERY = Query.of(
		"SELECT name, email, link FROM person " +
		"WHERE name = :name"
	);

//	private static final Dctor<Person> DCTOR = Dctor.of(
//		Field.of("name", Person::getName),
//		Field.of("email", p -> p.getEmail().map(Email::getAddress)),
//		Field.of("link_id", (p, c) -> LinkAccess.insert(p.getLink().orElse(null), c))
//	);
//
//	public static Long insert(final Person person, final Connection conn)
//		throws SQLException
//	{
//		return person != null && !person.isEmpty()
//			? INSERT_QUERY.insert(person, DCTOR, conn)
//			: null;
//	}

}