package io.jenetics.facilejdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Setter {

	public void set(final PreparedStatement stmt, final int index)
		throws SQLException;

}
