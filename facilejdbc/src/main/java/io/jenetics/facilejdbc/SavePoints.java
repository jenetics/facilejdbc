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
import java.sql.SQLException;

import io.jenetics.facilejdbc.function.SqlRunnable;
import io.jenetics.facilejdbc.function.SqlSupplier;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public final class SavePoints {
	private SavePoints() {}


	public static <T> T apply(
		final Connection conn,
		final SqlSupplier<? extends T> block
	)
		throws SQLException
	{
		final var savepoint = new SavePoint(conn);
		try (savepoint) {
			return block.get();
		} catch (Throwable e) {
			try {
				savepoint.rollback();
			} catch (Exception suppressed) {
				e.addSuppressed(suppressed);
			}
			throw e;
		}
	}

	public static void accept(final Connection conn, final SqlRunnable block)
		throws SQLException
	{
		apply(conn, () -> { block.run(); return null; });
	}

}
