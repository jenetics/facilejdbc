package io.jenetics.facilejdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This interface is responsible for setting all parameters, needed for executing
 * the given {@link PreparedStatement}.
 */
public interface Preparer {

	/**
	 * Fills the parameters of the given statement.
	 *
	 * @param stmt the prepared statement to fill (prepare)
	 * @throws SQLException if the preparation fails
	 * @throws NullPointerException if the given {@code stmt} is {@code null}
	 */
	public void prepare(final PreparedStatement stmt) throws SQLException;

}
