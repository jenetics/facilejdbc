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
import java.util.Arrays;

/**
 * Helper class for exporting DB result to CSV string.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.3
 * @since 1.3
 */
final class CSV {
	private CSV() {}

	private static final String EOL = "\r\n";
	private static final String SEPARATOR_STR = ",";
	private static final String QUOTE_STR = "\"";
	private static final String DOUBLE_QUOTE_STR = "\"\"";

	static String string(final ResultSet rs, final Connection conn)
		throws SQLException
	{
		final var out = new StringBuilder();
		final var md = rs.getMetaData();

		final var row = new Object[md.getColumnCount()];
		final var cols = Arrays.asList(row);

		// Append the header.
		for (int i = 0; i < row.length; ++i) {
			row[i] = md.getColumnLabel(i + 1);
		}
		out.append(join(cols)).append(EOL);

		// Append the rows.
		while (rs.next()) {
			for (int i = 0; i < row.length; ++i) {
				row[i] = rs.getObject(i + 1);
			}
			out.append(join(cols)).append(EOL);
		}

		return out.toString();
	}

	static String join(final Iterable<?> cols) {
		final var row = new StringBuilder(32);

		final var it = cols.iterator();
		while (it.hasNext()) {
			final var column = it.next();
			row.append(escape(column));
			if (it.hasNext()) {
				row.append(SEPARATOR_STR);
			}
		}

		return row.toString();
	}

	private static String escape(final Object value) {
		if (value == null) {
			return "";
		} else {
			return
				QUOTE_STR +
				value.toString().replace(QUOTE_STR, DOUBLE_QUOTE_STR) +
				QUOTE_STR;
		}
	}
}
