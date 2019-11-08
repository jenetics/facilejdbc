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
package io.jenetics.facilejdbc;

import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class SqlValues {
	private SqlValues() {
	}

	static Object toSqlValue(final Object value) {
		Object result = value;

		while (result instanceof Optional) {
			result = ((Optional<?>)result).orElse(null);
		}

		if (result instanceof URI) {
			result = result.toString();
		} else if (result instanceof URL) {
			result = result.toString();
		} else if (result instanceof ZonedDateTime) {
			result = ((ZonedDateTime)result).toOffsetDateTime();
		}

		return result;
	}

}
