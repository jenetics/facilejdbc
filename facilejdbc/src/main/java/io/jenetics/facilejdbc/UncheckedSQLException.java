package io.jenetics.facilejdbc;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.sql.SQLException;

/**
 * Wraps an {@link SQLException} with an unchecked exception. This exception is
 * used when <em>stream</em> processing the query results.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__!
 */
public class UncheckedSQLException extends RuntimeException {
	private static final long serialVersionUID = 1;

	/**
	 * Constructs new unchecked SQL exception.
	 *
	 * @param message the detail message, can be {@code null}
	 * @param cause the {@code SQLException}
	 * @throws NullPointerException if the cause is {@code null}
	 */
	public UncheckedSQLException(final String message, final SQLException cause) {
		super(message, requireNonNull(cause));
	}

	/**
	 * Constructs new unchecked SQL exception.
	 *
	 * @param cause the {@code SQLException}
	 * @throws NullPointerException if the cause is {@code null}
	 */
	public UncheckedSQLException(final SQLException cause) {
		super(requireNonNull(cause));
	}

	/**
	 * Returns the cause of this exception.
	 *
	 * @return the {@code SQLException} which is the cause of this exception.
	 */
	@Override
	public SQLException getCause() {
		return (SQLException)super.getCause();
	}

	/**
	 * Called to read the object from a stream.
	 *
	 * @param s the input stream
	 * @throws InvalidObjectException if the object is invalid or has a cause
	 *         that is not an {@code SQLException}
	 * @throws ClassNotFoundException if some error occurs while creating the
	 *         serialised exception
	 */
	private void readObject(final ObjectInputStream s)
		throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();
		Throwable cause = super.getCause();
		if (!(cause instanceof SQLException)) {
			throw new InvalidObjectException(format(
				"Cause must be an SQLException, but got '%s'.",
				cause.getClass().getName()
			));
		}
	}

}
