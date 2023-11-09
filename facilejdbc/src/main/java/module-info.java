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

/**
 * JDBC is the basic API for accessing relational databases. Being basic makes
 * it quite tedious to use directly. This leads to higher level abstractions
 * like <a href="https://docs.oracle.com/javaee/7/tutorial/partpersist.htm">JPA</a>
 * Using a full-grown _Object Relational Mapper_ on the other side might be to
 * heavy weight for many uses cases. <em>FacileJDBC</em> tries to fill the gap
 * by making the low-level JDBC access less verbose and tedious. SQL is still
 * used as a query language.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 2.1
 */
module io.jenetics.facilejdbc {
	requires transitive java.sql;

	exports io.jenetics.facilejdbc;
	exports io.jenetics.facilejdbc.function;
	exports io.jenetics.facilejdbc.spi;

	uses io.jenetics.facilejdbc.spi.SqlTypeMapper;
}
