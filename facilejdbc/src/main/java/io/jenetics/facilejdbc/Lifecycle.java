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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper methods/interfaces for handling AutoCloseable objects.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 1.3
 * @version 1.3
 */
final class Lifecycle {

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

	/**
	 * Extends the {@link AutoCloseable} and narrows the thrown exception to
	 * {@code E}.
	 */
	public interface ExtendedCloseable<E extends Exception>
		extends AutoCloseable
	{

		@Override
		void close() throws E;

		default <R extends RuntimeException>
		void uncheckedClose(final Function<? super E, ? extends R> mapper) {
			try {
				close();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				} else {
					@SuppressWarnings("unchecked")
					final var error = (E)e;
					throw mapper.apply(error);
				}
			}
		}

		/**
		 * Calls the {@link #close()} method and ignores every thrown exception.
		 * If the given {@code previousError} is <em>non-null</em>, the thrown
		 * exception is appended to the list of suppressed exceptions.
		 *
		 * @param previousError the error, which triggers the close of the given
		 *        {@code closeables}
		 */
		default void silentClose(final Throwable previousError) {
			try {
				close();
			} catch (Exception suppressed) {
				if (previousError != null) {
					previousError.addSuppressed(suppressed);
				}
			}
		}

		/**
		 * Create a new {@code ExtendedCloseable} object with the given initial
		 * {@code closeables} objects. The given list of objects are closed in
		 * reversed order.
		 *
		 * @see #of(Collection)
		 *
		 * @param closeables the initial closeables objects
		 * @return a new closeable object which collects the given
		 *        {@code closeables}
		 * @throws NullPointerException if one of the {@code closeables} is
		 *         {@code null}
		 */
		static <E extends Exception> ExtendedCloseable<E>
		of(final AutoCloseable... closeables) {
			return of(Arrays.asList(closeables));
		}

