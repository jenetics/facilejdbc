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
package io.jenetics.facilejdbc.testdb;

import static java.util.Objects.requireNonNull;
import static io.jenetics.facilejdbc.Param.value;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.RowParser;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public final record Author(String name, LocalDate birthDay) {
	public Author {
		requireNonNull(name);
	}

	/* *************************************************************************
	 * DB access
	 * ************************************************************************/

	private static final RowParser<Author> PARSER = RowParser.of(Author.class);

	private static final Dctor<Author> DCTOR = Dctor.of(Author.class);

	private static final Query INSERT = Query.of("""
		INSERT INTO author(name, birth_day)
		VALUES(:name, :birth_day);
		"""
	);

	private static final Query SELECT_ID_BY_NAME = Query.of(
		"SELECT id FROM author WHERE name = :name"
	);

	private static final Query SELECT_BY_ID = Query.of(
		"SELECT name, birth_day FROM author WHERE id = :id"
	);

	private static final Query SELECT_BY_BOOK_ID = Query.of("""
		SELECT name, birth_day
		FROM author
		INNER JOIN book_author ON book_author.author_id = author.id
		WHERE book_id = :book_id
		"""
	);

	/**
	 * Inserts the given author into the DB. Or just return the id of the author
	 * already inserted into the DB.
	 *
	 * @param author the book to insert
	 * @param conn the DB connection
	 * @return the primary key of the author
	 * @throws SQLException if a DB error occurs
	 */
	public static long insert(final Author author, final Connection conn)
		throws SQLException
	{
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

	/**
	 * Select the author by the DB id.
	 *
	 * @param id the primary key
	 * @param conn the DB connection
	 * @return the selected author
	 * @throws SQLException if a DB error occurs
	 */
	public static Optional<Author> selectById(final long id, final Connection conn)
		throws SQLException
	{
		return SELECT_BY_ID
			.on(value("id", id))
			.as(PARSER.singleOpt(), conn);
	}

	/**
	 * Selects the authors from a given book ID.
	 *
	 * @param id the book id
	 * @param conn the DB connection
	 * @return the list of authors for the given book ID
	 * @throws SQLException if a DB error occurs
	 */
	public static List<Author> selectByBookId(final long id, final Connection conn)
		throws SQLException
	{
		return SELECT_BY_BOOK_ID
			.on(value("book_id", id))
			.as(PARSER.list(), conn);
	}

}
