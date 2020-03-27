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

/**
 * Internal representation of an SQL query string. This parses the structure of
 * the SQL string, respectively finds the query parameters. A query parameter
 * starts with a colon ':' and contains of one or more Word-Character, as
 * defined by \W ([a-zA-Z_0-9]) in the Regex syntax.
 *
 * <pre>{@code
 * private static final Sql SELECT = Sql.of(
 *     "SELECT * FROM person " +
 *     "WHERE forename like :forename " +
 *     "ORDER BY surname;"
 * );
 *
 * private static final Sql INSERT = Sql.of(
 *     "INSERT INTO person(forename, surname, birthday, email) " +
 *     "VALUES(:forename, :surname, :birthday, :email);"
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

	private static final Pattern PARAM_PATTERN = Pattern.compile(
		"(?<!:):\\w+\\b(?=(?:[^\"'\\\\]*" +
		"(?:\\\\.|([\"'])(?:(?:(?!\\\\|\\1).)*\\\\.)*" +
		"(?:(?!\\\\|\\1).)*\\1))*[^\"']*$)"
	);

	private final String string;
	private final List<String> paramNames;

	Sql(final String string, final List<String> paramNames) {
		this.string = requireNonNull(string);
		this.paramNames = List.copyOf(paramNames);
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
	 * Return the list of parsed parameter names. The list may be empty or
	 * contain duplicate entries, depending on the input string. The list are
	 * in exactly the order they appeared in the SQL string.
	 *
	 * @return the parsed parameter names
	 */
	List<String> paramNames() {
		return paramNames;
	}

	@Override
	public int hashCode() {
		return Objects.hash(string, paramNames);
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj ||
			obj instanceof Sql &&
			string.equals(((Sql)obj).string) &&
			paramNames.equals(((Sql)obj).paramNames);
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
		final List<String> names = new ArrayList<>();
		final StringBuffer parsed = new StringBuffer();

		final Matcher matcher = PARAM_PATTERN.matcher(sql);
		while (matcher.find()) {
			final String match = matcher.group();
			names.add(match.substring(1));
			matcher.appendReplacement(parsed, "?");
		}
		matcher.appendTail(parsed);

		return new Sql(parsed.toString(), names);
	}


	/* *************************************************************************
	 *  Serialization methods
	 * ************************************************************************/

	void write(final DataOutput out) throws IOException {
		writeString(string, out);
		writeInt(paramNames.size(), out);
		for (String name : paramNames) {
			writeString(name, out);
		}
	}

	static Sql read(final DataInput in) throws IOException {
		final String sql = readString(in);
		final int paramCount = readInt(in);
		final List<String> paramNames = new ArrayList<>();
		for (int i = 0; i < paramCount; ++i) {
			paramNames.add(readString(in));
		}

		return new Sql(sql, paramNames);
	}

}
