/*
 * Facile JDBC Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.facilejdbc;

import static java.lang.String.format;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.jenetics.facilejdbc.Lifecycle.Value;

/**
 * A {@code Query} represents an executable piece of SQL text.
 *
 * <pre>{@code
 * private static final Query SELECT = Query.of("""
 *     SELECT * FROM person
 *     WHERE forename like :forename
 *     ORDER BY surname;
 *     """
 * );
 *
 * private static final Query INSERT = Query.of("""
 *     INSERT INTO person(forename, surname, birthday, email)
 *     VALUES(:forename, :surname, :birthday, :email);
 *     """
 * );
 * }</pre>
 *
 * @apiNote
 * This class is immutable and thread-safe.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 2.0
 * @since 1.0
 */
public final class Query implements Serializable {

	@java.io.Serial
	private static final long serialVersionUID = 1;

	private final Sql sql;
	private final ParamValues values;

	private final Integer fetchSize;
	private final Duration timeout;

	private Query(
		final Sql sql,
		final ParamValues values,
		final Integer fetchSize,
		final Duration timeout
	) {
		this.sql = requireNonNull(sql);
		this.values = requireNonNull(values);
		this.fetchSize = fetchSize;
		this.timeout = timeout;
	}

	/**
	 * Return the prepared SQL string. All parameter names have been replaced
	 * with '?' placeholders.
	 *
	 * @return the prepared SQL string
	 */
	public String sql() {
		return sql.string();
	}

	/**
	 * Return the original SQL string, this object is created with. So the
	 * following assertion holds for every possible SQL string;
	 * <pre>{@code
	 * final String sql = "SELECT * FROM table WHERE id = :id;";
	 * final Query query = Query.of(sql);
	 * assert sql.equals(query.rawSql());
	 * }</pre>
	 *
	 * @since 1.1
	 *
	 * @return the original SQL string
	 */
	public String rawSql() {
		return sql.sql();
	}

	/**
	 * Return the list of parsed parameter names. The list may be empty or
	 * contain duplicate entries, depending on the input string. The list is
	 * in exactly the order they appeared in the SQL string and can be used for
	 * determining the parameter index for the {@link PreparedStatement}.
	 *
	 * @return the parsed parameter names
	 */
	public List<String> paramNames() {
		return sql.paramNames();
	}

	/**
	 * Return the number of result set rows that is the default fetch size for
	 * {@link ResultSet} object created when {@code this} query is executed.
	 *
	 * @since 1.2
	 *
	 * @return the default fetch size for result sets generated from {@code this}
	 *         query object
	 */
	public Optional<Integer> fetchSize() {
		return Optional.ofNullable(fetchSize);
	}

	/**
	 * Return the query timeout.
	 *
	 * @since 1.2
	 *
	 * @return the query timeout
	 */
	public Optional<Duration> timeout() {
		return Optional.ofNullable(timeout);
	}

	/**
	 * Gives the JDBC driver a hint as to the number of rows that should be
	 * fetched from the database when more rows are needed. If the value
	 * specified is zero, then the hint is ignored. The default value is zero.
	 *
	 * @since 1.2
	 *
	 * @see Statement#setFetchSize(int)
	 *
	 * @param fetchSize the number of rows to fetch
	 * @return a new query object with the given fetch size set
	 * @throws IllegalArgumentException if {@code fetchSize < 0}
	 */
	public Query withFetchSize(final int fetchSize) {
		if (fetchSize < 0) {
			throw new IllegalArgumentException(
				"Fetch size must be positive: %s.".formatted(fetchSize)
			);
		}

		return new Query(sql, values, fetchSize, timeout);
	}

	/**
	 * Sets the timeout the driver will wait for a {@link Statement} object to
	 * execute. By default, there is no limit on the amount of time allowed for a
	 * running statement to complete. If the limit is exceeded, an
	 * {@link SQLTimeoutException} is thrown. A JDBC driver must apply this
	 * limit to execute, {@link Statement#executeQuery(String)} and
	 * {@link Statement#executeUpdate(String)} methods.
	 *
	 * @since 1.2
	 *
	 * @see Statement#setQueryTimeout(int)
	 *
	 * @param timeout the query timeout
	 * @return a new query object with the given query {@code timeout}
	 */
	public Query withTimeout(final Duration timeout) {
		return new Query(sql, values, fetchSize, timeout);
	}

