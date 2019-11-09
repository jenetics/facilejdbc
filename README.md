[![Build Status](https://travis-ci.org/jenetics/facilejdbc.svg?branch=master)](https://travis-ci.org/jenetics/facilejdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.jenetics/facilejdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.jenetics/facilejdbc)
[![Javadoc](https://www.javadoc.io/badge/io.jenetics/facilejdbc.svg)](http://www.javadoc.io/doc/io.jenetics/facilejdbc)

**_For building and running the library, Java 11 (or above) is required._**

# Facile JDBC

> _Making the JDBC usage simpler and less verbose._

## Overview

SQL is still the best abstraction for querying relational databases. JDBC, as standard way for reading and writing relational data is also well known and for many use cases sufficient. A common pain point about JDBC is it's verbosity, when it comes to read/write data to/from the _entity_ objects ([DTO](https://en.wikipedia.org/wiki/Data_transfer_object)). 
 
 The purpose of the `facilejdbc` library is to make the usage of SQL/JDBC less verbose. Not more or less. `facilejdbc` is heavily inspired by the Scala [Anorm](https://playframework.github.io/anorm/) library, which finds a good balance between simplicity and expressiveness.

> #### `facilejdbc` is not
>
> * OR-Mapper: `facilejdbc` is not an Object Relational Mapper, like [Hiberenate](https://hibernate.org/) or [JPA](https://docs.oracle.com/javaee/7/tutorial/partpersist.htm).
> * No typed query language. [SQL]() is still used as query language. It is not tried to make it type safe or _abstract_ it away, like [jOOQ](https://www.jooq.org/).
> * Query class generator. It's just the library no generated classes or meta-programming (reflection).


> #### `facilejdbc` has no
>
> * DB-vendor specific code. Uses 100% pure JDBC.
> * Dynamic query generation. The user is responsible for creating the SQL string dynamically, if needed.
> * Generated classes or dynamically generated proxies.
> * Transaction handling. The users are responsible when to create, commit or rollback their connections. 
> * No connection pooling.

After all this, you may wonder, what is the `facilejdbc` library doing for me?

> #### `facilejdbc` gives you
>
> * The possibility to fill query parameters by _name_ instead of its position: Available via the `Param` interface.
> * Functions for creating (parsing) _entity_ objects from query `ResultSet`s: Available via the `RowParser` interface
> * Functions for splitting (deconstructing) _entity_ objects to DB columns: Available via the `Dctor` interface.
> * One `Query` class for putting all things together.


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
final Query query = Query.of("SELECT 1");
final boolean result = query.execute(conn)
```

The `execute` method returns a `boolean` value, as specified in the [`PreparedStatement.execute()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/PreparedStatement.html#execute()) method.

### Select objects

Usually you will have a DTO where a table row is stored.

```java
@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@Accessors(fluent = true)
public final class Person { 
    private final String name;
    private final String email;
    private final String link;
}
```

If you want to select all persons with a given name, you can use the following query.

```java
static final Query SELECT = Query.of(
    "SELECT name, email, link " +
    "FROM person " +
    "WHERE name = :name"
);
```

This query is then executed as follows.

```java
final List<Person> persons = SELECT
    .on(value("name", "Franz"))
    .as(PARSER.list(), conn);
```

For converting the result into the `Person` DTO, you have to create a proper `RowParser`. The row parser is responsible for creating the DTOs from the query results.

```java
static final RowParser<Person> PARSER = (row, conn) -> Person.builder()
    .name(row.getString("name"))
    .email(row.getString("email"))
    .link(row.getString("link"))
    .build();
```

### Insert single objects

For inserting one new `Person` into the DB, you have to define an insertion query. 

```java
static final Query INSERT = Query.of(
    "INSERT INTO person(name, email, link) " +
    "VALUES(:name, :email, :link);"
);
```

Then you have to set all query parameters and execute the query.

```java
final boolean inserted = INSERT
    .on(
        value("name", "foo"),
        value("email", "foo@gmail.com"),
        value("link", "http://google.com"))
    .execute(conn);
```

### Batch insertion

If you have a collection of `Person`s, you can insert it in one batch.

```java
final List<Person> persons = ...;
final Batch batch = Batch.of(persons, DCTOR);
final int[] counts = INSERT.executeUpdate(batch, conn);
```

Analog to the record parser, you need a _deconstructor_ for splitting the DTO in its components/parameters.

```java
private static final Dctor<Person> DCTOR = Dctor.of(
    field("name", Person::name),
    field("email", Person::email),
    field("link", Person::link)
);
```

## License

The library is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

    Copyright 2019 Franz Wilhelmstötter

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and


## Release notes

* Initial release(s).
