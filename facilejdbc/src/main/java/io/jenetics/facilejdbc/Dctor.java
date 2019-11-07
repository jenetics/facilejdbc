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

import io.jenetics.facilejdbc.function.SqlFunction;
import io.jenetics.facilejdbc.function.SqlFunction2;

import java.sql.Connection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * This interface is responsible for creating a <em>row</em> {@link ParamValues}
 * from a given record.
 *
 * @param <T> the (deconstructed) record type
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface Dctor<T> {

	public static interface Field<T> {

		/**
		 * Return the name of the record field.
		 *
		 * @return the field name
		 */
		public String name();

		/**
		 * Return the SQL value for the give {@code row} field.
		 *
		 * @param row the actual row (record)
		 * @param conn the connection used for producing the SQL value, if needed
		 * @return the SQL value for the give {@code row} field
		 * @throws NullPointerException if the given {@code conn} is {@code null}
		 */
		public ParamValue value(final T row, final Connection conn);


		/**
		 * Create a new record field with the given {@code name} and field
		 * {@code accessor}.
		 *
		 * @param name the field name
		 * @param value the field accessor
		 * @param <T> the record type
		 * @return a new record field
		 */
		public static <T> Field<T> of(
			final String name,
			final SqlFunction2<? super T, Connection, ?> value
		) {
			requireNonNull(name);
			requireNonNull(value);

			return new Field<T>() {
				@Override
				public String name() {
					return name;
				}
				@Override
				public ParamValue value(final T row, final Connection conn) {
					return (index, stmt) ->
						stmt.setObject(index, value.apply(row, conn));
				}
			};
		}
	}


	/**
	 * Return the SQL parameter values for the given {@code record}.
	 *
	 * @param record the deconstructed record
	 * @param conn the DB connection used for record deconstruction, if needed
	 * @return a new row preparer
	 */
	public ParamValues apply(final T record, final Connection conn);


	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new deconstructor from the given field definitions.
	 *
	 * @see #of(Field[])
	 *
	 * @param fields the fields which describe the deconstruction
	 * @param <T> the type of the record to be deconstructed
	 * @return a new deconstructor from the given field definitions
	 */
	public static <T> Dctor<T> of(final List<Field<T>> fields) {
		final List<Field<T>> fls = List.copyOf(fields);

		return (record, conn) -> (params, stmt) -> {
			int index = 0;
			for (String name : params) {
				++index;
				final Field<T> field = get(name, fls);
				if (field != null) {
					field.value(record, conn).set(index, stmt);
				}
			}
		};
	}

	/**
	 * Create a new deconstructor from the given field definitions.
	 *
	 * @see #of(List)
	 *
	 * @param fields the fields which describe the deconstruction
	 * @param <T> the type of the record to be deconstructed
	 * @return a new deconstructor from the given field definitions
	 */
	@SafeVarargs
	public static <T> Dctor<T> of(final Field<T>... fields) {
		return of(asList(fields));
	}

	private static <T> Field<T> get(final String name, final List<Field<T>> fields) {
		for (Field<T> field : fields) {
			if (field.name().equals(name)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Create a new record field with the given {@code name} and field
	 * {@code accessor}.
	 *
	 * @see #field(String, SqlFunction)
	 *
	 * @param name the field name
	 * @param value the field accessor
	 * @param <T> the record type
	 * @return a new record field
	 */
	public static <T> Field<T> field(
		final String name,
		final SqlFunction2<? super T, Connection, ?> value
	) {
		return Field.of(name, value);
	}

	/**
	 * Create a new record field with the given {@code name} and field
	 * {@code accessor}.
	 *
	 * @see #field(String, SqlFunction2)
	 *
	 * @param name the field name
	 * @param value the field accessor
	 * @param <T> the record type
	 * @return a new record field
	 */
	public static <T> Field<T> field(
		final String name,
		final SqlFunction<? super T, ?> value
	) {
		return Field.of(name, (record, conn) -> value.apply(record));
	}

}
