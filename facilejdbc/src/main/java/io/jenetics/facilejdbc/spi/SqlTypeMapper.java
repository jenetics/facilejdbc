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
package io.jenetics.facilejdbc.spi;

/**
 * The purpose of this SPI is to map simple <em>domain</em> values into the
 * proper type of the used DB. This may lead to a more readable insertion code.
 * Usually, it is not possible to insert an {@code URI} field directly into the
 * DB. You have to convert it into a string object first.
 * {@snippet lang="java":
 * public record Person(String name, URI link) {}
 *
 * static final Dctor<Person> DCTOR = Dctor.of(
 *     field("name", Person::name),
 *     field("email", p -> p.link().toString())
 * );
 * }
 *
 * If a mapper for the {@code URI} class is defined, it is possible to write the
 * deconstructor more concise.
 * {@snippet lang="java":
 * static final Dctor<Person> DCTOR = Dctor.of(
 *     field("name", Person::name),
 *     field("email", Person::link)
 * );
 * }
 *
 * The implementation of such a mapping is quite simple and will look like showed
 * in the following code snippet.
 * {@snippet lang="java":
 * public class MyTypeMapper extends SqlTypeMapper {
 *     public Object convert(final Object value) {
 *         if (value instanceof URI) return value.toString();
 *         return value;
 *     }
 * }
 * }
 *
 * Add the following line
 * {@snippet lang="java":
 * org.foobar.MyTypeMapper
 * }
 *
 * to the service definition file
 * {@snippet lang="java":
 * META-INF/services/io.jenetics.facilejdbc.spi.SqlTypeMapper
 * }
 *
 * and you are done.
 *
 * @implNote
 * All values are converted by this <em>mapper</em> before inserted into the DB.
 * If more than one mapper <em>service</em> is available, the mapping is stopped
 * after the first conversion.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
public abstract class SqlTypeMapper {

	protected SqlTypeMapper() {
	}

	/**
	 * The conversion method to be implemented by the SPI implementor.
	 *
	 * @param value the value to be converted
	 * @return the converted value or the input {@code value}, if no conversion
	 *         is necessary
	 */
	public abstract Object convert(final Object value);

	/**
	 * This method performs the type mapping with the registered mapper services.
	 *
	 * @param value the value to map
	 * @return the mapped value of the original {@code value}, if no mapping
	 *         is applied
	 */
	public static Object map(final Object value) {
		return SqlTypeMapperHolder.INSTANCE.map(value);
	}

}

