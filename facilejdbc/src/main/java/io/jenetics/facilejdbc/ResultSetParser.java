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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This interface is responsible for parsing a {@link ResultSet} to a record of
 * type {@code T}. The usual way for creating a result set parser is via an
 * existing {@link RowParser}.
 *
 * <pre>{@code
 * final RowParser<Person> parser = row -> new Person(
 *     row.getString("name"),
 *     row.getString("email"),
 *     row.getString("link")
 * );
 *
 * final ResultSetParser<Person> rsp1 = parser.single();
 * final ResultSetParser<Optional<Person>> rsp2 = parser.singleOpt();
 * final ResultSetParser<List<Person>> rsp3 = parser.list();
 * }</pre>
 *
 * @see RowParser
 *
 * @param <T> the row type
 *
 * @apiNote
 * {@code ResultSetParser} are created via {@code RowParser} objects.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface ResultSetParser<T> {

	/**
	 * Converts the row on the current cursor position into a data object.
	 *
	 * @param rs the data source
	 * @return the stored data object
	 * @throws SQLException if reading of the current row fails
	 */
	public T parse(final ResultSet rs) throws SQLException;

}
