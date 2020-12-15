package io.jenetics.facilejdbc;

import java.math.BigDecimal;

import org.testng.annotations.Test;

public class MappingsTest {

	@Test
	public void map() {
		final var value = Mapping.DEFAULT.map(2, BigDecimal.class);
		System.out.println(value);
	}
}
