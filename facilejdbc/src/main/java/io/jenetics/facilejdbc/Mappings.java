package io.jenetics.facilejdbc;

import static java.lang.String.format;
import static java.util.Map.entry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

final class Mappings {

	static final Map<Class<?>, Map<Class<?>, Function<?, ?>>> MAPPINGS = Map.ofEntries(
		// Mapping of numerical types.
		entry(BigDecimal.class, Map.of(
			BigDecimal.class,(Function<BigDecimal, Object>)v -> v,
			BigInteger.class,(Function<BigDecimal, Object>)BigDecimal::toBigInteger,
			Double.class, (Function<BigDecimal, Object>)BigDecimal::doubleValue,
			Float.class, (Function<BigDecimal, Object>)BigDecimal::floatValue,
			Long.class, (Function<BigDecimal, Object>)BigDecimal::longValue,
			Integer.class, (Function<BigDecimal, Object>)BigDecimal::intValue,
			Short.class, (Function<BigDecimal, Object>)BigDecimal::shortValue
		)),
		entry(BigInteger.class, Map.of(
			BigDecimal.class, (Function<BigInteger, Object>)BigDecimal::new,
			BigInteger.class, (Function<BigInteger, Object>)v -> v,
			Double.class, (Function<BigInteger, Object>)BigInteger::doubleValue,
			Float.class, (Function<BigInteger, Object>)BigInteger::floatValue,
			Long.class, (Function<BigInteger, Object>)BigInteger::longValue,
			Integer.class, (Function<BigInteger, Object>)BigInteger::intValue,
			Short.class, (Function<BigInteger, Object>)BigInteger::shortValue
		)),
		entry(Boolean.class, Map.of(
			Boolean.class, (Function<Boolean, Object>)v -> v,
			Byte.class, (Function<Boolean, Object>)v -> v ? (byte)1 : (byte)0,
			Long.class, (Function<Boolean, Object>)v -> v ? (long)1 : (long)0,
			Integer.class, (Function<Boolean, Object>)v -> v ? 1 : 0,
			Short.class, (Function<Boolean, Object>)v -> v ? (short)1 : (short)0
		)),
		entry(Byte.class, Map.of(
			BigDecimal.class, (Function<Byte, Object>)BigDecimal::valueOf,
			BigInteger.class, (Function<Byte, Object>)BigInteger::valueOf,
			Byte.class, (Function<Byte, Object>)v -> v,
			Double.class, (Function<Byte, Object>)Byte::doubleValue,
			Float.class, (Function<Byte, Object>)Byte::floatValue,
			Long.class, (Function<Byte, Object>)Byte::longValue,
			Integer.class, (Function<Byte, Object>)Byte::intValue,
			Short.class, (Function<Byte, Object>)Byte::shortValue
		)),
		entry(Double.class, Map.of(
			BigDecimal.class, (Function<Double, Object>)BigDecimal::new,
			BigInteger.class, (Function<Double, Object>)v -> BigInteger.valueOf(v.longValue()),
			Byte.class, (Function<Double, Object>)Double::byteValue,
			Double.class, (Function<Double, Object>)Double::doubleValue,
			Float.class, (Function<Double, Object>)Double::floatValue,
			Long.class, (Function<Double, Object>)Double::longValue,
			Integer.class, (Function<Double, Object>)Double::intValue,
			Short.class, (Function<Double, Object>)Double::shortValue
		)),
		entry(Float.class, Map.of(
			BigDecimal.class, (Function<Float, Object>)BigDecimal::new,
			BigInteger.class, (Function<Float, Object>)v -> BigInteger.valueOf(v.longValue()),
			Byte.class, (Function<Float, Object>)Float::byteValue,
			Double.class, (Function<Float, Object>)Float::doubleValue,
			Float.class, (Function<Float, Object>)Float::floatValue,
			Long.class, (Function<Float, Object>)Float::longValue,
			Integer.class, (Function<Float, Object>)Float::intValue,
			Short.class, (Function<Float, Object>)Float::shortValue
		)),
		entry(Long.class, Map.of(
			BigDecimal.class, (Function<Long, Object>)BigDecimal::new,
			BigInteger.class, (Function<Long, Object>)BigInteger::valueOf,
			Byte.class, (Function<Long, Object>)Long::byteValue,
			Double.class, (Function<Long, Object>)Long::doubleValue,
			Float.class, (Function<Long, Object>)Long::floatValue,
			Long.class, (Function<Long, Object>)Long::longValue,
			Integer.class, (Function<Long, Object>)Long::intValue,
			Short.class, (Function<Long, Object>)Long::shortValue
		)),
		entry(Integer.class, Map.of(
			BigDecimal.class, (Function<Integer, Object>)BigDecimal::new,
			BigInteger.class, (Function<Integer, Object>)BigInteger::valueOf,
			Byte.class, (Function<Integer, Object>)Integer::byteValue,
			Double.class, (Function<Integer, Object>)Integer::doubleValue,
			Float.class, (Function<Integer, Object>)Integer::floatValue,
			Long.class, (Function<Integer, Object>)Integer::longValue,
			Integer.class, (Function<Integer, Object>)Integer::intValue,
			Short.class, (Function<Integer, Object>)Integer::shortValue
		)),
		entry(Short.class, Map.of(
			BigDecimal.class, (Function<Short, Object>)BigDecimal::new,
			BigInteger.class, (Function<Short, Object>)BigInteger::valueOf,
			Byte.class, (Function<Short, Object>)Short::byteValue,
			Double.class, (Function<Short, Object>)Short::doubleValue,
			Float.class, (Function<Short, Object>)Short::floatValue,
			Long.class, (Function<Short, Object>)Short::longValue,
			Integer.class, (Function<Short, Object>)Short::intValue,
			Short.class, (Function<Short, Object>)Short::shortValue
		))

		// Mapping of time types.

	);

	static <T> T mapTo(final Class<T> targetType, final Object value) {
		if (value != null) {
			final var sourceType = value.getClass();
			if (targetType == sourceType) {
				return targetType.cast(value);
			}

			final var sourceMapper = MAPPINGS.get(sourceType);
			if (sourceMapper != null) {
				@SuppressWarnings("unchecked")
				final var targetMapper = (Function<Object,  Object>)sourceMapper.get(targetType);
				if (targetMapper != null) {
					return targetType.cast(targetMapper.apply(value));
				}
			}

			throw new ClassCastException(format(
				"Mapping (%s -> %s) not supported for '%s'.",
				sourceType.getName(), targetType.getName(), value
			));
		} else {
			return null;
		}
	}

}
