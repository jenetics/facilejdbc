package io.jenetics.facilejdbc;

import io.jenetics.facilejdbc.function.SqlRunnable;
import io.jenetics.facilejdbc.function.SqlSupplier;

import java.sql.Connection;
import java.sql.SQLException;

public final class SavePoints {
	private SavePoints() {}


	public static <T> T apply(
		final Connection conn,
		final SqlSupplier<? extends T> block
	)
		throws SQLException
	{
		final var savepoint = new CloseableSavePoint(conn);
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
