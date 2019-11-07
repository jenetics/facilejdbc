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

import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.RowParser;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalDate;

import static io.jenetics.facilejdbc.Dctor.field;

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@Accessors(fluent = true)
public final class Person {
	private final String forename;
	private final String surname;
	private final LocalDate birthday;
	private final String email;

	public static final RowParser<Person> PARSER = row -> Person.builder()
		.forename(row.getString("forename"))
		.surname(row.getString("surname"))
		.birthday(row.getDate("birthday").toLocalDate())
		.email(row.getString("email"))
		.build();


	public static final Dctor<Person> DCTOR = Dctor.of(
		field("forename", Person::forename),
		field("surname", Person::surname),
		field("birthday", Person::birthday),
		field("email", Person::email)
	);
}