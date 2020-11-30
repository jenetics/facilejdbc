package io.jenetics.facilejdbc.function;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class JC {

	/**
	 * A method which takes an argument and can throw an exception.
	 *
	 * @param <A> the argument type
	 * @param <E> the exception type
	 */
	@FunctionalInterface
	public interface ThrowingMethod<A, E extends Exception> {
		void apply(final A arg) throws E;
	}

	/**
	 * A function which takes an argument and can throw an exception.
	 *
	 * @param <A> the argument type
	 * @param <R> the return type
	 * @param <E> the exception type
	 */
	@FunctionalInterface
	public interface ThrowingFunction<A, R, E extends Exception> {
		R apply(final A arg) throws E;
	}

	static final class UncheckedException extends RuntimeException {
		private static final long serialVersionUID = 1;

		/**
		 * Constructs new unchecked SQL exception.
		 *
		 * @param message the detail message, can be {@code null}
		 * @param cause the {@code SQLException}
		 * @throws NullPointerException if the cause is {@code null}
		 */
		public UncheckedException(final String message, final Exception cause) {
			super(message, requireNonNull(cause));
		}

		/**
		 * Constructs new unchecked SQL exception.
		 *
		 * @param cause the {@code SQLException}
		 * @throws NullPointerException if the cause is {@code null}
		 */
		public UncheckedException(final Exception cause) {
			super(requireNonNull(cause));
		}

	}

	public interface ExtendedCloseable extends AutoCloseable {

		default void uncheckedClose(
			final Function<? super Exception, ? extends RuntimeException> mapper
		) {
			try {
				close();
			} catch (Exception e) {
				throw mapper.apply(e);
			}
		}

		default void silentClose() {
			silentClose(null);
		}

		default void silentClose(final Throwable previousError) {
			try {
				close();
			} catch (Exception suppressed) {
				if (previousError != null) {
					previousError.addSuppressed(suppressed);
				}
			}
		}

		static ExtendedCloseable of(final AutoCloseable closeable) {
			requireNonNull(closeable);
			return closeable::close;
		}

		static ExtendedCloseable of(final AutoCloseable... closeables) {
			return of(Arrays.asList(closeables));
		}

		static ExtendedCloseable
		of(final Collection<? extends AutoCloseable> closeables) {
			final List<AutoCloseable> list = new ArrayList<>();
			closeables.forEach(c -> list.add(requireNonNull(c)));
			Collections.reverse(list);

			return () -> {
				if (list.size() == 1) {
					list.get(0).close();
				} else if (list.size() > 1) {
					JC.invokeAll(AutoCloseable::close, list);
				}
			};
		}

	}

	/**
	 * This interface represents a closeable value. It is useful in cases where
	 * the value doesn't implement the {@link Closeable} interface but needs
	 * some cleanup work to do after usage.
	 *
	 * <pre>{@code
	 * final CloseableValue<Path> file = CloseableValue.of(
	 *     Files.createTempFile("test-", ".txt" ),
	 *     Files::deleteIfExists
	 * );
	 *
	 * // Automatically delete the file after the test.
	 * try (file) {
	 *     Files.write(file.get(), "foo".getBytes());
	 *     final var writtenText = Files.readString(file.get());
	 *     assert "foo".equals(writtenText);
	 * }
	 * }</pre>
	 *
	 * @see #of(Object, ThrowingMethod)
	 * @see #build(ThrowingFunction)
	 *
	 * @param <T> the value type
	 */
	public interface CloseableValue<T> extends Supplier<T>, ExtendedCloseable {

		default <E extends Exception> void trying(
			final ThrowingMethod<? super T, ? extends E> block,
			final Closeable... closeables
		)
			throws E
		{
			try {
				block.apply(get());
			} catch (Throwable error) {
				ExtendedCloseable.of(closeables).silentClose(error);
				silentClose(error);
				throw error;
			}
		}

		static <T> CloseableValue<T> of(
			final T value,
			final ThrowingMethod<? super T, ? extends Exception> close
		) {
			requireNonNull(value);
			requireNonNull(close);

			return new CloseableValue<>() {
				@Override
				public T get() {
					return value;
				}
				@Override
				public void close() throws Exception {
					close.apply(get());
				}
				@Override
				public String toString() {
					return format("CloseableValue[%s]", get());
				}
			};
		}

		static <T, E extends Exception> CloseableValue<T>
		build(
			final ThrowingFunction<
				? super ResourceCollector,
				? extends T,
				? extends E> builder
		)
			throws E
		{
			requireNonNull(builder);

			final var resources = ResourceCollector.of();
			try {
				return CloseableValue.of(
					builder.apply(resources),
					value -> resources.close()
				);
			} catch (Throwable error) {
				resources.silentClose(error);
				throw error;
			}
		}

	}

	public interface ResourceCollector extends ExtendedCloseable {

		<C extends Closeable> C add(final C closeable);

		ExtendedCloseable toCloseable();

		@Override
		default void close() throws Exception {
			toCloseable().close();
		}

		static ResourceCollector
		of(final Collection<? extends Closeable> closeables) {
			final List<Closeable> resources = new ArrayList<>();
			closeables.forEach(c -> resources.add(requireNonNull(c)));

			return new ResourceCollector() {
				@Override
				public synchronized <C extends Closeable>
				C add(final C closeable) {
					resources.add(requireNonNull(closeable));
					return closeable;
				}
				@Override
				public synchronized ExtendedCloseable toCloseable() {
					return ExtendedCloseable.of(resources);
				}
			};
		}

		static ResourceCollector of(final Closeable... closeables) {
			return of(Arrays.asList(closeables));
		}

	}

	private JC() {
	}

	static <A, E extends Exception> void invokeAll(
		final ThrowingMethod<? super A, ? extends E> method,
		final Collection<? extends A> objects
	)
		throws E
	{
		raise(invokeAll0(method, objects));
	}

	private static <E extends Exception> void raise(final Throwable error)
		throws E
	{
		if (error instanceof RuntimeException) {
			throw (RuntimeException)error;
		} else if (error instanceof Error) {
			throw (Error)error;
		} else if (error != null) {
			@SuppressWarnings("unchecked")
			final var e = (E)error;
			throw e;
		}
	}

	private static final int MAX_SUPPRESSED = 5;

	static <A, E extends Exception> Throwable invokeAll0(
		final ThrowingMethod<? super A, ? extends E> method,
		final Collection<? extends A> objects
	) {
		int suppressedCount = 0;
		Throwable error = null;
		for (var object : objects) {
			if (error != null) {
				try {
					method.apply(object);
				} catch (Exception suppressed) {
					if (suppressedCount++ < MAX_SUPPRESSED) {
						error.addSuppressed(suppressed);
					}
				}
			} else {
				try {
					method.apply(object);
				} catch (Throwable e) {
					error = e;
				}
			}
		}

		return error;
	}

}
