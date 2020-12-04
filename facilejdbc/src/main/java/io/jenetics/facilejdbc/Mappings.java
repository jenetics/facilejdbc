package io.jenetics.facilejdbc;

import static java.lang.String.format;
import static java.util.Map.entry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

final class Mappings {

	static final Map<Class<?>, Map<Class<?>, Function<?, ?>>> MAPPINGS = Map.ofEntries(
		entry(BigDecimal.class, Map.ofEntries(
			entry(BigDecimal.class,(Function<BigDecimal, Object>)v -> v),
			entry(BigInteger.class,(Function<BigDecimal, Object>)BigDecimal::toBigInteger),
			entry(Double.class, (Function<BigDecimal, Object>)BigDecimal::doubleValue),
			entry(Float.class, (Function<BigDecimal, Object>)BigDecimal::floatValue),
			entry(Long.class, (Function<BigDecimal, Object>)BigDecimal::longValue),
			entry(Integer.class, (Function<BigDecimal, Object>)BigDecimal::intValue),
			entry(Short.class, (Function<BigDecimal, Object>)BigDecimal::shortValue)
		)),
		entry(BigInteger.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<BigInteger, Object>)BigDecimal::new),
			entry(BigInteger.class, (Function<BigInteger, Object>)v -> v),
			entry(Double.class, (Function<BigInteger, Object>)BigInteger::doubleValue),
			entry(Float.class, (Function<BigInteger, Object>)BigInteger::floatValue),
			entry(Long.class, (Function<BigInteger, Object>)BigInteger::longValue),
			entry(Integer.class, (Function<BigInteger, Object>)BigInteger::intValue),
			entry(Short.class, (Function<BigInteger, Object>)BigInteger::shortValue)
		)),
		entry(Boolean.class, Map.ofEntries(
			entry(Boolean.class, (Function<Boolean, Object>)v -> v),
			entry(Byte.class, (Function<Boolean, Object>)v -> v ? (byte)1 : (byte)0),
			entry(Long.class, (Function<Boolean, Object>)v -> v ? (long)1 : (long)0),
			entry(Integer.class, (Function<Boolean, Object>)v -> v ? 1 : 0),
			entry(Short.class, (Function<Boolean, Object>)v -> v ? (short)1 : (short)0)
		)),
		entry(Byte.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Byte, Object>)BigDecimal::valueOf),
			entry(BigInteger.class, (Function<Byte, Object>)BigInteger::valueOf),
			entry(Byte.class, (Function<Byte, Object>)v -> v),
			entry(Double.class, (Function<Byte, Object>)Byte::doubleValue),
			entry(Float.class, (Function<Byte, Object>)Byte::floatValue),
			entry(Long.class, (Function<Byte, Object>)Byte::longValue),
			entry(Integer.class, (Function<Byte, Object>)Byte::intValue),
			entry(Short.class, (Function<Byte, Object>)Byte::shortValue)
		)),
		entry(Double.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Double, Object>)BigDecimal::new),
			entry(BigInteger.class, (Function<Double, Object>)v -> BigInteger.valueOf(v.longValue())),
			entry(Byte.class, (Function<Double, Object>)Double::byteValue),
			entry(Double.class, (Function<Double, Object>)Double::doubleValue),
			entry(Float.class, (Function<Double, Object>)Double::floatValue),
			entry(Long.class, (Function<Double, Object>)Double::longValue),
			entry(Integer.class, (Function<Double, Object>)Double::intValue),
			entry(Short.class, (Function<Double, Object>)Double::shortValue)
		)),
		entry(Float.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Float, Object>)BigDecimal::new),
			entry(BigInteger.class, (Function<Float, Object>)v -> BigInteger.valueOf(v.longValue())),
			entry(Byte.class, (Function<Float, Object>)Float::byteValue),
			entry(Double.class, (Function<Float, Object>)Float::doubleValue),
			entry(Float.class, (Function<Float, Object>)Float::floatValue),
			entry(Long.class, (Function<Float, Object>)Float::longValue),
			entry(Integer.class, (Function<Float, Object>)Float::intValue),
			entry(Short.class, (Function<Float, Object>)Float::shortValue)
		)),
		entry(Long.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Long, Object>)BigDecimal::new),
			entry(BigInteger.class, (Function<Long, Object>)BigInteger::valueOf),
			entry(Byte.class, (Function<Long, Object>)Long::byteValue),
			entry(Double.class, (Function<Long, Object>)Long::doubleValue),
			entry(Float.class, (Function<Long, Object>)Long::floatValue),
			entry(Long.class, (Function<Long, Object>)Long::longValue),
			entry(Integer.class, (Function<Long, Object>)Long::intValue),
			entry(Short.class, (Function<Long, Object>)Long::shortValue)
		)),
		entry(Integer.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Integer, Object>)BigDecimal::new),
			entry(BigInteger.class, (Function<Integer, Object>)BigInteger::valueOf),
			entry(Byte.class, (Function<Integer, Object>)Integer::byteValue),
			entry(Double.class, (Function<Integer, Object>)Integer::doubleValue),
			entry(Float.class, (Function<Integer, Object>)Integer::floatValue),
			entry(Long.class, (Function<Integer, Object>)Integer::longValue),
			entry(Integer.class, (Function<Integer, Object>)Integer::intValue),
			entry(Short.class, (Function<Integer, Object>)Integer::shortValue)
		)),
		entry(Short.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Short, Object>)BigDecimal::new),
			entry(BigInteger.class, (Function<Short, Object>)BigInteger::valueOf),
			entry(Byte.class, (Function<Short, Object>)Short::byteValue),
			entry(Double.class, (Function<Short, Object>)Short::doubleValue),
			entry(Float.class, (Function<Short, Object>)Short::floatValue),
			entry(Long.class, (Function<Short, Object>)Short::longValue),
			entry(Integer.class, (Function<Short, Object>)Short::intValue),
			entry(Short.class, (Function<Short, Object>)Short::shortValue)
		))
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
