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

import static org.assertj.core.api.Assertions.assertThat;
import static io.jenetics.facilejdbc.Dctor.field;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class RecordsTest {

	@Test(dataProvider = "snakeCaseStrings")
	public void toSnakeCase(final String component, final String column) {
		final String snake_case = Records.toSnakeCase(component);

		assertThat(snake_case).isEqualTo(column);
	}

	@DataProvider
	public Object[][] snakeCaseStrings() {
		return new Object[][] {
			{"", ""},
			{"a", "a"},
			{"simple", "simple"},
			{"simpleName", "simple_name"},
			{"SimpleName", "simple_name"},
			{"simple_name", "simple_name"},
			{"Simple_Name", "simple_name"},
			{"Simple___Name", "simple___name"},
			{"simple___Name", "simple___name"},
			{"_Simple_Name", "_simple_name"},
			{"___Simple_Name", "___simple_name"},
			{"IOError", "i_o_error"},
			{"IoError", "io_error"},
			{"ioError", "io_error"}
		};
	}

	record Book(
		String title,
		String author,
		String isbn,
		int pages,
		LocalDate publishedAt
	){}

	private static final Book BOOK = new Book(
		"Auf der Suche nach der verlorenen Zeit",
		"Marcel Proust",
		"978-3150300701",
		4325,
		LocalDate.parse("2020-11-13")
	);

	@Test
	public void dctor() throws SQLException {
		final List<String> bookColumnNames = List.of(
			"title",
			"author",
			"isbn",
			"pages",
			"published_at"
		);
		final Dctor<Book> dctor = Records.dctor(Book.class);

		final var stmt = new MockPreparedStatement();
		dctor.unapply(BOOK, null).set(bookColumnNames, stmt);

		assertThat(stmt.get(1)).isEqualTo(BOOK.title());
		assertThat(stmt.get(2)).isEqualTo(BOOK.author());
		assertThat(stmt.get(3)).isEqualTo(BOOK.isbn());
		assertThat(stmt.get(4)).isEqualTo(BOOK.pages());
		assertThat(stmt.get(5)).isEqualTo(BOOK.publishedAt());
	}

	@Test
	public void dctor2() throws SQLException {
		final List<String> bookColumnNames = List.of(
			"title",
			"primary_author",
			"isbn13",
			"pages",
			"published_at"
		);
		final Dctor<Book> dctor = Records.dctor(
			Book.class,
			component -> switch (component.getName()) {
				case "author" -> "primary_author";
				case "isbn" -> "isbn13";
				default -> Records.toSnakeCase(component);
			}
		);

		final var stmt = new MockPreparedStatement();
		dctor.unapply(BOOK, null).set(bookColumnNames, stmt);

		assertThat(stmt.get(1)).isEqualTo(BOOK.title());
		assertThat(stmt.get(2)).isEqualTo(BOOK.author());
		assertThat(stmt.get(3)).isEqualTo(BOOK.isbn());
		assertThat(stmt.get(4)).isEqualTo(BOOK.pages());
		assertThat(stmt.get(5)).isEqualTo(BOOK.publishedAt());
	}

	@Test
	public void dctor3() throws SQLException {
		final List<String> bookColumnNames = List.of(
			"title",
			"author",
			"isbn",
			"pages",
			"published_at",
			"title_hash"
		);
		final Dctor<Book> dctor = Records.dctor(
			Book.class,
			field("title_hash", book -> book.title().hashCode())
		);

		final var stmt = new MockPreparedStatement();
		dctor.unapply(BOOK, null).set(bookColumnNames, stmt);

		assertThat(stmt.get(1)).isEqualTo(BOOK.title());
		assertThat(stmt.get(2)).isEqualTo(BOOK.author());
		assertThat(stmt.get(3)).isEqualTo(BOOK.isbn());
		assertThat(stmt.get(4)).isEqualTo(BOOK.pages());
		assertThat(stmt.get(5)).isEqualTo(BOOK.publishedAt());
		assertThat(stmt.get(6)).isEqualTo(BOOK.title().hashCode());
	}

	@Test
	public void dctor4() throws SQLException {
		final List<String> bookColumnNames = List.of(
			"title",
			"author",
			"isbn",
			"pages",
			"published_at",
			"title_hash"
		);
		final Dctor<Book> dctor = Records.dctor(
			Book.class,
			field("pages", book -> book.pages()*3),
			field("title_hash", book -> book.title().hashCode())
		);

		final var stmt = new MockPreparedStatement();
		dctor.unapply(BOOK, null).set(bookColumnNames, stmt);

		assertThat(stmt.get(1)).isEqualTo(BOOK.title());
		assertThat(stmt.get(2)).isEqualTo(BOOK.author());
		assertThat(stmt.get(3)).isEqualTo(BOOK.isbn());
		assertThat(stmt.get(4)).isEqualTo(BOOK.pages()*3);
		assertThat(stmt.get(5)).isEqualTo(BOOK.publishedAt());
		assertThat(stmt.get(6)).isEqualTo(BOOK.title().hashCode());
	}

}
