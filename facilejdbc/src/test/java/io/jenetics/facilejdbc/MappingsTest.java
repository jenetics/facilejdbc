package io.jenetics.facilejdbc;

import java.math.BigDecimal;

import org.testng.annotations.Test;

public class MappingsTest {

	@Test
	public void map() {
		final var value = Mappings.mapTo(BigDecimal.class, 2);
		System.out.println(value);

		int count = 0;
		for (var m : Mappings.MAPPINGS.values()) {
			for (var n : m.values()) {
				++count;
			}
		}
		System.out.println(count);
	}
}