		/**
		 * Create a new {@code ExtendedCloseable} object with the given
		 * {@code closeables} objects. The given list of objects are closed in
		 * reversed order.
		 *
		 * @see #of(AutoCloseable...)
		 *
		 * @param closeables the initial closeables objects
		 * @return a new closeable object which collects the given
		 *        {@code closeables}
		 * @throws NullPointerException if one of the {@code closeables} is
		 *         {@code null}
		 */
		static <E extends Exception> ExtendedCloseable<E>
		of(final Collection<? extends AutoCloseable> closeables) {
			final List<AutoCloseable> list = new ArrayList<>();
			closeables.forEach(c -> list.add(requireNonNull(c)));
			Collections.reverse(list);

			return () -> Lifecycle.<AutoCloseable, E>invokeAll(
				c -> {
					try {
						c.close();
					} catch (Exception e) {
						raise(e);
					}
				},
				list
			);
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
	public interface CloseableValue<T, E extends Exception>
		extends Supplier<T>, ExtendedCloseable<E>
	{

		/**
		 * Create a new closeable value with the given {@code value} and the
		 * {@code close} method.
		 *
		 * @param value the actual value
		 * @param close the {@code close} method for the given {@code value}
		 * @param <T> the value type
		 * @return a new closeable value
		 * @throws NullPointerException if one of the arguments is {@code null}
		 */
		static <T, E extends Exception> CloseableValue<T, E> of(
			final T value,
			final ThrowingMethod<? super T, ? extends E> close
		) {
			requireNonNull(close);

			return new CloseableValue<>() {
				@Override
				public T get() {
					return value;
				}
				@Override
				public void close() throws E {
					close.apply(get());
				}
				@Override
				public String toString() {
					return format("CloseableValue[%s]", get());
				}
			};
		}

		/**
		 * Opens a kind of {@code try-catch} with resources block. The difference
		 * is, that the resources, registered with the
		 * {@link ResourceCollector#add(AutoCloseable)} method, are only closed in
		 * the case of an error. If the <em>value</em> could be created, the
		 * caller is responsible for closing the opened <em>resources</em> by
		 * calling the {@link CloseableValue#close()} method.
		 *
		 * <pre>{@code
		 * final CloseableValue<Stream<Object>> result = CloseableValue.build(resources -> {
		 *     final var fin = resources.add(new FileInputStream(file.toFile()));
		 *     final var bin = resources.add(new BufferedInputStream(fin));
		 *     final var oin = resources.add(new ObjectInputStream(bin));
		 *
		 *     return Stream.generate(() -> readNextObject(oin))
		 *         .takeWhile(Objects::nonNull);
		 * });
		 *
		 * try (result) {
		 *     result.get().forEach(System.out::println);
		 * }
		 * }</pre>
		 *
		 * @see ResourceCollector
		 *
		 * @param builder the builder method
		 * @param <T> the value type of the created <em>closeable</em> value
		 * @param <E> the thrown exception type while building the value
		 * @return the closeable built value
		 * @throws E in the case of an error. If this exception is thrown, all
		 *         <em>registered</em> resources are closed.
		 * @throws NullPointerException if the given {@code builder} is
		 *         {@code null}
		 */
		static <T, E extends Exception> CloseableValue<T, E>
		build(
			final ThrowingFunction<
				? super ResourceCollector<E>,
				? extends T,
				? extends E> builder
		)
			throws E
		{
			requireNonNull(builder);

			final var resources = ResourceCollector.<E>of();
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

	/**
	 * This class allows to collect one or more {@link Closeable} objects into
	 * one. The registered closeable objects are closed in reverse order.
	 * <p>
	 * Using the {@code ResourceCollector} class can simplify the the creation of
	 * dependent input streams, where it might be otherwise necessary to create
	 * nested {@code try-with-resources} blocks.
	 *
	 * <pre>{@code
	 * try (var resources = ResourceCollector.of()) {
	 *     final var fin = resources.add(new FileInputStream(file));
	 *     if (fin.read() != -1) {
	 *         return;
	 *     }
	 *     final var oin = resources.add(new ObjectInputStream(fin));
	 *     // ...
	 * }
	 * }</pre>
	 *
	 * @see CloseableValue#build(ThrowingFunction)
	 */
	public interface ResourceCollector<E extends Exception>
		extends ExtendedCloseable<E>
	{

		/**
		 * Registers the given {@code closeable} to the list of managed
		 * closeables.
		 *
		 * @param closeable the new closeable to register
		 * @param <C> the closeable type
		 * @return the registered closeable
		 */
		<C extends AutoCloseable> C add(final C closeable);

		/**
		 * Create a new closeable object from a snapshot of the currently
		 * registered resources.
		 *
		 * @see ExtendedCloseable#of(Collection)
		 *
		 * @return a new closeable object
		 */
		ExtendedCloseable<E> toCloseable();

		@Override
		default void close() throws E {
			toCloseable().close();
		}

		/**
		 * Create a new {@code ResourceCollector} object with the given initial
		 * {@code closeables} objects.
		 *
		 * @see #of(AutoCloseable...)
		 *
		 * @param closeables the initial closeables objects
		 * @return a new resource collector object which collects the given
		 *        {@code closeables}
		 * @throws NullPointerException if one of the {@code closeables} is
		 *         {@code null}
		 */
		static <E extends Exception> ResourceCollector<E>
		of(final Collection<? extends AutoCloseable> closeables) {
			final List<AutoCloseable> resources = new ArrayList<>();
			closeables.forEach(c -> resources.add(requireNonNull(c)));

			return new ResourceCollector<E>() {
				@Override
				public synchronized <C extends AutoCloseable>
				C add(final C closeable) {
					resources.add(requireNonNull(closeable));
					return closeable;
				}
				@Override
				public synchronized ExtendedCloseable<E> toCloseable() {
					return ExtendedCloseable.of(resources);
				}
			};
		}

		/**
		 * Create a new {@code ResourceCollector} object with the given initial
		 * {@code closeables} objects.
		 *
		 * @see #of(Collection)
		 *
		 * @param closeables the initial closeables objects
		 * @return a new closeable object which collects the given
		 *        {@code closeables}
		 * @throws NullPointerException if one of the {@code closeables} is
		 *         {@code null}
		 */
		static <E extends Exception> ResourceCollector<E>
		of(final AutoCloseable... closeables) {
			return of(Arrays.asList(closeables));
		}

	}

	private Lifecycle() {
	}

	/**
	 * Invokes the {@code method} on all given {@code objects}, no matter if one
	 * of the method invocations throws an exception. The first exception thrown
	 * is rethrown after invoking the method on the remaining objects, all other
	 * exceptions are swallowed.
	 *
	 * <pre>{@code
	 * final var streams = new ArrayList<InputStream>();
	 * streams.add(new FileInputStream(file1));
	 * streams.add(new FileInputStream(file2));
	 * streams.add(new FileInputStream(file3));
	 * // ...
	 * invokeAll(Closeable::close, streams);
	 * }</pre>
	 *
	 * @param <A> the closeable object type
	 * @param <E> the exception type
	 * @param objects the objects where the methods are called.
	 * @param method the method which is called on the given object.
	 * @throws E the first exception thrown by the one of the method
	 *         invocation.
	 */
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

	/**
	 * Invokes the {@code method}> on all given {@code objects}, no matter if one
	 * of the method invocations throws an exception. The first exception thrown
	 * is returned, all other exceptions are swallowed.
	 *
	 * @param objects the objects where the methods are called.
	 * @param method the method which is called on the given object.
	 * @return the first exception thrown by the method invocation or {@code null}
	 *         if no exception has been thrown
	 */
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
