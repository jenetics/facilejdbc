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
 */
final class SqlTypeMapperHolder {
	static final SqlTypeMapperHolder INSTANCE = new SqlTypeMapperHolder();

	private final List<SqlTypeMapper> converters;
	private final Function<Object, Object> mapper;

	private SqlTypeMapperHolder() {
		converters = converters();

		if (converters.isEmpty()) {
			mapper = SqlTypeMapperHolder::identity;
		} else {
			mapper = this::convert;
		}
	}

	private static List<SqlTypeMapper> converters() {
		return ServiceLoader.load(SqlTypeMapper.class).stream()
			.map(Provider::get)
			.collect(Collectors.toList());
	}

	private static Object identity(final Object value) {
		return value;
	}

	private Object convert(final Object value) {
		for (var converter : converters) {
			final Object nv = converter.convert(value);
			if (nv != value) {
				return nv;
			}
		}

		return value;
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
