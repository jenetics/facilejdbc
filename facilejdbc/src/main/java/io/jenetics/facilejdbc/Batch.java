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

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a whole batch of SQL query parameters. A <em>batch</em> is
 * essentially an {@link Iterable} of rows/records.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
public interface Batch extends Iterable<Batch.Row> {

	/**
	 * An batch entry is actually a factory function, which is able to create a
	 * <em>row</em> {@link ParamValues} from parameter indices and a connection.
	 */
	@FunctionalInterface
	public static interface Row {

		/**
		 * Creates a <em>row</em> {@link ParamValues} from the given arguments.
		 *
		 * @param conn the connection used for created the preparer, if needed
		 * @return a <em>row</em> {@link ParamValues}
		 */
		public ParamValues get(final Connection conn);
	}

	/**
	 * Return a stream with the the given batch entries (rows).
	 *
	 * @return a stream with the the given batch entries (rows)
	 */
	public default Stream<Row> stream() {
		return StreamSupport.stream(spliterator(), false);
	}


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new batch from the given records (rows) and the deconstructor.
	 *
	 * @param rows the rows to be inserted by the created batch
	 * @param dctor the record deconstructor
	 * @param <T> the row type
	 * @return a new batch from the given arguments
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public static <T> Batch of(final Iterable<T> rows, final Dctor<T> dctor) {
		requireNonNull(rows);
		requireNonNull(dctor);

		return () -> new Iterator<>() {
			private final Iterator<T> it = rows.iterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public Row next() {
				final T row = it.next();
				return conn -> dctor.apply(row, conn);
			}
		};
	}

	/**
	 * Create a new batch from the given rows.
	 *
	 * @see #of(List[])
	 *
	 * @param rows the rows to be inserted by the created batch
	 * @return a new batch from the given arguments
	 * @throws NullPointerException if the given {@code rows} are {@code null}
	 */
	public static Batch of(final List<List<Param>> rows) {
		requireNonNull(rows);

		return () -> new Iterator<>() {
			private final Iterator<List<Param>> it = rows.iterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public Row next() {
				final List<Param> row = it.next();
				return conn -> new Params(row);
			}
		};
	}

	/**
	 * Create a new batch from the given rows.
	 *
	 * @see #of(List)
	 *
	 * @param rows the rows to be inserted by the created batch
	 * @return a new batch from the given arguments
	 * @throws NullPointerException if the given {@code rows} are {@code null}
	 */
	@SafeVarargs
	public static Batch of(final List<Param>... rows) {
		return Batch.of(asList(rows));
	}

}
