package io.jenetics.facilejdbc;

import java.util.List;
import java.util.function.Function;

public interface Ctor<T> {

	final record Field<T>(String name, T value){}

	T apply(final Function<? super List<Field<?>>, ? extends T> ctor);

}
