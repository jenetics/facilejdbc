package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

import static io.jenetics.facilejdbc.spi.SqlTypeMapper.map;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MultiParam extends BaseParam {

	/**
	 * Return the parameter value.
	 *
	 * @return the parameter value
	 */
	List<ParamValue> values();

	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/


	static MultiParam of(final String name, final List<ParamValue> values) {
		requireNonNull(name);
		final var vals = List.copyOf(values);

		return new MultiParam() {
			@Override
			public String name() {
				return name;
			}
			@Override
			public List<ParamValue> values() {
				return vals;
			}
		};
	}

	static MultiParam values(final String name, final Object... values) {
		return of(
			name,
			Stream.of(values)
				.map(v -> (ParamValue)(index, stmt) -> stmt.setObject(index, map(v)))
				.collect(Collectors.toUnmodifiableList())
		);
	}

}
