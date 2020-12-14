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

import java.util.function.Function;

/**
 * Interface for mapping objects from a given <em>source</em> type to a given
 * <em>target</em> type. The mapping is used for automatic conversion of JDBC
 * types to it's Java types.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@FunctionalInterface
public interface Mapping {

	/**
	 * Return a <em>mapping</em> function from the given {@code source} to the
	 * given {@code target} type.
	 *
	 * @param source the source type
	 * @param target the target type
	 * @return the mapper function or {@code null} if no mapper function is
	 *         available
	 */
	Function<Object, Object> mapper(final Class<?> source, final Class<?> target);

	/**
	 * Maps the {@code source} object to an object of the given {@code target}
	 * type. The mapping function is {@code null}-friendly; if the {@code source}
	 * object is {@code null}, the mapping function returns {@code null}.
	 *
	 * @param source the source object
	 * @param target the target type
	 * @param <T> the target type
	 * @return the mapped {@code source} object
	 * @throws NullPointerException if the given {@code target} type is
	 *         {@code null}
	 * @throws ClassCastException if there is no mapping function to the given
	 *         {@code target} type
	 */
	default <T> T map(final Object source, final Class<? extends T> target) {
		requireNonNull(target);
		return source != null ? map0(source, target) : null;
	}

	private <T> T map0(final Object source, final Class<? extends T> target) {
		return source.getClass() == target
			? target.cast(source)
			: map1(source, target);
	}

	private <T> T map1(final Object source, final Class<? extends T> target) {
		final var mapper = mapper(source.getClass(), target);

		if (mapper != null) {
			return target.cast(mapper.apply(source));
		} else {
			throw new ClassCastException(format(
				"Mapping (%s -> %s) not supported for '%s'.",
				source.getClass().getName(), target.getName(), source
			));
		}
	}

	/**
	 * Appends the {@code other} mapping to {@code this} on. If no mapping
	 * function can be found for a given pair of {@code source} and
	 * {@code target} type, the {@code other} mapping is tried.
	 *
	 * @param other the other mapping
	 * @return the combined mapping
	 * @throws NullPointerException if the given {@code other} mapping is
	 *         {@code null}
	 */
	default Mapping or(final Mapping other) {
		requireNonNull(other);

		return (source, target) -> {
			final var mapper = mapper(source, target);
			if (mapper == null) {
				return other.mapper(source, target);
			} else {
				return mapper;
			}
		};
	}

}
