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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * This represents a whole Sql row (parameter set). Instead of representing the
 * row directly, a row is defined it's <em>insertion</em> strategy.
 *
 * @see Param
 * @see ParamValue
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface ParamValues {

	/**
	 * Represents an empty parameter value set.
	 */
	ParamValues EMPTY = (params, stmt) -> {};

	/**
	 * Fills the parameters of the given statement.
	 *
	 * @param paramNames the list of parameter names which should be set, if
	 *        available by this {@code ParamValues} object. The list of parameter
	 *        names is exhaustive and can be used for determining the parameter
	 *        index in the prepared statement.
	 * @param stmt the prepared statement to fill (set)
	 * @throws SQLException if setting the parameter values fails
	 */
	void set(final List<String> paramNames, final PreparedStatement stmt)
		throws SQLException;

	/**
	 * Returns a composed {@code SqlParamValues} that performs, in sequence,
	 * {@code this} operation followed by the {@code after} operation. If
	 * performing either operation throws an exception, it is relayed to the
	 * caller of the composed operation. If performing this operation throws an
	 * exception, the after operation will not be performed.
	 *
	 * @param after the preparer to perform after {@code this} preparer
	 * @return a composed preparer
	 * @throws NullPointerException if the {@code after} parameter is {@code null}
	 */
	default ParamValues andThen(final ParamValues after) {
		requireNonNull(after);

		if (this == EMPTY) {
			return after;
		} else {
			return (params, stmt) -> {
				ParamValues.this.set(params, stmt);
				after.set(params, stmt);
			};
		}
	}

}
