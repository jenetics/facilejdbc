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
 * Represents a SQL value which can be set to a prepared statement. Instead of
 * representing the parameter value directly, a value is defined by it's
 * <em>insertion</em> strategy.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface ParamValue {

	/**
	 * Fills the parameter value to the given statement.
	 *
	 * @param index the index of the value to set
	 * @param stmt the prepared statement to fill (set)
	 * @throws SQLException if the preparation fails
	 */
	public void set(final int index, final PreparedStatement stmt)
		throws SQLException;

}
