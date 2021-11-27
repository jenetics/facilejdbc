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
import static io.jenetics.facilejdbc.Dctor.fieldValue;
import static io.jenetics.facilejdbc.Param.value;
import static io.jenetics.facilejdbc.RowParser.int64;
import static io.jenetics.facilejdbc.RowParser.string;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.jenetics.facilejdbc.Batch;
import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Records;
import io.jenetics.facilejdbc.RowParser;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public final record Book(
	String title,
	Isbn isbn,
	Integer pages,
	LocalDate publishedAt,
	String language,
	List<Author> authors
) {

	public Book {
		requireNonNull(title);
		authors = List.copyOf(authors);
	}

	/* *************************************************************************
	 * DB access
	 * ************************************************************************/

	static final RowParser<Book> PARSER = Records.parserWithFields(
		Book.class,
		Map.of(
			"isbn", string("isbn").map(Isbn::new),
			"authors", int64("id").map(Author::selectByBookId)
		)
	);

	private static final Dctor<Book> DCTOR = Dctor.of(
		Book.class,
		field("isbn", book -> book.isbn().value())
	);

	private static final Query INSERT= Query.of("""
		INSERT INTO book(title, isbn, language, published_at, pages)
		VALUES(:title, :isbn, :language, :published_at, :pages);
		"""
	);

	private static final Query INSERT_BOOK_AUTHOR = Query.of("""
		INSERT INTO book_author(book_id, author_id)
		VALUES(:book_id, :author_id);
		"""
	);

	private static final Query SELECT_BY_TITLE = Query.of("""
		SELECT id, title, isbn, language, published_at, pages
		FROM book WHERE LCASE(title) like :title
		"""
	);

	/**
	 * Inserts the given book into the DB.
	 *
	 * @param book the book to insert
	 * @param conn the DB connection
	 * @return the primary key of the newly inserted book
	 * @throws SQLException if a DB error occurs
	 */
	static long insert(final Book book, final Connection conn)
		throws SQLException
	{
		final Long id = INSERT
			.on(book, DCTOR)
			.executeInsert(conn)
			.orElseThrow();

		insertAuthors(id, book.authors(), conn);
		return id;
	}

	private static void insertAuthors(
		final Long bookId,
		final List<Author> authors,
		final Connection conn
	)
		throws SQLException
	{
		final Batch batch = Batch.of(
			authors,
			Dctor.of(
				fieldValue("book_id", bookId),
				field("author_id", Author::insert)
			)
		);

		INSERT_BOOK_AUTHOR.executeUpdate(batch, conn);
	}

	/**
	 * Selects all books from the DB.
	 *
	 * @param conn the DB connection
	 * @return all books in the DB
	 * @throws SQLException if a DB error occurs
	 */
	public static Set<Book> selectAll(final Connection conn) throws SQLException {
		return Query
			.of("SELECT * FROM book;")
			.as(PARSER.unmodifiableSet(), conn);
	}

	/**
	 * Selects books by its title.
	 *
	 * @param title the title pattern to select
	 * @param conn the DB connection
	 * @return the found books
	 * @throws SQLException if a DB error occurs
	 */
	public static List<Book>
	selectByTitle(final String title, final Connection conn)
		throws SQLException
	{
		return SELECT_BY_TITLE
			.on(value("title", "%" + title.toLowerCase() + "%"))
			.as(PARSER.list(), conn);
	}

}
