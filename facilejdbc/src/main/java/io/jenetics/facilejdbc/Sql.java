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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static io.jenetics.facilejdbc.SerialIO.readInt;
import static io.jenetics.facilejdbc.SerialIO.readString;
import static io.jenetics.facilejdbc.SerialIO.writeInt;
import static io.jenetics.facilejdbc.SerialIO.writeString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Internal representation of an SQL query string. This parses the structure of
 * the SQL string, respectively finds the query parameters. A query parameter
 * starts with a colon ':' and contains of one or more Word-Character, as
 * defined by \W ([a-zA-Z_0-9]) in the Regex syntax.
 *
 * <pre>{@code
 * private static final Sql SELECT = Sql.of("""
 *     SELECT * FROM person
 *     WHERE forename like :forename
 *     ORDER BY surname;
 *     """
 * );
 *
 * private static final Sql INSERT = Sql.of("""
 *     INSERT INTO person(forename, surname, birthday, email)
 *     VALUES(:forename, :surname, :birthday, :email);
 *     """
 * );
 * }</pre>
 *
 * @see Query
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.1
 * @since 1.0
 */
final class Sql {

	private static final record Param(int index, String name){}

	private static final Pattern PARAM_PATTERN = Pattern.compile(
		"(?<!:):\\w+\\b(?=(?:[^\"'\\\\]*" +
		"(?:\\\\.|([\"'])(?:(?:(?!\\\\|\\1).)*\\\\.)*" +
		"(?:(?!\\\\|\\1).)*\\1))*[^\"']*$)"
	);

	private final String string;
	private final List<Param> params;

	private List<String> paramNames = null;

	Sql(final String string, final List<Param> params) {
		this.string = requireNonNull(string);
		this.params = List.copyOf(params);
	}

	/**
	 * Return the prepared SQL string. All parameter names has been replaced
	 * with '?' placeholders.
	 *
	 * @return the prepared SQL string
	 */
	String string() {
		return string;
	}

	/**
	 * Return the original SQL string, this object is created with. So the
	 * following assertion holds for every possible SQL string;
	 * <pre>{@code
	 * final String query = "SELECT * FROM table WHERE id = :id;";
	 * final Sql sql = Sql.of(query);
	 * assert query.equals(sql.sql());
	 * }</pre>
	 *
	 * @since 1.1
	 *
	 * @return the original SQL string
	 */
	String sql() {
		final StringBuilder sql = new StringBuilder();

		int index = 0;
		for (var param : params) {
			sql.append(string, index, param.index - 1);
			sql.append(":").append(param.name);
			index = param.index;
		}
		sql.append(string.substring(index));

		return sql.toString();
	}

	/**
	 * Return the list of parsed parameter names. The list may be empty or
	 * contain duplicate entries, depending on the input string. The list are
	 * in exactly the order they appeared in the SQL string.
	 *
	 * @return the parsed parameter names
	 */
	List<String> paramNames() {
		List<String> names = paramNames;
		if (names == null) {
			paramNames = names = params.stream()
				.map(p -> p.name)
				.collect(Collectors.toUnmodifiableList());
		}

		return names;
	}

	@Override
	public int hashCode() {
		return Objects.hash(string, params);
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj ||
			obj instanceof Sql sql &&
			string.equals(sql.string) &&
			params.equals(sql.params);
	}

	@Override
	public String toString() {
		return string;
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
		final List<Param> params = new ArrayList<>();
		final StringBuffer parsed = new StringBuffer();

		final Matcher matcher = PARAM_PATTERN.matcher(sql);
		while (matcher.find()) {
			final String name = matcher.group().substring(1);
			matcher.appendReplacement(parsed, "?");
			final int index = parsed.length();
			params.add(new Param(index, name));
		}
		matcher.appendTail(parsed);

		return new Sql(parsed.toString(), params);
	}


	/* *************************************************************************
	 *  Serialization methods
	 * ************************************************************************/

	void write(final DataOutput out) throws IOException {
		writeString(string, out);
		writeInt(params.size(), out);
		for (var param : params) {
			writeInt(param.index, out);
			writeString(param.name, out);
		}
	}

	static Sql read(final DataInput in) throws IOException {
		final String sql = readString(in);
		final int paramCount = readInt(in);
		final List<Param> params = new ArrayList<>();
		for (int i = 0; i < paramCount; ++i) {
			params.add(new Param(readInt(in), readString(in)));
		}

		return new Sql(sql, params);
	}

}
