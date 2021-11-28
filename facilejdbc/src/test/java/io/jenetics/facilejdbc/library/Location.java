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
package io.jenetics.facilejdbc.library;

import com.github.javafaker.Faker;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.Records;
import io.jenetics.facilejdbc.RowParser;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
public record Location(
	double lat,
	double lon,
	Integer ele,
	Instant createdAt,
	Double speed,
	Double magvar,
	Double geoidheight,
	String name,
	String cmt,
	String dscr,
	String src,
	String sym,
	String type,
	String fix,
	Integer sat,
	Double hdop,
	Double vdop,
	Double pdop,
	Integer ageofdgpsdata,
	Integer dgpsid,
	Double course,
	String extensions
) {

	static final RowParser<Location> PARSER = Records.parserWithFields(
		Location.class,
		Map.of("createdAt", RowParser.instant("created_at"))
	);

	static final Dctor<Location> DCTOR = Dctor.of(Location.class);

	static final Query INSERT = Query.of("""
		INSERT INTO location(
			lat, lon, ele, created_at, speed, magvar, geoidheight, name,
			cmt, dscr, src, sym, type, fix, sat, hdop, vdop, pdop,
			ageofdgpsdata, dgpsid, course, extensions
		)
		VALUES(
			:lat, :lon, :ele, :created_at, :speed, :magvar, :geoidheight, :name,
			:cmt, :dscr, :src, :sym, :type, :fix, :sat, :hdop, :vdop, :pdop,
			:ageofdgpsdata, :dgpsid, :course, :extensions
		)
		"""
	);

	static final Query SELECT_ALL = Query.of("SELECT * FROM location ORDER BY id");

	static Location next(final Random random) {
		final var faker = new Faker(random);

		return new Location(
			random.nextDouble(),
			random.nextDouble(),
			nullable(random, random::nextInt),
			nullable(random, () -> Instant.ofEpochMilli(random.nextInt(10000))),
			nullable(random, random::nextDouble),
			nullable(random, random::nextDouble),
			nullable(random, random::nextDouble),
			nullable(random, faker.friends()::character),
			nullable(random, faker.friends()::character),
			nullable(random, faker.friends()::character),
			nullable(random, faker.friends()::character),
			nullable(random, faker.friends()::character),
			nullable(random, faker.friends()::character),
			nullable(random, () -> {
				final var value = faker.friends().character();
				return value.substring(0, Math.min(value.length(), 10));
			}),
			nullable(random, random::nextInt),
			nullable(random, random::nextDouble),
			nullable(random, random::nextDouble),
			nullable(random, random::nextDouble),
			nullable(random, random::nextInt),
			nullable(random, random::nextInt),
			nullable(random, random::nextDouble),
			nullable(random, faker.chuckNorris()::fact)
		);
	}

	private static <T> T nullable(
		final Random random,
		final Supplier<? extends T> value
	) {
		return random.nextBoolean() ? value.get() : null;
	}

}
