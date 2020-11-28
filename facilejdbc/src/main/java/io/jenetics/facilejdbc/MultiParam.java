package io.jenetics.facilejdbc;

import static java.util.Objects.requireNonNull;

import java.util.List;

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

}
