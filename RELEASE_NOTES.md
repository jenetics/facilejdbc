## Release notes

### [2.1.0](https://github.com/jenetics/facilejdbc/releases/tag/v2.1.0)

#### Improvements

* [#23](https://github.com/jenetics/facilejdbc/issues/23): Implementation of `Stored` class.
```java
// Reading 'Link' objects from db.
final List<Stored<Long, Link>> links = select
	.as(LINK_PARSER.stored("id").list(), conn);

// Printing the result + its DB ids.
links.forEach(System.out::println);

// > Stored[id=1, value=Link[http://jenetics.io, text=null, type=null]]
// > Stored[id=2, value=Link[http://jenetics.io, text=Jenetics, type=web]]
// > Stored[id=3, value=Link[https://duckduckgo.com, text=DuckDuckGo, type=search]]
```
* [#49](https://github.com/jenetics/facilejdbc/issues/49): Implement `PreparedQuery` class.
```java
final var query = INSERT_LINK.prepareQuery(conn);
final var batch = Batch.of(links, LINK_DCTOR);
query.execute(batch);
```

#### Bug

* [#23](https://github.com/jenetics/facilejdbc/issues/23): Remove wrong `null` check in `Param` factory method.

### [2.0.0](https://github.com/jenetics/facilejdbc/releases/tag/v2.0.0)

#### Improvements

* [#21](https://github.com/jenetics/facilejdbc/issues/21): Create `Ctor` instances from Record classes. It is now possible to create `Ctor` directly from `record` classes.
```java
// Simple `Dctor` creation.
final Dctor<Book> dctor = Dctor.of(Book.class);

// Adapt the name conversion.
final Dctor<Book> dctor = Records.dctor(
    Book.class,
    component -> switch (component.getName()) {
        case "author" -> "primary_author";
        case "isbn" -> "isbn13";
        default -> Records.toSnakeCase(component);
    }
);

// Add additional columns.
final Dctor<Book> dctor = Records.dctor(
    Book.class,
    field("title_hash", book -> book.title().hashCode())
);
```
* [#43](https://github.com/jenetics/facilejdbc/issues/43): Create `RowParser` instances from `record` classes.
```java
// Simple `RowParser` creation.
final RowParser<Book> parser = RowParser.of(Book.class);

// Adapting the record component parsing.
final RowParser<Book> parser = Records.parserWithFields(
    Book.class,
    Map.of(
        "isbn", string("isbn").map(Isbn::new),
        "authors", int64("id").map(Author::selectByBookId)
    )
);
```


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