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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Default JDBC type mappings.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class Mappings {
	private Mappings() {}

	/**
	 * Helper class which represents a specific mapping.
	 *
	 * @param <A> the source type
	 * @param <B> the target type
	 */
	private record Mapping<A, B>(
		Class<A> source,
		Class<B> target,
		Function<A, B> mapper
	){}

	private static final List<Mapping<?, ?>> MAPPINGS_LIST = List.of(
		/* *********************************************************************
		 * Numeric conversions.
		 * ********************************************************************/
		mapping(BigDecimal.class, BigInteger.class, BigDecimal::toBigInteger),
		mapping(BigDecimal.class, Double.class, BigDecimal::doubleValue),
		mapping(BigDecimal.class, Float.class, BigDecimal::floatValue),
		mapping(BigDecimal.class, Long.class, BigDecimal::longValue),
		mapping(BigDecimal.class, Integer.class, BigDecimal::intValue),
		mapping(BigDecimal.class, Short.class, BigDecimal::shortValue),
		mapping(BigDecimal.class, Byte.class, BigDecimal::byteValue),

		mapping(BigInteger.class, BigDecimal.class, BigDecimal::new),
		mapping(BigInteger.class, Double.class, BigInteger::doubleValue),
		mapping(BigInteger.class, Float.class, BigInteger::floatValue),
		mapping(BigInteger.class, Long.class, BigInteger::longValue),
		mapping(BigInteger.class, Integer.class, BigInteger::intValue),
		mapping(BigInteger.class, Short.class, BigInteger::shortValue),
		mapping(BigInteger.class, Byte.class, BigInteger::byteValue),

		mapping(Double.class, BigDecimal.class, BigDecimal::new),
		mapping(Double.class, BigInteger.class, Double::longValue, BigInteger::valueOf),
		mapping(Double.class, Float.class, Double::floatValue),
		mapping(Double.class, Long.class, Double::longValue),
		mapping(Double.class, Integer.class, Double::intValue),
		mapping(Double.class, Short.class, Double::shortValue),
		mapping(Double.class, Byte.class, Double::byteValue),

		mapping(Float.class, BigDecimal.class, BigDecimal::new),
		mapping(Float.class, BigInteger.class, Float::longValue, BigInteger::valueOf),
		mapping(Float.class, Double.class, Float::doubleValue),
		mapping(Float.class, Long.class, Float::longValue),
		mapping(Float.class, Integer.class, Float::intValue),
		mapping(Float.class, Short.class, Float::shortValue),
		mapping(Float.class, Byte.class, Float::byteValue),

		mapping(Long.class, BigDecimal.class, BigDecimal::new),
		mapping(Long.class, BigInteger.class, BigInteger::valueOf),
		mapping(Long.class, Double.class, Long::doubleValue),
		mapping(Long.class, Float.class, Long::floatValue),
		mapping(Long.class, Integer.class, Long::intValue),
		mapping(Long.class, Short.class, Long::shortValue),
		mapping(Long.class, Byte.class, Long::byteValue),

		mapping(Integer.class, BigDecimal.class, BigDecimal::new),
		mapping(Integer.class, BigInteger.class, Integer::longValue, BigInteger::valueOf),
		mapping(Integer.class, Double.class, Integer::doubleValue),
		mapping(Integer.class, Float.class, Integer::floatValue),
		mapping(Integer.class, Long.class, Integer::longValue),
		mapping(Integer.class, Short.class, Integer::shortValue),
		mapping(Integer.class, Byte.class, Integer::byteValue),

		mapping(Short.class, BigDecimal.class, BigDecimal::new),
		mapping(Short.class, BigInteger.class, Short::longValue, BigInteger::valueOf),
		mapping(Short.class, Double.class, Short::doubleValue),
		mapping(Short.class, Float.class, Short::floatValue),
		mapping(Short.class, Long.class, Short::longValue),
		mapping(Short.class, Integer.class, Short::intValue),
		mapping(Short.class, Byte.class, Short::byteValue),

		mapping(Byte.class, BigDecimal.class, BigDecimal::valueOf),
		mapping(Byte.class, BigInteger.class, Byte::longValue, BigInteger::valueOf),
		mapping(Byte.class, Double.class, Byte::doubleValue),
		mapping(Byte.class, Float.class, Byte::floatValue),
		mapping(Byte.class, Long.class, Byte::longValue),
		mapping(Byte.class, Integer.class, Byte::intValue),
		mapping(Byte.class, Short.class, Byte::shortValue),

		mapping(Boolean.class, Long.class, v -> v ? (long)1 : (long)0),
		mapping(Boolean.class, Integer.class, v -> v ? 1 : 0),
		mapping(Boolean.class, Short.class, v -> v ? (short)1 : (short)0),
		mapping(Boolean.class, Byte.class, v -> v ? (byte)1 : (byte)0),

		/* *********************************************************************
		 * Date/Time conversions.
		 * ********************************************************************/

		mapping(Date.class, Long.class, Date::getTime),
		mapping(Date.class, Instant.class, Date::toInstant),
		mapping(Date.class, java.util.Date.class, Date::getTime, java.util.Date::new),
		mapping(Date.class, LocalDate.class, Date::toLocalDate),
		mapping(Date.class, LocalDateTime.class, Date::toInstant, Mappings::toLocalDateTime),
		mapping(Date.class, OffsetDateTime.class, Date::toInstant, Mappings::toOffsetDateTime),
		mapping(Date.class, ZonedDateTime.class, Date::toInstant, Mappings::toZonedDateTime),

		mapping(Time.class, Long.class, Time::getTime),
		mapping(Time.class, Instant.class, Time::toInstant),
		mapping(Time.class, LocalTime.class, Time::toLocalTime),

		mapping(Timestamp.class, Long.class, Timestamp::getTime),
		mapping(Timestamp.class, Instant.class, Timestamp::toInstant),
		mapping(Timestamp.class, java.util.Date.class, Timestamp::getTime, java.util.Date::new),
		mapping(Timestamp.class, LocalDate.class, Timestamp::toInstant, Mappings::toLocalDate),
		mapping(Timestamp.class, LocalDateTime.class, Timestamp::toLocalDateTime),
		mapping(Timestamp.class, OffsetDateTime.class, Timestamp::toInstant, Mappings::toOffsetDateTime),
		mapping(Timestamp.class, ZonedDateTime.class, Timestamp::toInstant, Mappings::toZonedDateTime),

		/* *********************************************************************
		 * Other conversions.
		 * ********************************************************************/

		mapping(String.class, UUID.class, UUID::fromString)
	);

	private static LocalDate toLocalDate(final Instant instant) {
		return LocalDate.ofInstant(instant, ZoneId.systemDefault());
	}

	private static LocalDateTime toLocalDateTime(final Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	private static ZonedDateTime toZonedDateTime(final Instant instant) {
		return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	private static OffsetDateTime toOffsetDateTime(final Instant instant) {
		return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	private static final Map<Class<?>, Map<Class<?>, Function<?, ?>>>
		MAPPINGS = MAPPINGS_LIST.stream()
			.collect(groupingBy(
				Mapping::source,
				toMap(Mapping::target, Mapping::mapper)));

	private static <A, B> Mapping<A, B> mapping(
		final Class<A> source,
		final Class<B> target,
		final Function<A, B> mapper
	) {
		return new Mapping<>(source, target, mapper);
	}

	private static <A, B, C> Mapping<A, C> mapping(
		final Class<A> source,
		final Class<C> target,
		final Function<A, B> converter,
		final Function<B, C> mapper
	) {
		return new Mapping<>(source, target, mapper.compose(converter));
	}

	/**
	 * The mapping function. Return the mapper for the given {@code source} and
	 * {@code target} type, or {@code null} if no mapper is available.
	 *
	 * @param source the source type
	 * @param target the target type
	 * @return the mapping function, or {@code null}
	 */
	static Function<Object, Object>
	mapper(final Class<?> source, final Class<?> target) {
		final var sm = MAPPINGS.get(source);
		if (sm != null) {
			@SuppressWarnings("unchecked")
			final var tm = (Function<Object,  Object>)sm.get(target);
			return tm;
		} else {
			return null;
		}
	}

}
