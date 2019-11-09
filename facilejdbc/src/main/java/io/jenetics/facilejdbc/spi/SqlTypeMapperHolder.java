/*
 * Java Genetic Algorithm Library (@__identifier__@).
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
package io.jenetics.facilejdbc.spi;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converter holder class for lazy loading.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class SqlTypeMapperHolder {
	static final SqlTypeMapperHolder INSTANCE = new SqlTypeMapperHolder();

	private final Function<Object, Object> mapper;

	private SqlTypeMapperHolder() {
		final List<SqlTypeMapper> converters =
			ServiceLoader.load(SqlTypeMapper.class).stream()
				.map(Provider::get)
				.collect(Collectors.toList());

		if (converters.isEmpty()) {
			mapper = Function.identity();
		} else if (converters.size() == 1) {
			mapper = converters.get(0)::convert;
		} else {
			mapper = value -> {
				for (var converter : converters) {
					final Object nv = converter.convert(value);
					if (nv != value) {
						return nv;
					}
				}

				return value;
			};
		}
	}

	Object map(final Object value) {
		final Object nullable = toNullable(value);
		return mapper.apply(nullable);
	}

	private static Object toNullable(final Object value) {
		Object result = value;
		while (result instanceof Optional) {
			result = ((Optional<?>)result).orElse(null);
		}
		return result;
	}
}
