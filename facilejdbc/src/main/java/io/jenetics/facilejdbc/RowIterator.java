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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Converts the cursor like {@link ResultSet} into a {@link Row} iterator.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.3
 * @since 1.3
 */
final class RowIterator implements Iterator<Row> {

	private final ResultSet rs;
	private final Row row;

	private boolean hasNext = false;
	private boolean finished = false;

	RowIterator(final ResultSet rs) {
		this.rs = requireNonNull(rs);
		row = ResultSetRow.of(rs);
	}

	@Override
	public boolean hasNext() {
		if (finished) return false;
		if (hasNext) return true;

		try {
			return (hasNext = rs.next());
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		} finally {
			if (!hasNext) finished = true;
		}
	}

	@Override
	public Row next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		hasNext = false;
		return row;
	}
}
