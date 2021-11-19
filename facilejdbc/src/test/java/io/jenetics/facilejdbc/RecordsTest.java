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
import java.util.Map;

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

	@Test
	public void dctor() throws SQLException {
		record Book(
			String title,
			String author,
			String isbn,
			int pages,
			LocalDate publishedAt
		) {}

		/*
		final Dctor<Book> dctor = Records.dctor(
			Book.class,
			Records::toSnakeCase,
			Map.of("publishedAt", "published_at2"),
			List.of(field("published_at2", Book::publishedAt))
		);
		 */

		final Dctor<Book> dctor = Records.dctor(
			Book.class,
			component -> switch (component.getName()) {
				case "publishedAt" -> "published_at2";
				default -> Records.toSnakeCase(component);
			},
			List.of(
				field("published_at2", Book::publishedAt),
				field("id", book -> book.isbn() + ":" + book.author())
			)
		);

		Records.dctor(
			Book.class,
			component -> switch (component.getName()) {
				case "title" -> "title";
				case "author" -> "author";
				case "isbn" -> "isbn";
				case "pages" -> "pages";
				case "publishedAt" -> "published_at2";
				default -> throw new IllegalStateException();
			},
			List.of(field("published_at2", Book::publishedAt))
		);

		final var book = new Book(
			"Auf der Suche nach der verlorenen Zeit",
			"Marcel Proust",
			"978-3150300701",
			4325,
			LocalDate.parse("2020-11-13")
		);

		final ParamValues values = dctor.unapply(book, null);

		final var stmt = new MockPreparedStatement();
		values.set(List.of("title", "author", "isbn", "pages", "published_at2", "id"), stmt);

		assertThat(stmt.get(1)).isEqualTo(book.title());
		assertThat(stmt.get(2)).isEqualTo(book.author());
		assertThat(stmt.get(3)).isEqualTo(book.isbn());
		assertThat(stmt.get(4)).isEqualTo(book.pages());
		assertThat(stmt.get(5)).isEqualTo(book.publishedAt());
		System.out.println(stmt.get(6));
	}

}
