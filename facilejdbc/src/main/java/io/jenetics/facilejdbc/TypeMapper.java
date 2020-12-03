package io.jenetics.facilejdbc;

import java.sql.PreparedStatement;
import java.sql.Statement;

final class TypeMapper {
	private TypeMapper() {
	}


	<T> T read(final Statement stmt, final String name) {
		return null;
	}

	interface RecordField<T> {
		T get(final String name, final Row row);
	}

}