	/* *************************************************************************
	 * Query parameter setting.
	 * ************************************************************************/

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = :id;")
	 *     .on(List.of(Param.value("id", 43245))
	 *     .as(PARSER.singleOpt(), conn);
	 * }</pre>
	 *
	 * @see #on(Param...)
	 * @see #on(Map)
	 * @see #on(Object, Dctor)
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 */
	public Query on(final Iterable<? extends Param> params) {
		final List<SingleParam> singleParams = new ArrayList<>();
		final List<MultiParam> multiParams = new ArrayList<>();
		for (var param : params) {

			if (param instanceof SingleParam p) {
				singleParams.add(p);
			} else if (param instanceof MultiParam p) {
				multiParams.add(p);
			} else {
				throw new AssertionError(format(
					"Type '%s' not expected.", param.getClass().getName()
				));
			}
		}

		return onSingleParam(singleParams).onMultiParam(multiParams);
	}

	private Query onSingleParam(final List<SingleParam> params) {
		return params.isEmpty()
			? this
			: new Query(sql, values.andThen(new Params(params)), fetchSize, timeout);
	}

	private Query onMultiParam(final List<MultiParam> params) {
		if (params.isEmpty()) {
			return this;
		} else {
			final Sql sql = params.stream()
				.reduce(
					this.sql,
					(s, p) -> s.expand(p.name(), p.values().size()),
					(s1, s2) -> { throw new AssertionError(); });

			final ParamValues values = this.values.andThen(
				new Params(
					params.stream()
						.flatMap(Query::toParams)
						.collect(Collectors.toList())
				)
			);

			return new Query(sql, values, fetchSize, timeout);
		}
	}

