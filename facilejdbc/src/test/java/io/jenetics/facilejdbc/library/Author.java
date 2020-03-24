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

import static java.util.Objects.requireNonNull;
import static io.jenetics.facilejdbc.Dctor.field;
import static io.jenetics.facilejdbc.Param.value;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.RowParser;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public final class Author {

	private final String name;
	private final LocalDate birthDay;

	public Author(final String name, final LocalDate birthDay) {
		this.name = requireNonNull(name);
		this.birthDay = birthDay;
	}

	public String name() {
		return name;
	}

	public LocalDate birthDay() {
		return birthDay;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, birthDay);
	}

	@Override
	public boolean equals(final Object other) {
		return other == this ||
			other instanceof Author &&
			Objects.equals(name, ((Author) other).name) &&
			Objects.equals(birthDay, ((Author) other).birthDay);
	}


	/* *************************************************************************
	 * DB access
	 * ************************************************************************/

	public static final RowParser<Author> PARSER = (row, conn) -> new Author(
		row.getString("name"),
		row.getDate("birth_day").toLocalDate()
	);

	public static final Dctor<Author> DCTOR = Dctor.of(
		field("forename", Author::name),
		field("birth_day", Author::birthDay)
	);

	private static final Query INSERT = Query.of(
		"INSERT INTO author(name, birth_day) " +
		"VALUES(:name, :birth_day);"
	);

	private static final Query SELECT_ID_BY_NAME = Query.of(
		"SELECT id FROM author WHERE name = :name"
	);

	private static final Query SELECT_BY_ID = Query.of(
		"SELECT name, birth_day FROM author WHERE id = :id"
	);

	private static final Query SELECT_BY_BOOK_ID = Query.of(
		"SELECT name, birth_day " +
		"FROM author " +
		"INNER JOIN book_author ON book_author.author_id = author.id " +
		"WHERE book_id = :book_id"
	);

	public static Long insert(final Author author, final Connection conn)
		throws SQLException
	{
		if (author == null) return null;

		Long authorId = SELECT_ID_BY_NAME
			.on(value("name", author.name()))
			.as(RowParser.int64("id").singleNull(), conn);

		if (authorId == null) {
			authorId = INSERT
				.on(author, DCTOR)
				.executeInsert(conn)
				.orElseThrow();
		}

		return authorId;
	}

	public static Author selectById(final long id, final Connection conn)
		throws SQLException
	{
		return SELECT_BY_ID
			.on(value("id", id))
			.as(PARSER.singleNull(), conn);
	}

	public static List<Author> selectByBookId(final long id, final Connection conn)
		throws SQLException
	{
		return SELECT_BY_BOOK_ID
			.on(value("book_id", id))
			.as(PARSER.list(), conn);
	}

}
