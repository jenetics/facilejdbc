package io.jenetics.facilejdbc;

import java.util.function.Function;

@FunctionalInterface
public interface Ctor<T> {

	interface Column<C> {
		String name();
		RowParser<C> value();

		static <C> Column<C> of(final String name, final RowParser<C> value) {
			return new Column<C>() {
				@Override
				public String name() {
					return name;
				}

				@Override
				public RowParser<C> value() {
					return value;
				}
			};
		}

	}

	T apply(
		final Function<? super Object[], ? extends T> ctr,
		final Column<?>[] columns
	);

}
