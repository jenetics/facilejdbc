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

import java.sql.SQLException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class ParamsTest {

	@Test
	public void set() throws SQLException {
		final var stmt = new MockPreparedStatement();

		final Params params = new Params(List.of(
			Param.value("name_1", "value_1"),
			Param.value("name_2", "value_2"),
			Param.value("name_3", "value_3"),
			Param.value("name_4", "value_4"),
			Param.value("name_5", "value_5")
		));

		params.set(List.of("name_2", "name_5", "name_1"), stmt);
		Assert.assertEquals(stmt.get(1), "value_2");
		Assert.assertEquals(stmt.get(2), "value_5");
		Assert.assertEquals(stmt.get(3), "value_1");
	}

}
