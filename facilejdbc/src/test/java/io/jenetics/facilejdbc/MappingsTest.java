package io.jenetics.facilejdbc;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MappingsTest {

	@Test
	public void map() {
		final var value = Mapping.DEFAULT.map(2, BigDecimal.class);
		System.out.println(value);
	}

	@Test(dataProvider = "numericalMappings")
	public void numericalMappings(final Object source, final Object target) {
		final var actual = Mapping.DEFAULT.map(source, target.getClass());
		Assert.assertEquals(actual, target);
	}

	@DataProvider(name = "numericalMappings")
	public Object[][] numericalMappings() {
		return new Object[][] {
			{BigDecimal.valueOf(1,0), BigInteger.valueOf(1)},
			{BigDecimal.valueOf(1,0), 1L},
			{BigDecimal.valueOf(1,0), 1},
			{BigDecimal.valueOf(1,0), (short)1},
			{BigDecimal.valueOf(1,0), (byte)1},
			{BigDecimal.valueOf(1,0), (double)1},
			{BigDecimal.valueOf(1,0), (float)1},

			{BigInteger.valueOf(2), BigDecimal.valueOf(2, 0)},
			{BigInteger.valueOf(2), 2L},
			{BigInteger.valueOf(2), 2},
			{BigInteger.valueOf(2), (short)2},
			{BigInteger.valueOf(2), (byte)2},
			{BigInteger.valueOf(2), (double)2},
			{BigInteger.valueOf(2), (float)2},

			{3L, BigDecimal.valueOf(3, 0)},
			{3L, BigInteger.valueOf(3)},
			{3L, 3},
			{3L, (short)3},
			{3L, (byte)3},
			{3L, (double)3},
			{3L, (float)3},

			{4, BigDecimal.valueOf(4, 0)},
			{4, BigInteger.valueOf(4)},
			{4, 4L},
			{4, (short)4},
			{4, (byte)4},
			{4, (double)4},
			{4, (float)4},

			{(short)5, BigDecimal.valueOf(5, 0)},
			{(short)5, BigInteger.valueOf(5)},
			{(short)5, 5L},
			{(short)5, 5},
			{(short)5, (byte)5},
			{(short)5, (double)5},
			{(short)5, (float)5},

			{(byte)6, BigDecimal.valueOf(6, 0)},
			{(byte)6, BigInteger.valueOf(6)},
			{(byte)6, 6L},
			{(byte)6, 6},
			{(byte)6, (short)6},
			{(byte)6, (double)6},
			{(byte)6, (float)6},

			{(float)7, BigDecimal.valueOf(7, 0)},
			{(float)7, BigInteger.valueOf(7)},
			{(float)7, 7L},
			{(float)7, 7},
			{(float)7, (short)7},
			{(float)7, (byte)7},
			{(float)7, (double)7},

			{(double)8, BigDecimal.valueOf(8, 0)},
			{(double)8, BigInteger.valueOf(8)},
			{(double)8, 8L},
			{(double)8, 8},
			{(double)8, (short)8},
			{(double)8, (byte)8},
			{(double)8, (float)8}
		};
	}

}
