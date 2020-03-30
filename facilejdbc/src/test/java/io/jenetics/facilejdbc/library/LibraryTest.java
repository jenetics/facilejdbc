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
package io.jenetics.facilejdbc.library;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.jenetics.facilejdbc.util.Queries;
import io.jenetics.facilejdbc.util.Transactional;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class LibraryTest {

	private final Transactional db = () -> DriverManager.getConnection(
		"jdbc:hsqldb:mem:testdb",
		"SA",
		""
	);

	private static final List<Book> BOOKS = List.of(
		new Book(
			"Auf der Suche nach der verlorenen Zeit",
			"978-3518061756",
			5100,
			List.of(
				new Author(
					"Marcel Proust",
					LocalDate.parse("1922-11-08")
				)
			)
		),
		new Book(
			"Database Design for Mere Mortals",
			"978-0321884497",
			654,
			List.of(
				new Author(
					"Michael J. Hernandez",
					null
				)
			)
		),
		new Book(
			"Der alte Mann und das Meer",
			"B00JM4RD2S",
			142,
			List.of(
				new Author(
					"Ernest Hemingway",
					LocalDate.parse("1899-07-21")
				)
			)
		)
	);


	@BeforeClass
	public void setup() throws IOException, SQLException {
		final var queries = Queries.read(
			getClass().getResourceAsStream("/library-hsqldb.sql")
		);

		db.transaction().apply(conn -> {
			for (var query : queries) {
				query.execute(conn);
			}
			return null;
		});
	}

	@Test
	public void insert() throws SQLException {
		final long id = db.transaction().apply(conn ->
			Book.insert(BOOKS.get(0), conn)
		);

		Assert.assertTrue(id >= 0);
	}

	@Test(dependsOnMethods = "insert")
	public void select() throws SQLException {
		final List<Book> books = db.transaction().apply(conn ->
			Book.selectByTitle(BOOKS.get(0).title(), conn)
		);

		Assert.assertEquals(books.size(), 1);
		Assert.assertEquals(books.get(0), BOOKS.get(0));
	}

	@Test(dependsOnMethods = "select")
	public void insertAndSelectAuthor() throws SQLException {
		final long id = db.transaction().apply(conn ->
			Author.insert(BOOKS.get(1).authors().get(0), conn)
		);

		Assert.assertTrue(id >= 0);

		final Optional<Author> author = db.transaction().apply(conn ->
			Author.selectById(id, conn)
		);
		Assert.assertTrue(author.isPresent());

		Assert.assertEquals(
			author.orElseThrow(),
			BOOKS.get(1).authors().get(0)
		);
	}

	@Test(dependsOnMethods = "insertAndSelectAuthor")
	public void insertRestOfBooks() throws SQLException {
		db.transaction().accept(conn -> {
			for (int i = 1; i < BOOKS.size(); ++i) {
				Book.insert(BOOKS.get(i), conn);
			}
		});
	}

	@Test(dependsOnMethods = "insertRestOfBooks")
	public void selectAll() throws SQLException {
		final Set<Book> books = db.transaction().apply(Book::selectAll);
		Assert.assertEquals(
			books,
			Set.copyOf(BOOKS)
		);
	}

}
