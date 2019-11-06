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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This represents a whole Sql row (parameter set).
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface SqlParamValues {

	/**
	 * Represents an empty parameter value set.
	 */
	public static final SqlParamValues EMPTY = (stmt, indices) -> {};

	/**
	 * Fills the parameters of the given statement.
	 *
	 * @param stmt the prepared statement to fill (set)
	 * @param indices the parameter indices
	 * @throws SQLException if the preparation fails
	 * @throws NullPointerException if the given {@code stmt} is {@code null}
	 */
	public void set(final PreparedStatement stmt, final ParamIndexes indices)
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
	 */
	public default SqlParamValues andThen(final SqlParamValues after) {
		return (stmt, indices) -> {
			SqlParamValues.this.set(stmt, indices);
			after.set(stmt, indices);
		};
	}

}
