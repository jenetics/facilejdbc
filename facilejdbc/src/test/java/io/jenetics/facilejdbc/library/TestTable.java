package io.jenetics.facilejdbc.library;

import java.util.random.RandomGenerator;

import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import io.jenetics.facilejdbc.RowParser;

public record TestTable(String stringValue, Integer intValue, Double floatValue) {

	public static final RowParser<TestTable> PARSER = RowParser.of(TestTable.class);
	public static final Dctor<TestTable> DCTOR = Dctor.of(TestTable.class);

	public static final Query INSERT = Query.of("""
		INSERT INTO test_table(string_value, int_value, float_value)
		VALUES(:string_value, :int_value, :float_value);
		"""
	);

	public static final Query SELECT = Query.of("SELECT * FROM test_table;");

	public static TestTable next(final RandomGenerator random) {
		return new TestTable(
			"value_" + Math.abs(random.nextInt()),
			random.nextInt(),
			random.nextDouble()
		);
	}

}
