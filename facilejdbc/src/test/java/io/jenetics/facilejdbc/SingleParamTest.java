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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public class SingleParamTest {

	@Test
	public void of() {
		final ParamValue value = (index, stmt) -> {};
		final SingleParam param = SingleParam.of("foo", value);

		Assert.assertEquals(param.name(),"foo");
		Assert.assertEquals(param.value(), value);
	}

	@Test
	public void lazy() throws SQLException {
		final var stmt = new MockPreparedStatement();

		final SingleParam param = SingleParam.lazyValue("foo", () -> "bar");
		param.value().set(1, stmt);

		Assert.assertEquals(param.name(),"foo");
		Assert.assertEquals(stmt.get(1), "bar");
	}

	@Test
	public void value() throws SQLException {
		final var stmt = new MockPreparedStatement();

		final SingleParam param = SingleParam.value("foo", "bar");
		param.value().set(1, stmt);

		Assert.assertEquals(param.name(),"foo");
		Assert.assertEquals(stmt.get(1), "bar");
	}

}
