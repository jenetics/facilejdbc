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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
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
public final class Sql implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Pattern PARAM_PATTERN = Pattern.compile("(\\s+:[\\w]+)");

	private final String _string;
	private final List<String> _paramNames;

	private Sql(final String string, final List<String> paramNames) {
		_string = requireNonNull(string);
		_paramNames = unmodifiableList(paramNames);
	}

	/**
	 * Return the prepared SQL string. All parameter names has been replaced
	 * with '?' placeholders.
	 *
	 * @return the prepared SQL string
	 */
	public String string() {
		return _string;
	}

	/**
	 * Return the list of parsed parameter names. The list may be empty or
	 * contain duplicate entries, depending on the input string.
	 *
	 * @return the parsed parameter names
	 */
	public List<String> paramNames() {
		return _paramNames;
	}

	/**
	 * Return the parameter indexes of {@code this} SQL query.
	 *
	 * @return the parameter indexes of {@code this} SQL query
	 */
	public SqlParamIndices paramIndices() {
		return this::paramIndex;
	}

	/**
	 * Return the parameter index of the given parameter name, if available. The
	 * returned index is <em>one</em>-based and can directly used in prepared
	 * statements.
	 *
	 * @param name the parameter name
	 * @return the parameter index of the given parameter name
	 * @throws NullPointerException if the given {@code name} is {@code null}
	 */
	public OptionalInt paramIndex(final String name) {
		for (int i = 0; i < _paramNames.size(); ++i) {
			if (name.equals(_paramNames.get(i))) {
				return OptionalInt.of(i + 1);
			}
		}
		return OptionalInt.empty();
	}

	@Override
	public int hashCode() {
		return _string.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return obj == this ||
			obj instanceof Sql &&
			_string.equals(((Sql) obj)._string);
	}

	@Override
	public String toString() {
		return _string;
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

		final Matcher matcher = PARAM_PATTERN.matcher(sql);
		while (matcher.find()) {
			final String match = matcher.group(1);

			if (match != null) {
				final int start = match.indexOf(':');

				final String name = match.substring(start + 1);
				names.add(name);

				final String replacement = match.substring(0, start) + "?";
				matcher.appendReplacement(parsedQuery, replacement);
			}

		}
		matcher.appendTail(parsedQuery);

		return new Sql(parsedQuery.toString(), names);
	}


	/* *************************************************************************
	 *  Java object serialization
	 * ************************************************************************/

	private Object writeReplace() {
		return new Serial(Serial.SQL, this);
	}

	private void readObject(final ObjectInputStream stream)
		throws InvalidObjectException
	{
		throw new InvalidObjectException("Serialization proxy required.");
	}

	void write(final DataOutput out) throws IOException {
		IO.writeString(_string, out);
	}

	static Sql read(final DataInput in) throws IOException {
		return Sql.of(IO.readString(in));
	}

}
