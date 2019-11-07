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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class SqlTest {

	@Test(dataProvider = "strings")
	public void parsing(
		final String string,
		final List<String> params,
		final String sql
	) {
		final Sql s = Sql.of(string);

		Assert.assertEquals(s.paramNames(), params);
		Assert.assertEquals(s.string(), sql);
	}

	@DataProvider
	public Object[][] strings() {
		return new Object[][] {
			{"", List.of(), ""},
			{" ", List.of(), " "},
			{"a ", List.of(), "a "},
			{"VALUES(:a, :b)", List.of("a", "b"), "VALUES(?, ?)"},
			{"VALUES(:a,:b)", List.of("a", "b"), "VALUES(?,?)"},
			{"VALUES(:a, :a)", List.of("a", "a"), "VALUES(?, ?)"},
			{"a = :name1", List.of("name1"), "a = ?"},
			{"a :name1", List.of("name1"), "a ?"},
			{"a :name1 :name2  :name1", List.of("name1", "name2", "name1"), "a ? ?  ?"},
			{"a :name1 b", List.of("name1"), "a ? b"},
			{"a :name1 b:name2 :name3", List.of("name1", "name3"), "a ? b:name2 ?"},
			{"a :name1 ::name2 :name3", List.of("name1", "name3"), "a ? ::name2 ?"}
		};
	}

}
