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

import static io.jenetics.facilejdbc.Param.value;
import static io.jenetics.facilejdbc.util.HSQLDB.transaction;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.jenetics.facilejdbc.testmodel.Person;
import io.jenetics.facilejdbc.util.IO;

public class QueryExecutionTest {

	private static final Query SELECT = Query.of(
		"SELECT * FROM person " +
		"WHERE forename like :forename " +
		"ORDER BY surname;"
	);

	private static final Query INSERT = Query.of(
		"INSERT INTO person(forename, surname, birthday, email) " +
		"VALUES(:forename, :surname, :birthday, :email);"
	);

	@BeforeClass
	public void setup() throws IOException, SQLException {
		final String[] sqls = IO.
			toSQLText(getClass().getResourceAsStream("/testmodel.sql"))
			.split(";");

		transaction(conn -> {
			for (String sql : sqls) {
				if (!sql.isBlank()) {
					Query.of(sql).execute(conn);
				}
			}
			return null;
		});

	}

	@Test
	public void insertSelect() throws SQLException {
		final Person person = Person.builder()
			.forename("Franz")
			.surname("Wilhelmstötter")
			.birthday(LocalDate.now())
			.email("franz.wilhelmstoetter@gmail.com")
			.build();

		final Optional<Long> id = transaction(conn ->
			INSERT
				.on(person, Person.DCTOR)
				.executeInsert(conn)
		);
		Assert.assertTrue(id.isPresent());

		Person selected = transaction(conn ->
		 	SELECT
				.on(value("forename", "Franz"))
				.as(Person.PARSER.single(), conn)
		);
		Assert.assertEquals(selected, person);

		selected = transaction(conn ->
			Query.of("SELECT * FROM person WHERE id = :id")
				.on(value("id", id.orElseThrow()))
				.as(Person.PARSER.single(), conn)
		);
		Assert.assertEquals(selected, person);
	}

	@Test
	public void sameParamName() throws SQLException {
		final Query insert = Query.of(
			"INSERT INTO person(forename, surname, birthday, email) " +
			"VALUES(:forename, :forename, :birthday, :email);"
		);

		transaction(conn ->
			insert
				.on(
					value("forename", "Werner"),
					value("birthday", LocalDate.now()),
					value("email", "some.email@gmail.com"))
				.execute(conn)
		);

		final Person selected = transaction(conn ->
			SELECT
				.on(value("forename", "Werner"))
				.as(Person.PARSER.single(), conn)
		);

		Assert.assertEquals(selected.forename(), "Werner");
		Assert.assertEquals(selected.surname(), "Werner");
	}

	@Test
	public void batchInsert() throws SQLException {
		final Set<Person> persons = IntStream.range(0, 20)
			.mapToObj(QueryExecutionTest::person)
			.collect(Collectors.toSet());

		final Batch batch = Batch.of(persons, Person.DCTOR);
		final int[] counts = transaction(conn ->
			INSERT.executeUpdate(batch, conn)
		);
		final int[] expected = new int[persons.size()];
		Arrays.fill(expected, 1);
		Assert.assertEquals(counts, expected);

		final List<Person> selected = transaction(conn ->
			SELECT
				.on(value("forename", "Forename-%"))
				.as(Person.PARSER.list(), conn)
		);

		Assert.assertEquals(new HashSet<>(selected), persons);
	}

	private static Person person(final int index) {
		final LocalDate birthday = LocalDate.now();
		return Person.builder()
			.forename("Forename-" + index)
			.surname("Surname-" + index)
			.birthday(birthday.plusDays(index))
			.email("email@gmail.com" + index)
			.build();
	}

}
