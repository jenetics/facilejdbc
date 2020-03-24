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

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class PersonAccess {
	private PersonAccess() {}

//	@Value
//	@Builder(builderClassName = "Builder", toBuilder = true)
//	@Accessors(fluent = true)
//	public static final class Person {
//		private final String name;
//		private final String email;
//		private final Link link;
//	}
//
//	@Value
//	@Builder(builderClassName = "Builder", toBuilder = true)
//	@Accessors(fluent = true)
//	public static final class Link {
//		private final String name;
//		private final URI url;
//	}
//
//	private static final RowParser<Link> LINK_PARSER = (row, conn) -> new Link(
//		row.getString("name"),
//		URI.create(row.getString("url"))
//	);
//
//	private static final Dctor<Link> LINK_DCTOR = Dctor.of(
//		field("name", Link::name),
//		field("url", l -> l.url.toString())
//	);
//
//	private static Link selectLink(final Long linkId, final Connection conn)
//		throws SQLException
//	{
//		return Query.of("SELECT * FROM link WHERE id = :id")
//			.on(value("id", linkId))
//			.as(LINK_PARSER.singleNull(), conn);
//	}
//
//	private static Long insertLink(final Link link, final Connection conn)
//		throws SQLException
//	{
//		return Query.of("INSERT INTO link(name, url) VALUES(:name, :url")
//			.on(link, LINK_DCTOR)
//			.executeInsert(conn)
//			.orElseThrow();
//	}
//
//	private static final RowParser<Person> PERSON_PARSER = (row, conn) -> new Person(
//		row.getString("name"),
//		row.getString("email"),
//		selectLink(row.getLong("link_id"), conn)
//	);
//
//
//
//	private static final Dctor<Person> PERSON_DCTOR = Dctor.of(
//		field("name", Person::name),
//		field("email", Person::email),
//		field("link_id", (p, c) -> insertLink(p.link(), c))
//	);
//
//
//
//	private static final Query INSERT_PERSON = Query.of(
//		"INSERT INTO person(name, email, link) " +
//		"VALUES(:name, :email, :link);"
//	);
//
//	private static final Query SELECT_PERSON = Query.of(
//		"SELECT name, email, link " +
//		"FROM person " +
//		"WHERE name = :name"
//	);
//
//	public static void main(final String[] args) throws SQLException {
//		final DataSource ds = null;
//
//		// SELECT
//		final List<Person> persons = transaction(ds, conn ->
//			SELECT_PERSON
//				.on(value("name", "Franz"))
//				.as(PERSON_PARSER.list(), conn)
//		);
//
//		// INSERT
//		final boolean inserted = transaction(ds, conn ->
//			INSERT_PERSON
//				.on(
//					value("name", "foo"),
//					value("email", "foo@gmail.com"),
//					value("link", "http://google.com"))
//				.execute(conn)
//		);
//
//		final Optional<Integer> pk = transaction(ds, conn ->
//			INSERT_PERSON
//				.on(
//					value("name", "foo"),
//					value("email", "foo@gmail.com"),
//					value("link", "http://google.com"))
//				.executeInsert(RowParser.int32(1), conn)
//		);
//
//
//		// BATCH execution
//		final Batch batch = Batch.of(persons, PERSON_DCTOR);
//		final int[] counts = transaction(ds, conn ->
//			INSERT_PERSON.executeUpdate(batch, conn)
//		);
//
//	}

}
