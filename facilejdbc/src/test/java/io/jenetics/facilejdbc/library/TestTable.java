package io.jenetics.facilejdbc.library;

public record TestTable(String stringValue, Integer intValue, Float floatValue) {

//	public static final Ctor<TestTable> CTOR = Ctor.of(TestTable.class);
//	public static final Dctor<TestTable> DCTOR = Dctor.of(TestTable.class);
//
//	public static final Query INSERT = Query.of("""
//		INSERT INTO test_table(string_value, int_value, float_value)
//		VALUES(:string_value, :int_value, :float_value);
//		"""
//	);
//
//	public static final Query SELECT = Query.of("SELECT * FROM test_table;");
//
//	public static TestTable next(final Random random) {
//		return new TestTable(
//			"value_" + Math.abs(random.nextInt()),
//			random.nextInt(),
//			(float)random.nextDouble()
//		);
//	}

}
