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
import java.util.function.Function;

/**
 * Represents a whole batch of query parameter values. A <em>batch</em> is
 * essentially an {@link Iterable} of records or row-creation functions. The
 * available factory functions makes it easy to create a batch from a given
 * list of records or parameters.
 *
 * <pre>{@code
 * final List<Person> persons = ...;
 * final Dctor<Person> dctor = ...;
 * final Batch batch = Batch.of(persons, dctor);
 * }</pre>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
public interface Batch extends Iterable<Function<Connection, ParamValues>> {


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new batch from the given rows.
	 *
	 * @see #of(List[])
	 *
	 * @param rows the rows to be inserted by the created batch
	 * @return a new batch from the given arguments
	 * @throws NullPointerException if the given {@code rows} are {@code null}
	 */
	static Batch of(final Iterable<? extends List<? extends SingleParam>> rows) {
		requireNonNull(rows);

		return () -> new Iterator<>() {
			private final Iterator<? extends List<? extends SingleParam>>
				it = rows.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public Function<Connection, ParamValues> next() {
				final List<? extends SingleParam> row = it.next();
				return conn -> new Params(row);
			}
		};
	}

	/**
	 * Create a new batch from the given rows.
	 *
	 * @see #of(Iterable)
	 *
	 * @param rows the rows to be inserted by the created batch
	 * @return a new batch from the given arguments
	 * @throws NullPointerException if the given {@code rows} are {@code null}
	 */
	@SafeVarargs
	static Batch of(final List<? extends SingleParam>... rows) {
		return Batch.of(asList(rows));
	}

	/**
	 * Create a new batch from the given records (rows) and the deconstructor.
	 *
	 * @param records the rows to be inserted by the created batch
	 * @param dctor the record deconstructor
	 * @param <T> the row type
	 * @return a new batch from the given arguments
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	static <T> Batch of(
		final Iterable<? extends T> records,
		final Dctor<? super T> dctor
	) {
		requireNonNull(records);
		requireNonNull(dctor);

		return () -> new Iterator<>() {
			private final Iterator<? extends T> it = records.iterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public Function<Connection, ParamValues> next() {
				final T record = it.next();
				return conn -> dctor.unapply(record, conn);
			}
		};
	}

}
