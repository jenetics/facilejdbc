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


## Executing SQL queries

The main entry point for using the library is the `Query` interface.

```java
final DataSource ds = ...;
final Query query = Query.of("SELECT 1");
final boolean result = DB.transaction(ds, query::execute);
```

The `execute` method returns a `boolean` value, as specified in the [`PreparedStatement.execute()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/PreparedStatement.html#execute()) method.

### Select objects

Usually you will have a data object where a row of a table is stored.

```java
public static final class Person { 
    private final String _name;
    private final String _email;
    private final String _link;
    public Person(final String name, final String email, final String link) { 
    	_name = name; _email = email; _link = link; 
    }
    public String name() { return _name; }
    public String email() { return _email; }
    public String link() { return _link; }
}
```

For reading records from the DB you can create a select query, which looks like the following code snippet.

```java
static final Query SELECT_PERSON = Query.of(
    "SELECT name, email, link " +
    "FROM person " +
    "WHERE name = :name"
);
```

Executing person records by name.

```java
final List<Person> persons = transaction(ds, conn ->
    SELECT_PERSON
        .on(value("name", "Franz"))
        .as(PARSER.list(), conn)
);
```

The following code shows the needed `Person` parser.

```java
static final RowParser<Person> PARSER = row -> new Person(
    row.getString("name"),
    row.getString("email"),
    row.getString("link")
);
```

The row parser is responsable for creating the DTOs from the query results.

### Insert single objects

First you have to define the insert query object.

```java
static final Query INSERT_PERSON = Query.of(
    "INSERT INTO person(name, email, link) " +
    "VALUES(:name, :email, :link);"
);
```

The query object is then used for inserting a person.

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

It is also possible to do a batch insert.

```java
final List<Person> persons = ...;
final Batch<Person> batch = Batch.of(persons, DCTOR);
final int count = transaction(ds, conn ->
    INSERT_PERSON.execute(batch, conn)
);
```

Analog to the record parser, you need a _deconstructor_ for splitting the DTO in its components.

```java
private static final Dctor<Person> DCTOR = Dctor.of(
    field("name", Person::name),
    field("email", Person::email),
    field("link", Person::link)
);
```
