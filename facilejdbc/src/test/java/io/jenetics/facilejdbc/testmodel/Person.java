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

import static io.jenetics.facilejdbc.Dctor.field;

import java.time.LocalDate;

import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.RowParser;

public final class Person {
	private final String forename;
	private final String surname;
	private final LocalDate birthday;
	private final String email;

	public static final RowParser<Person> PARSER = (row, conn) -> new Person(
		row.getString("forename"),
		row.getString("surname"),
		row.getDate("birthday").toLocalDate(),
		row.getString("email")
	);

	public static final Dctor<Person> DCTOR = Dctor.of(
		field("forename", Person::forename),
		field("surname", Person::surname),
		field("birthday", Person::birthday),
		field("email", Person::email)
	);

	public Person(
		final String forename,
		final String surname,
		final LocalDate birthday,
		final String email
	) {
		this.forename = forename;
		this.surname = surname;
		this.birthday = birthday;
		this.email = email;
	}

	public String forename() {
		return forename;
	}

	public String surname() {
		return surname;
	}

	public LocalDate birthday() {
		return birthday;
	}

	public String email() {
		return email;
	}
}
