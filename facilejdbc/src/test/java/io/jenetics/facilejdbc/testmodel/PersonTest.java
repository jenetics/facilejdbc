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
package io.jenetics.facilejdbc.testmodel;

import static io.jenetics.facilejdbc.Param.value;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.util.H2DB;
import io.jenetics.facilejdbc.util.IO;

public class PersonTest {

	private final H2DB db = H2DB.INSTANCE; 

	@BeforeClass
	public void setup() throws IOException, SQLException {
		final String[] sqls = IO.
			toSQLText(getClass().getResourceAsStream("/testmodel.sql"))
			.split(";");

		for (String sql : sqls) {
			Query.of(sql).execute(db.conn());
		}
	}

	@Test
	public void insertSelect() throws SQLException {
		final Person person = Person.builder()
			.forename("Franz")
			.surname("Wilhelmstötter")
			.birthday(LocalDate.now())
			.email("franz.wilhelmstoetter@gmail.com")
			.build();

		Person.Access.INSERT
			.on(person, Person.Access.DCTOR)
			.execute(db.conn());

		final Person selected = Person.Access.SELECT
			.on(value("forename", "Franz"))
			.as(Person.Access.PARSER.single(), db.conn());

		Assert.assertEquals(selected, person);
	}

}
