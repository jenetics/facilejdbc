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

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal representation of an SQL query string. This parses the structure of
 * the SQL string, respectively finds the query parameters. A query parameter
 * starts with a colon ':' and contains of one or more Word-Character, as
 * defined by \W ([a-zA-Z_0-9]) in the Regex syntax.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class Sql {

	private static final Pattern PARAM_PATTERN =
		Pattern.compile("(^|[^\\w:]+?):([\\w]+)");

	private final String _sql;
	private final List<String> _params;

	private Sql(final String sql, final List<String> params) {
		_sql = requireNonNull(sql);
		_params = unmodifiableList(params);
	}

	/**
	 * Return the prepared SQL string. All parameter names has been replaced
	 * with '?' placeholders.
	 *
	 * @return the prepared SQL string
	 */
	String sql() {
		return _sql;
	}

	/**
	 * Return the list of parsed parameter names. The list may be empty or
	 * contain duplicate entries, depending on the input string.
	 *
	 * @return the parsed parameter names
	 */
	List<String> params() {
		return _params;
	}

	@Override
	public int hashCode() {
		return _sql.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj == this ||
			obj instanceof Sql &&
			_sql.equals(((Sql) obj)._sql);
	}

	@Override
	public String toString() {
		return _sql;
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new Sql object from the given SQL string.
	 *
	 * @param sql the SQL string to parse.
	 * @return the newly created {@code Sql} object
	 * @throws NullPointerException if the given SQL string is {@code null}
	 */
	static Sql of(final String sql) {
		final List<String> names = new ArrayList<>();
		final StringBuffer parsedQuery = new StringBuffer();
		System.out.println("#" + sql + "#");

		final Matcher matcher = PARAM_PATTERN.matcher(sql);

		int start = 0;
		while (matcher.find()) {
			parsedQuery.append(sql, start, matcher.start(2));

			final String name = matcher.group(2);
			System.out.println("### '" + matcher.group(1) + "' : '" + name + "' :" + matcher.groupCount() + ":" + matcher.group());

			if (name != null) {
				names.add(name);
				matcher.appendReplacement(parsedQuery, " ?");
			}

			start = matcher.end();
		}
		parsedQuery.append(sql.substring(start));

		return new Sql(parsedQuery.toString(), names);
	}

}
