package io.jenetics.facilejdbc.util;

import io.jenetics.facilejdbc.function.SqlFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class HSQLDB {
	private HSQLDB() {
	}

	private static Connection conn() throws SQLException {
		return DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
	}

	public static <T> T
	transaction(final SqlFunction<? super Connection, ? extends T> block)
		throws SQLException
	{
		return Db.transaction(conn(), block);
	}

}
