package io.jenetics.facilejdbc;

import static java.util.Map.entry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

final class Jdbc2Java {

	final record Mapping(Class<?> jdbc, Class<?> java){}

	static final Map<Class<?>, Map<Class<?>, Function<?, ?>>> MAPPINGS = Map.ofEntries(
		entry(BigDecimal.class, Map.ofEntries(
			entry(BigDecimal.class,(Function<BigDecimal, ?>)v -> v),
			entry(BigInteger.class,(Function<BigDecimal, ?>)BigDecimal::toBigInteger),
			entry(Double.class, (Function<BigDecimal, ?>)BigDecimal::doubleValue),
			entry(double.class, (Function<BigDecimal, ?>)BigDecimal::doubleValue),
			entry(Float.class, (Function<BigDecimal, ?>)BigDecimal::floatValue),
			entry(float.class, (Function<BigDecimal, ?>)BigDecimal::floatValue),
			entry(Long.class, (Function<BigDecimal, ?>)BigDecimal::longValue),
			entry(long.class, (Function<BigDecimal, ?>)BigDecimal::longValue),
			entry(Integer.class, (Function<BigDecimal, ?>)BigDecimal::intValue),
			entry(int.class, (Function<BigDecimal, ?>)BigDecimal::intValue),
			entry(Short.class, (Function<BigDecimal, ?>)BigDecimal::shortValue),
			entry(short.class, (Function<BigDecimal, ?>)BigDecimal::shortValue)
		)),
		entry(BigInteger.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<BigInteger, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<BigInteger, ?>)v -> v),
			entry(Double.class, (Function<BigInteger, ?>)BigInteger::doubleValue),
			entry(double.class, (Function<BigInteger, ?>)BigInteger::doubleValue),
			entry(Float.class, (Function<BigInteger, ?>)BigInteger::floatValue),
			entry(float.class, (Function<BigInteger, ?>)BigInteger::floatValue),
			entry(Long.class, (Function<BigInteger, ?>)BigInteger::longValue),
			entry(long.class, (Function<BigInteger, ?>)BigInteger::longValue),
			entry(Integer.class, (Function<BigInteger, ?>)BigInteger::intValue),
			entry(int.class, (Function<BigInteger, ?>)BigInteger::intValue),
			entry(Short.class, (Function<BigInteger, ?>)BigInteger::shortValue),
			entry(short.class, (Function<BigInteger, ?>)BigInteger::shortValue)
		)),
		entry(Boolean.class, Map.ofEntries(
			entry(Boolean.class, (Function<Boolean, ?>)v -> v),
			entry(boolean.class, (Function<Boolean, ?>)v -> v),
			entry(Byte.class, (Function<Boolean, ?>)v -> v ? (byte)1 : (byte)0),
			entry(byte.class, (Function<Boolean, ?>)v -> v ? (byte)1 : (byte)0),
			entry(Long.class, (Function<Boolean, ?>)v -> v ? (long)1 : (long)0),
			entry(long.class, (Function<Boolean, ?>)v -> v ? (long)1 : (long)0),
			entry(Integer.class, (Function<Boolean, ?>)v -> v ? 1 : 0),
			entry(int.class, (Function<Boolean, ?>)v -> v ? 1 : 0),
			entry(Short.class, (Function<Boolean, ?>)v -> v ? (short)1 : (short)0),
			entry(short.class, (Function<Boolean, ?>)v -> v ? (short)1 : (short)0)
		)),
		entry(boolean.class, Map.ofEntries(
			entry(Boolean.class, (Function<Boolean, ?>)v -> v),
			entry(boolean.class, (Function<Boolean, ?>)v -> v),
			entry(Byte.class, (Function<Boolean, ?>)v -> v ? (byte)1 : (byte)0),
			entry(byte.class, (Function<Boolean, ?>)v -> v ? (byte)1 : (byte)0),
			entry(Long.class, (Function<Boolean, ?>)v -> v ? (long)1 : (long)0),
			entry(long.class, (Function<Boolean, ?>)v -> v ? (long)1 : (long)0),
			entry(Integer.class, (Function<Boolean, ?>)v -> v ? 1 : 0),
			entry(int.class, (Function<Boolean, ?>)v -> v ? 1 : 0),
			entry(Short.class, (Function<Boolean, ?>)v -> v ? (short)1 : (short)0),
			entry(short.class, (Function<Boolean, ?>)v -> v ? (short)1 : (short)0)
		)),
		entry(Byte.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Byte, ?>)BigDecimal::valueOf),
			entry(BigInteger.class, (Function<Byte, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Byte, ?>)v -> v),
			entry(byte.class, (Function<Byte, ?>)v -> v),
			entry(Double.class, (Function<Byte, ?>)Byte::doubleValue),
			entry(double.class, (Function<Byte, ?>)Byte::doubleValue),
			entry(Float.class, (Function<Byte, ?>)Byte::floatValue),
			entry(float.class, (Function<Byte, ?>)Byte::floatValue),
			entry(Long.class, (Function<Byte, ?>)Byte::longValue),
			entry(long.class, (Function<Byte, ?>)Byte::longValue),
			entry(Integer.class, (Function<Byte, ?>)Byte::intValue),
			entry(int.class, (Function<Byte, ?>)Byte::intValue),
			entry(Short.class, (Function<Byte, ?>)Byte::shortValue),
			entry(short.class, (Function<Byte, ?>)Byte::shortValue)
		)),
		entry(byte.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Byte, ?>)BigDecimal::valueOf),
			entry(BigInteger.class, (Function<Byte, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Byte, ?>)v -> v),
			entry(byte.class, (Function<Byte, ?>)v -> v),
			entry(Double.class, (Function<Byte, ?>)Byte::doubleValue),
			entry(double.class, (Function<Byte, ?>)Byte::doubleValue),
			entry(Float.class, (Function<Byte, ?>)Byte::floatValue),
			entry(float.class, (Function<Byte, ?>)Byte::floatValue),
			entry(Long.class, (Function<Byte, ?>)Byte::longValue),
			entry(long.class, (Function<Byte, ?>)Byte::longValue),
			entry(Integer.class, (Function<Byte, ?>)Byte::intValue),
			entry(int.class, (Function<Byte, ?>)Byte::intValue),
			entry(Short.class, (Function<Byte, ?>)Byte::shortValue),
			entry(short.class, (Function<Byte, ?>)Byte::shortValue)
		)),
		entry(Double.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Double, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Double, ?>)v -> BigInteger.valueOf(v.longValue())),
			entry(Byte.class, (Function<Double, ?>)Double::byteValue),
			entry(byte.class, (Function<Double, ?>)Double::byteValue),
			entry(Double.class, (Function<Double, ?>)Double::doubleValue),
			entry(double.class, (Function<Double, ?>)Double::doubleValue),
			entry(Float.class, (Function<Double, ?>)Double::floatValue),
			entry(float.class, (Function<Double, ?>)Double::floatValue),
			entry(Long.class, (Function<Double, ?>)Double::longValue),
			entry(long.class, (Function<Double, ?>)Double::longValue),
			entry(Integer.class, (Function<Double, ?>)Double::intValue),
			entry(int.class, (Function<Double, ?>)Double::intValue),
			entry(Short.class, (Function<Double, ?>)Double::shortValue),
			entry(short.class, (Function<Double, ?>)Double::shortValue)
		)),
		entry(double.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Double, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Double, ?>)v -> BigInteger.valueOf(v.longValue())),
			entry(Byte.class, (Function<Double, ?>)Double::byteValue),
			entry(byte.class, (Function<Double, ?>)Double::byteValue),
			entry(Double.class, (Function<Double, ?>)Double::doubleValue),
			entry(double.class, (Function<Double, ?>)Double::doubleValue),
			entry(Float.class, (Function<Double, ?>)Double::floatValue),
			entry(float.class, (Function<Double, ?>)Double::floatValue),
			entry(Long.class, (Function<Double, ?>)Double::longValue),
			entry(long.class, (Function<Double, ?>)Double::longValue),
			entry(Integer.class, (Function<Double, ?>)Double::intValue),
			entry(int.class, (Function<Double, ?>)Double::intValue),
			entry(Short.class, (Function<Double, ?>)Double::shortValue),
			entry(short.class, (Function<Double, ?>)Double::shortValue)
		)),
		entry(Float.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Float, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Float, ?>)v -> BigInteger.valueOf(v.longValue())),
			entry(Byte.class, (Function<Float, ?>)Float::byteValue),
			entry(byte.class, (Function<Float, ?>)Float::byteValue),
			entry(Double.class, (Function<Float, ?>)Float::doubleValue),
			entry(double.class, (Function<Float, ?>)Float::doubleValue),
			entry(Float.class, (Function<Float, ?>)Float::floatValue),
			entry(float.class, (Function<Float, ?>)Float::floatValue),
			entry(Long.class, (Function<Float, ?>)Float::longValue),
			entry(long.class, (Function<Float, ?>)Float::longValue),
			entry(Integer.class, (Function<Float, ?>)Float::intValue),
			entry(int.class, (Function<Float, ?>)Float::intValue),
			entry(Short.class, (Function<Float, ?>)Float::shortValue),
			entry(short.class, (Function<Float, ?>)Float::shortValue)
		)),
		entry(float.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Float, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Float, ?>)v -> BigInteger.valueOf(v.longValue())),
			entry(Byte.class, (Function<Float, ?>)Float::byteValue),
			entry(byte.class, (Function<Float, ?>)Float::byteValue),
			entry(Double.class, (Function<Float, ?>)Float::doubleValue),
			entry(double.class, (Function<Float, ?>)Float::doubleValue),
			entry(Float.class, (Function<Float, ?>)Float::floatValue),
			entry(float.class, (Function<Float, ?>)Float::floatValue),
			entry(Long.class, (Function<Float, ?>)Float::longValue),
			entry(long.class, (Function<Float, ?>)Float::longValue),
			entry(Integer.class, (Function<Float, ?>)Float::intValue),
			entry(int.class, (Function<Float, ?>)Float::intValue),
			entry(Short.class, (Function<Float, ?>)Float::shortValue),
			entry(short.class, (Function<Float, ?>)Float::shortValue)
		)),
		entry(Long.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Long, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Long, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Long, ?>)Long::byteValue),
			entry(byte.class, (Function<Long, ?>)Long::byteValue),
			entry(Double.class, (Function<Long, ?>)Long::doubleValue),
			entry(double.class, (Function<Long, ?>)Long::doubleValue),
			entry(Float.class, (Function<Long, ?>)Long::floatValue),
			entry(float.class, (Function<Long, ?>)Long::floatValue),
			entry(Long.class, (Function<Long, ?>)Long::longValue),
			entry(long.class, (Function<Long, ?>)Long::longValue),
			entry(Integer.class, (Function<Long, ?>)Long::intValue),
			entry(int.class, (Function<Long, ?>)Long::intValue),
			entry(Short.class, (Function<Long, ?>)Long::shortValue),
			entry(short.class, (Function<Long, ?>)Long::shortValue)
		)),
		entry(long.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Long, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Long, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Long, ?>)Long::byteValue),
			entry(byte.class, (Function<Long, ?>)Long::byteValue),
			entry(Double.class, (Function<Long, ?>)Long::doubleValue),
			entry(double.class, (Function<Long, ?>)Long::doubleValue),
			entry(Float.class, (Function<Long, ?>)Long::floatValue),
			entry(float.class, (Function<Long, ?>)Long::floatValue),
			entry(Long.class, (Function<Long, ?>)Long::longValue),
			entry(long.class, (Function<Long, ?>)Long::longValue),
			entry(Integer.class, (Function<Long, ?>)Long::intValue),
			entry(int.class, (Function<Long, ?>)Long::intValue),
			entry(Short.class, (Function<Long, ?>)Long::shortValue),
			entry(short.class, (Function<Long, ?>)Long::shortValue)
		)),
		entry(Integer.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Integer, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Integer, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Integer, ?>)Integer::byteValue),
			entry(byte.class, (Function<Integer, ?>)Integer::byteValue),
			entry(Double.class, (Function<Integer, ?>)Integer::doubleValue),
			entry(double.class, (Function<Integer, ?>)Integer::doubleValue),
			entry(Float.class, (Function<Integer, ?>)Integer::floatValue),
			entry(float.class, (Function<Integer, ?>)Integer::floatValue),
			entry(Long.class, (Function<Integer, ?>)Integer::longValue),
			entry(long.class, (Function<Integer, ?>)Integer::longValue),
			entry(Integer.class, (Function<Integer, ?>)Integer::intValue),
			entry(int.class, (Function<Integer, ?>)Integer::intValue),
			entry(Short.class, (Function<Integer, ?>)Integer::shortValue),
			entry(short.class, (Function<Integer, ?>)Integer::shortValue)
		)),
		entry(int.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Integer, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Integer, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Integer, ?>)Integer::byteValue),
			entry(byte.class, (Function<Integer, ?>)Integer::byteValue),
			entry(Double.class, (Function<Integer, ?>)Integer::doubleValue),
			entry(double.class, (Function<Integer, ?>)Integer::doubleValue),
			entry(Float.class, (Function<Integer, ?>)Integer::floatValue),
			entry(float.class, (Function<Integer, ?>)Integer::floatValue),
			entry(Long.class, (Function<Integer, ?>)Integer::longValue),
			entry(long.class, (Function<Integer, ?>)Integer::longValue),
			entry(Integer.class, (Function<Integer, ?>)Integer::intValue),
			entry(int.class, (Function<Integer, ?>)Integer::intValue),
			entry(Short.class, (Function<Integer, ?>)Integer::shortValue),
			entry(short.class, (Function<Integer, ?>)Integer::shortValue)
		)),
		entry(Short.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Short, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Short, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Short, ?>)Short::byteValue),
			entry(byte.class, (Function<Short, ?>)Short::byteValue),
			entry(Double.class, (Function<Short, ?>)Short::doubleValue),
			entry(double.class, (Function<Short, ?>)Short::doubleValue),
			entry(Float.class, (Function<Short, ?>)Short::floatValue),
			entry(float.class, (Function<Short, ?>)Short::floatValue),
			entry(Long.class, (Function<Short, ?>)Short::longValue),
			entry(long.class, (Function<Short, ?>)Short::longValue),
			entry(Integer.class, (Function<Short, ?>)Short::intValue),
			entry(int.class, (Function<Short, ?>)Short::intValue),
			entry(Short.class, (Function<Short, ?>)Short::shortValue),
			entry(short.class, (Function<Short, ?>)Short::shortValue)
		)),
		entry(short.class, Map.ofEntries(
			entry(BigDecimal.class, (Function<Short, ?>)BigDecimal::new),
			entry(BigInteger.class, (Function<Short, ?>)BigInteger::valueOf),
			entry(Byte.class, (Function<Short, ?>)Short::byteValue),
			entry(byte.class, (Function<Short, ?>)Short::byteValue),
			entry(Double.class, (Function<Short, ?>)Short::doubleValue),
			entry(double.class, (Function<Short, ?>)Short::doubleValue),
			entry(Float.class, (Function<Short, ?>)Short::floatValue),
			entry(float.class, (Function<Short, ?>)Short::floatValue),
			entry(Long.class, (Function<Short, ?>)Short::longValue),
			entry(long.class, (Function<Short, ?>)Short::longValue),
			entry(Integer.class, (Function<Short, ?>)Short::intValue),
			entry(int.class, (Function<Short, ?>)Short::intValue),
			entry(Short.class, (Function<Short, ?>)Short::shortValue),
			entry(short.class, (Function<Short, ?>)Short::shortValue)
		))
	);

	static Object toJava(final Object value) {
		if (value != null) {
			return null;
		} else {
			return null;
		}
	}

}
