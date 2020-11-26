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

import static io.jenetics.facilejdbc.Dctor.fieldValue;

import java.sql.SQLException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class DctorTest {

	@Test
	public void deconstruct() throws SQLException {
		final Dctor<Paper> dctor = Dctor.of(Paper.class);

		final ParamValues values = dctor.unapply(
			new Paper("titleValue", "isbnValue", 123),
			null
		);

		final var stmt = new MockPreparedStatement();
		values.set(List.of("title", "isbn", "pages"), stmt);
		Assert.assertEquals(stmt.get(1), "titleValue");
		Assert.assertEquals(stmt.get(2), "isbnValue");
		Assert.assertEquals(stmt.get(3), 123);
	}

	@Test
	public void fromRecord() throws SQLException {
		final record Foo(String colA, String colB, String colC) {}

		final Dctor<Foo> dctor = Dctor.of(
			Foo.class,
			fieldValue("col_b", "replaced_b")
		);

		final var values = dctor.unapply(new Foo("1", "2", "3"), null);
		final var stmt = new MockPreparedStatement();
		values.set(List.of("col_a", "col_b", "col_c"), stmt);
		Assert.assertEquals(stmt.get(1), "1");
		Assert.assertEquals(stmt.get(2), "replaced_b");
		Assert.assertEquals(stmt.get(3), "3");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void fromDuplicateRecordField() {
		final record Foo(String colA, String colB, String colC) {}

		final Dctor<Foo> dctor = Dctor.of(
			Foo.class,
			fieldValue("col_b", "replaced_b"),
			fieldValue("col_b", "replaced_b")
		);
	}

	@Test(dataProvider = "componentNames")
	public void toSnakeCase(final String cc, final String sc) {
		Assert.assertEquals(toSnakeCase(cc), sc);
	}

	@DataProvider
	public Object[][] componentNames() {
		return new Object[][] {
			{"forName", "for_name"},
			{"sureName", "sure_name"},
			{"userLoginCount", "user_login_count"},
			{"userCreatedAt", "user_created_at"}
		};
	}

	private static String toSnakeCase(final String str) {
		final var result = new StringBuilder();

		for (int i = 0; i < str.length(); i++) {
			final char ch = str.charAt(i);
			if (Character.isUpperCase(ch)) {
				result.append('_');
				result.append(Character.toLowerCase(ch));
			} else {
				result.append(ch);
			}
		}

		return result.toString();
	}

}
