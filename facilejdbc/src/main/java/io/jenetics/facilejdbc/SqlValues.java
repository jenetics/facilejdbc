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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.jenetics.facilejdbc.spi.SqlTypeConverter;

/**
 * Helper class for doing SQL type conversion.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class SqlValues {

	/**
	 * Converter holder class for lazy loading.
	 */
	private static final class ConvertHolder {
		static final ConvertHolder INSTANCE = new ConvertHolder();

		private final List<SqlTypeConverter> converters;
		private final Function<Object, Object> mapper;

		private ConvertHolder() {
			converters = SqlTypeConverter.converters();

			if (converters.isEmpty()) {
				mapper = ConvertHolder::identity;
			} else {
				mapper = this::map;
			}
		}

		private static Object identity(final Object value) {
			return value;
		}

		private Object map(final Object value) {
			for (var converter : converters) {
				final Object nv = converter.convert(value);
				if (nv != value) {
					return nv;
				}
			}

			return value;
		}
	}

	private SqlValues() {
	}

	/**
	 * This method is called by the {@link ParamValue} and {@link Dctor.Field}
	 * field classes, for converting values to the proper SQL type.
	 *
	 * @param value the <em>raw</em>-value to convert
	 * @return the converted value
	 */
	static Object toSqlValue(final Object value) {
		final Object nullable = toNullable(value);
		return ConvertHolder.INSTANCE.mapper.apply(nullable);
	}

	private static Object toNullable(final Object value) {
		Object result = value;
		while (result instanceof Optional) {
			result = ((Optional<?>)result).orElse(null);
		}
		return result;
	}

}
