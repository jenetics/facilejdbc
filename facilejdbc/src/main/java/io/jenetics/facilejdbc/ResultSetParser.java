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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface is responsible for parsing a {@link ResultSet} to a record of
 * type {@code T}. The intended way for creating a result-set parser is via an
 * existing {@link RowParser}.
 * {@snippet lang="java":
 * final RowParser<Person> parser = (row, conn) -> new Person(
 *     row.getString("name"),
 *     row.getString("email"),
 *     row.getString("link")
 * );
 *
 * final ResultSetParser<Person> rsp1 = parser.single();
 * final ResultSetParser<Optional<Person>> rsp2 = parser.singleOpt();
 * final ResultSetParser<List<Person>> rsp3 = parser.list();
 * }
 *
 * @see RowParser
 *
 * @param <T> the record/row type
 *
 * @apiNote
 * {@code ResultSetParser} are created via {@code RowParser} objects. There is
 * no need for <em>implementing</em> this interface directly.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.3
 * @since 1.0
 */
@FunctionalInterface
public interface ResultSetParser<T> {

	/**
	 * Converts the row on the current cursor position into a data object.
	 *
	 * @param rs the data source
	 * @param conn the connection used for producing the record, if needed
	 * @return the stored data object
	 * @throws SQLException if reading of the current row fails
	 */
	T parse(final ResultSet rs, final Connection conn)
		throws SQLException;

	/**
	 * Return a {@link ResultSet} parser, which converts the query result to a
	 * CSV line.
	 * {@snippet lang = "java":
	 * final var select = Query.of("SELECT * FROM book;");
	 * final var csv = select.as(ResultSetParser.csv(), conn);
	 * System.out.println(csv);
	 *}
	 * The CSV output will look like this:
	 * <pre>
	 * "ID","PUBLISHED_AT","TITLE","ISBN","PAGES"
	 * "0","1987-02-04","Auf der Suche nach der verlorenen Zeit","978-3518061756","5100"
	 * "1","1945-01-04","Database Design for Mere Mortals","978-0321884497","654"
	 * "2","1887-02-04","Der alte Mann und das Meer","B00JM4RD2S","142"
	 * </pre>
	 *
	 * @since 3.0
	 *
	 * @see RowParser#csvLine()
	 *
	 * @return a CSV {@link ResultSet} parser
	 */
	static ResultSetParser<String> csv() {
		return CSV::string;
	}

	/**
	 * Return a {@link ResultSet} parser, which converts the query result to a
	 * CSV line.
	 * {@snippet lang = "java":
	 * final var select = Query.of("SELECT * FROM book;");
	 * final var csv = select.as(ResultSetParser.csvLine(), conn);
	 * System.out.println(csv);
	 *}
	 * The CSV output will look like this:
	 * <pre>
	 * "ID","PUBLISHED_AT","TITLE","ISBN","PAGES"
	 * "0","1987-02-04","Auf der Suche nach der verlorenen Zeit","978-3518061756","5100"
	 * "1","1945-01-04","Database Design for Mere Mortals","978-0321884497","654"
	 * "2","1887-02-04","Der alte Mann und das Meer","B00JM4RD2S","142"
	 * </pre>
	 *
	 * @since 1.3
	 *
	 * @see RowParser#csvLine()
	 *
	 * @return a CSV {@link ResultSet} parser
	 */
	@Deprecated(since = "3.0", forRemoval = true)
	static ResultSetParser<String> csvLine() {
		return CSV::string;
	}

}
