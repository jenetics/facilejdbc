## Release notes

### [1.3.0](https://github.com/jenetics/facilejdbc/releases/tag/v1.3.0)

* [#45](https://github.com/jenetics/facilejdbc/issues/45): Make `RowParser` composable.
* [#40](https://github.com/jenetics/facilejdbc/issues/40): Allow streaming of selection results.
```java
final var select = Query.of("SELECT * FROM person;");

// Make sure to close the returned stream.
try (var persons = select.as(PARSER.stream(), conn)) {
    // Do some processing with the person stream.
    persons.forEach(person -> ...);
}
```
* [#39](https://github.com/jenetics/facilejdbc/issues/39): Parser for exporting results as CSV file/string.
```java
final var select = Query.of("SELECT * FROM book;");
final var csv = select.as(ResultSetParser.csvLine(), conn);
System.out.println(csv);
```
* [#26](https://github.com/jenetics/facilejdbc/issues/26): Implement multi-value parameter
```java
final List<Book> results = Query.of("SELECT * FROM book WHERE id IN(:ids);")
    .on(Param.values("ids", 1, 2, 3, 4))
    .as(PARSER.list(), conn);
```

#### Improvements

### [1.2.0](https://github.com/jenetics/facilejdbc/releases/tag/v1.2.0)

#### Improvements

* [#24](https://github.com/jenetics/facilejdbc/issues/24): Support for SQL type transformation methods.
* [#27](https://github.com/jenetics/facilejdbc/issues/27): Allow to define the query timeout and fetch size.

### [1.1.0](https://github.com/jenetics/facilejdbc/releases/tag/v1.1.0)

#### Improvements

* [#15](https://github.com/jenetics/facilejdbc/issues/15): Make the `Query` class serializable.
* [#17](https://github.com/jenetics/facilejdbc/issues/17): Add lightwieght transaction functionality.
```java
final Transactional db = () -> DriverManager.getConnection(
    "jdbc:hsqldb:mem:testdb",
    "SA",
    ""
);
db.transaction().accept(conn -> {
    for (var query : queries) {
        query.execute(conn);
    }
});
final long id = db.transaction().apply(conn ->
    Book.insert(BOOKS.get(0), conn)
);
```
* [#19](https://github.com/jenetics/facilejdbc/issues/19): The original SQL string is reconstructible from the query object; `Query.rawSql()`.