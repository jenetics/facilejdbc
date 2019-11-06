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
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a whole batch of SQL query parameters.
 *
 * @param <T> the record (row) type of the batch.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public interface Batch<T> extends Iterable<Batch.Entry> {

	/**
	 * An batch entry is actually a factory function, which is able to create a
	 * <em>row</em> {@link ParamValues} from parameter indices and a connection.
	 */
	@FunctionalInterface
	public static interface Entry {

		/**
		 * Creates a <em>row</em> {@link ParamValues} from the given arguments.
		 *
		 * @param conn the connection used for created the preparer, if needed
		 * @return a <em>row</em> {@link ParamValues}
		 */
		public ParamValues apply(final Connection conn);
	}

	/**
	 * Return a stream with the the given batch entries (rows).
	 *
	 * @return a stream with the the given batch entries (rows)
	 */
	public default Stream<Entry> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	public static <T> Batch<T> of(final Iterable<T> rows, final Dctor<T> dctor) {
		return () -> new Iterator<>() {
			private final Iterator<T> it = rows.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Entry next() {
				final T row = it.next();
				return conn -> dctor.apply(row, conn);
			}
		};
	}

}
