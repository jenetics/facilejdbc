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
public class BatchTest {

	@Test
	public void iterate() throws SQLException {
		final Dctor<Paper> dctor = Dctor.of(Paper.class);

		final Batch batch = Batch.of(
			List.of(
				new Paper("title_1", "isbn_1", 1),
				new Paper("title_2", "isbn_2", 2),
				new Paper("title_3", "isbn_3", 3),
				new Paper("title_4", "isbn_4", 4),
				new Paper("title_5", "isbn_5", 5)
			),
			dctor
		);

		int index = 1;
		for (var params : batch) {
			final ParamValues values = params.apply(null);

			final var stmt = new MockPreparedStatement();
			values.set(List.of("title", "isbn", "pages"), stmt);

			Assert.assertEquals(stmt.get(1), "title_" + index);
			Assert.assertEquals(stmt.get(2), "isbn_" + index);
			Assert.assertEquals(stmt.get(3), index);

			++index;
		}

	}

}
