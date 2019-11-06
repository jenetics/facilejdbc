**_UNDER DEVELOPMENT_**

# Facile JDBC

> _Making the JDBC usage simpler and less verbose._

## Overview

SQL is still the best abstraction for querying relational databases. JDBC, as standard way for reading and writing relational data is also well known and for many use cases sufficient. A common pain point about JDBC is it's verbosity, when it comes to read/write data to/from the _entity_ objects ([DTO](https://en.wikipedia.org/wiki/Data_transfer_object)). 
 
 The purpose of the `facilejdbc` library is to make the usage of SQL/JDBC less verbose. Not more or less. `facilejdbc` is heavily inspired by the Scala [Anorm](https://playframework.github.io/anorm/) library, which finds a good balance between simplicity and expressiveness.

> #### `facilejdbc` is not
>
> * OR-Mapper: `facilejdbc` is not an Object Relational Mapper, like [Hiberenate](https://hibernate.org/) or [JPA](https://docs.oracle.com/javaee/7/tutorial/partpersist.htm).
> * Query language: [SQL]() is still used as query language. It is not tried to make it type safe or _abstract_ it away, like [jOOQ](https://www.jooq.org/).


## Concepts

The main entry point, when using `facilejdbc`, is the `Query` class. This is the place where you define your SQL string.

```java
final Query query = Query.of("SELECT 1");
```

Since the `Query` class doesn't store any mutable state, it is possible to define commonly used queries and use it in different threads. _The `Query` class is thread safe._ 


## Examples

### Executing queries

SQL queries are executed via the `Query` class. All what it needs is a JDBC `Connection`.

```java
final DataSource ds = ...;
final Query query = Query.of("SELECT 1");
final boolean result = Db.transaction(ds, conn -> 
    query.execute(conn)
);
```

The `execute` method returns a `boolean` value, as specified in the [`PreparedStatement.execute()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/PreparedStatement.html#execute()) method.

### Select objects

Usually you will have a DTO where a table row is stored.

```java
public final class Person { 
    private final String _name;
    private final String _email;
    private final String _link;
    public Person(String name, String email, String link) { 
    	_name = name; _email = email; _link = link; 
    }
    public String name() { return _name; }
    public String email() { return _email; }
    public String link() { return _link; }
}
```

If you want to select all persons with a given name, you can use the following query.

```java
static final Query SELECT_PERSON = Query.of(
    "SELECT name, email, link " +
    "FROM person " +
    "WHERE name = :name"
);
```

This query is then executed as follows.

```java
final List<Person> persons = transaction(ds, conn ->
    SELECT_PERSON
        .on(value("name", "Franz"))
        .as(PEERSON_PARSER.list(), conn)
);
```

For converting the result into the `Person` DTO, you have to create a proper `RowParser`. The row parser is responsible for creating the DTOs from the query results.

```java
static final RowParser<Person> PERSON_PARSER = row -> new Person(
    row.getString("name"),
    row.getString("email"),
    row.getString("link")
);
```

### Insert single objects

For inserting one new `Person` into the DB, you have to define an insertion query. 

```java
static final Query INSERT_PERSON = Query.of(
    "INSERT INTO person(name, email, link) " +
    "VALUES(:name, :email, :link);"
);
```

Then you have to set all query parameters and execute the query.

```java
final boolean inserted = transaction(ds, conn ->
    INSERT_PERSON
        .on(
            value("name", "foo"),
            value("email", "foo@gmail.com"),
            value("link", "http://google.com"))
        .execute(conn)
);
```

### Batch insertion

If you have a collection of `Person`s, you can insert it in one batch.

```java
final List<Person> persons = ...;
final Batch<Person> batch = Batch.of(persons, DCTOR);
final int count = transaction(ds, conn ->
    INSERT_PERSON
        .execute(batch, conn)
);
```

Analog to the record parser, you need a _deconstructor_ for splitting the DTO in its components/parameters.

```java
private static final Dctor<Person> DCTOR = Dctor.of(
    field("name", Person::name),
    field("email", Person::email),
    field("link", Person::link)
);
```