	private static Stream<SingleParam> toParams(final MultiParam param) {
		final List<ParamValue> values = param.values();
		return IntStream.range(0, values.size())
			.mapToObj(i -> SingleParam.of(Sql.name(param.name(), i), values.get(i)));
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = :id;")
	 *     .on(Param.value("id", 43245)
	 *     .as(PARSER.singleOpt(), conn);
	 * }</pre>
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 * @throws IllegalArgumentException if another type then {@link SingleParam}
	 *         or {@link MultiParam} is given
	 */
	public Query on(final Param... params) {
		return on(asList(params));
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * <pre>{@code
	 * final var result = Query.of("SELECT * FROM table WHERE id = :id;")
	 *     .on(Map.of("id", 43245))
	 *     .as(PARSER.singleOpt(), conn);
	 * }</pre>
	 *
	 * @param params the query parameters
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code params} is {@code null}
	 */
	public Query on(final Map<String, ?> params) {
		return on(
			params.entrySet().stream()
				.map(e -> Param.value(e.getKey(), e.getValue()))
				.toList()
		);
	}

	/**
	 * Return a new query object with the given query parameter values.
	 *
	 * @param record the query parameters
	 * @param dctor the deconstructor used to <em>split</em> the parameters
	 * @param <T> the parameter record type
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public <T> Query on(final T record, final Dctor<? super T> dctor) {
		requireNonNull(record);
		requireNonNull(dctor);

		final ParamValues values = (params, stmt) -> dctor
			.unapply(record, stmt.getConnection())
			.set(params, stmt);

		return new Query(sql, this.values.andThen(values), fetchSize, timeout);
	}

	/**
	 * Return a new query object with the given query parameter values. They are
	 * automatically extracted from the record components.
	 *
	 * @since 2.0
	 *
	 * @see Dctor#of(Class, Dctor.Field[])
	 *
	 * @param record the query parameters
	 * @param <T> the parameter record type
	 * @return a new query object with the set parameters
	 * @throws NullPointerException if the given {@code record} is {@code null}
	 */
	public <T extends Record> Query on(final T record) {
		@SuppressWarnings("unchecked")
		final var type = (Class<T>)record.getClass();
		return on(record, Dctor.of(type));
	}


	/* *************************************************************************
	 * Executing query.
	 * ************************************************************************/

	/**
	 * Executes {@code this} query and parses the result with the given
	 * result-set parser.
	 *
	 * @see PreparedStatement#executeQuery()
	 *
	 * @param parser the parser which converts the query result to the desired
	 *        type
	 * @param conn the DB connection where {@code this} query is executed on
	 * @param <T> the result type
	 * @return the query result, parsed to the desired type
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given result parser or connection is
	 *         {@code null}
	 */
	public <T> T as(
		final ResultSetParser<? extends T> parser,
		final Connection conn
	)
		throws SQLException
	{
		final Value<T, SQLException> result = Value.build(resources -> {
			final var stmt = resources.add(prepare(conn), Statement::close);
			final var rs = resources.add(stmt.executeQuery(), ResultSet::close);
			return parser.parse(rs, conn);
		});

		return prepare(result);
	}

	@SuppressWarnings("unchecked")
	private static <T> T prepare(final Value<T, SQLException> result)
		throws SQLException
	{
		if (result.get() instanceof Stream<?> stream) {
			return (T)stream.onClose(() ->
				result.uncheckedClose(UncheckedSQLException::new)
			);
		} else {
			try (result) {
				return result.get();
			}
		}
	}

	private PreparedStatement prepare(final Connection conn)
		throws SQLException
	{
		final PreparedStatement stmt = conn.prepareStatement(sql());
		values.set(paramNames(), stmt);
		setParams(stmt);
		return stmt;
	}

	private void setParams(final Statement stmt) throws SQLException {
		if (fetchSize != null) {
			stmt.setFetchSize(fetchSize);
		}
		if (timeout != null) {
			stmt.setQueryTimeout((int)timeout.toSeconds());
		}
	}

	/**
	 * Executes the SQL statement defined by {@code this} query object, which
	 * may be any kind of SQL statement.
	 *
	 * @see PreparedStatement#execute()
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return {@code true} if the first result is a {@link java.sql.ResultSet}
	 *         object; {@code false} if the first result is an update count or
	 *         there is no result
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given connection is {@code null}
	 */
	public boolean execute(final Connection conn) throws SQLException {
		try (var stmt = prepare(conn)) {
			return stmt.execute();
		}
	}

	/**
	 * Executes the SQL statement in a {@link PreparedStatement} object, which
	 * must be an SQL Data Manipulation Language (DML) statement, such as
	 * {@code INSERT}, {@code UPDATE} or {@code DELETE}; or an SQL statement
	 * that returns nothing, such as a DDL statement.
	 *
	 * @see PreparedStatement#executeUpdate()
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return either (1) the row count for SQL Data Manipulation Language (DML)
	 *         statements or (2) 0 for SQL statements that return nothing
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if the given connection is {@code null}
	 */
	public int executeUpdate(final Connection conn) throws SQLException {
		try (var stmt = prepare(conn)) {
			return stmt.executeUpdate();
		}
	}

	/**
	 * Executes the SQL statement in a {@link PreparedStatement} object, which
	 * must be an SQL {@code INSERT}. It returns, the optionally generated, key
	 * for the inserted row.
	 *
	 * @see PreparedStatement#executeUpdate()
	 * @see #executeInsert(Connection)
	 *
	 * @param keyParser the row parser for the key to return
	 * @param conn the DB connection where {@code this} query is executed on
	 * @param <K> the generated key type
	 * @return the generated key, if available
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public <K> Optional<K> executeInsert(
		final RowParser<? extends K> keyParser,
		final Connection conn
	)
		throws SQLException
	{
		try (var stmt = prepareInsert(conn)) {
			stmt.executeUpdate();
			return readId(keyParser, stmt, conn);
		}
	}

	private PreparedStatement prepareInsert(final Connection conn)
		throws SQLException
	{
		final var stmt = conn.prepareStatement(
			sql.string(),
			RETURN_GENERATED_KEYS
		);

		values.set(paramNames(), stmt);
		setParams(stmt);
		return stmt;
	}

	private static <K> Optional<K> readId(
		final RowParser<? extends K> keyParser,
		final Statement stmt,
		final Connection conn
	)
		throws SQLException
	{
		try (ResultSet keys = stmt.getGeneratedKeys()) {
			@SuppressWarnings("unchecked")
			final var key = (Optional<K>)keyParser.singleOpt().parse(keys, conn);
			return key;
		}
	}

	/**
	 * Executes the SQL statement in a {@link PreparedStatement} object, which
	 * must be an SQL {@code INSERT}. It returns, the optionally generated, key
	 * for the inserted row.
	 *
	 * @see PreparedStatement#executeUpdate()
	 * @see #executeInsert(RowParser, Connection)
	 *
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return the generated key, if available
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public Optional<Long> executeInsert(final Connection conn)
		throws SQLException
	{
		return executeInsert(RowParser.int64(1), conn);
	}


	/* *************************************************************************
	 * Batch query.
	 * ************************************************************************/

	/**
	 * Executes the given batch on {@code this} query object, which may be any
	 * kind of SQL statement.
	 *
	 * @see PreparedStatement#execute()
	 * @see #execute(Connection)
	 * @see #executeUpdate(Batch, Connection)
	 *
	 * @param batch the batch to execute
	 * @param conn the DB connection where {@code this} query is executed on
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public void execute(final Batch batch, final Connection conn)
		throws SQLException
	{
		try (var query = prepareQuery(conn)) {
			query.execute(batch);
		}
	}

	/**
	 * Executes the given {@code batch} for this query, which must be an SQL
	 * Data Manipulation Language (DML) statement, such as {@code INSERT},
	 * {@code UPDATE} or {@code DELETE}.
	 *
	 * @see PreparedStatement#executeUpdate()
	 * @see #executeUpdate(Connection)
	 * @see #execute(Batch, Connection)
	 *
	 * @param batch the batch to execute
	 * @param conn the DB connection where {@code this} query is executed on
	 * @return an int[] with the update counts
	 * @throws SQLException if a database access error occurs
	 * @throws java.sql.SQLTimeoutException when the driver has determined that
	 *         the timeout value has been exceeded
	 * @throws NullPointerException if one of the arguments is {@code null}
	 */
	public int[] executeUpdate(final Batch batch, final Connection conn)
		throws SQLException
	{
		try (var query = prepareQuery(conn)) {
			return query.executeUpdate(batch);
		}
	}

	/**
	 * Return a prepared query from {@code this} query object. The returned
	 * query has the given connection attached.
	 *
	 * @since 2.1
	 *
	 * @param conn the connection used by the created prepared query
	 * @return a new prepared query
	 * @throws SQLException if preparing the query fails
	 */
	public PreparedQuery prepareQuery(final Connection conn) throws SQLException {
		return new PreparedQuery(prepare(conn), paramNames());
	}

	@Override
	public String toString() {
		return sql();
	}

	/* *************************************************************************
	 * Static factory methods.
	 * ************************************************************************/

	/**
	 * Create a new query object from the given SQL string.
	 * <pre>{@code
	 * private static final Query SELECT = Query.of("""
	 *     SELECT * FROM person
	 *     WHERE forename like :forename
	 *     ORDER BY surname;
	 *     """
	 * );
	 *
	 * private static final Query INSERT = Query.of("""
	 *     INSERT INTO person(forename, surname, birthday, email)
	 *     VALUES(:forename, :surname, :birthday, :email);
	 *     """
	 * );
	 * }</pre>
	 *
	 * @param sql the SQL string of the created query
	 * @return a new query object from the given SQL string
	 * @throws NullPointerException if the given SQL string is {@code null}
	 */
	public static Query of(final String sql) {
		return new Query(Sql.of(sql), ParamValues.EMPTY, null, null);
	}


	/* *************************************************************************
	 *  Java object serialization
	 * ************************************************************************/

	@java.io.Serial
	private Object writeReplace() {
		return new Serial(this);
	}

	@java.io.Serial
	private void readObject(final ObjectInputStream stream)
		throws InvalidObjectException
	{
		throw new InvalidObjectException("Serialization proxy required.");
	}

	private void write(final DataOutput out) throws IOException {
		sql.write(out);
	}

	private static Query read(final DataInput in) throws IOException {
		return new Query(Sql.read(in), ParamValues.EMPTY, null, null);
	}

	private static final class Serial implements Externalizable {
		@java.io.Serial
		private static final long serialVersionUID = 1;

		/**
		 * The object being serialized.
		 */
		private Query object;

		/**
		 * Constructor for deserialization.
		 */
		public Serial() {
		}

		/**
		 * Creates an instance for serialization.
		 *
		 * @param object  the object
		 */
		Serial(final Query object) {
			this.object = object;
		}

		@Override
		public void writeExternal(final ObjectOutput out) throws IOException {
			object.write(out);
		}

		@Override
		public void readExternal(final ObjectInput in) throws IOException {
			object = Query.read(in);
		}

		@java.io.Serial
		private Object readResolve() {
			return object;
		}

	}

}
