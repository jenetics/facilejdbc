[![Build Status](https://travis-ci.org/jenetics/facilejdbc.svg?branch=master)](https://travis-ci.org/jenetics/facilejdbc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.jenetics/facilejdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.jenetics/facilejdbc)
[![Javadoc](https://www.javadoc.io/badge/io.jenetics/facilejdbc.svg)](http://www.javadoc.io/doc/io.jenetics/facilejdbc)

**_For building and running the library, Java 11 (or above) is required._**

# Facile JDBC

_Making the JDBC usage simpler and less verbose._

## Overview

JDBC is the basic API for accessing relational databases. Being basic makes it quite tedious to use directly. This lead to higher level abstractions like [JPA](https://docs.oracle.com/javaee/7/tutorial/partpersist.htm). Using a full grown _Object Relational Mapper_ on the other side might be to heavy weight for many uses cases. _FacileJDBC_ tries to fill the gap by making the low level JDBC access less verbose and tedious. SQL is still used as query language.

#### _FacileJDBC_ gives you

> * A lightweight wrapper around the JDBC API.
> * The possibility to fill query parameters by _name_ instead of its position: Available via the `Param` interface.
> * Functions for creating (parsing) _entity_ objects from query `ResultSet`s: Available via the `RowParser` interface
> * Functions for splitting (deconstructing) _entity_ objects to DB columns: Available via the `Dctor` interface.
> * A `Query` object to putting all things together.

#### _FacileJDBC_ is not

> * An OR-Mapper, like [JPA](https://docs.oracle.com/javaee/7/tutorial/partpersist.htm) or [Hibernate](https://hibernate.org/).
> * No type safe query language like [jOOQ](https://www.jooq.org/).


#### _FacileJDBC_ has no

> * DB-vendor specific code, uses 100% pure JDBC.
> * Query generation capabilities. The user is responsible for creating the proper SQL string.
> * Generated classes or dynamically generated proxies.
> * Transaction handling. The users are responsible when to create, commit or rollback their connections. 
> * No connection pooling.

## Examples

The following example show how to use _FacileJDBC_ for different use cases. For a detailed description of the API, also have a look at the [Javadoc](https://www.javadoc.io/doc/io.jenetics/facilejdbc). 

### Executing queries

SQL queries are defined via the `Query` class. Since the `Query` class is immutable, it is safe to use it in a multi-threaded environment or define it as _static_ class member. For executing query, all what it needs is a JDBC `Connection`.

```java
final Query query = Query.of("SELECT 1");
final boolean result = query.execute(conn)
```

The `execute` method returns a `boolean` value as specified in the [`PreparedStatement.execute()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/PreparedStatement.html#execute()) method. _The [Javadoc](https://www.javadoc.io/doc/io.jenetics/facilejdbc) documents the used methods of the `PreparedStatement` for every _execute_ method.

### Selecting objects

Usually, your selected rows will be stored in [DTO](https://en.wikipedia.org/wiki/Data_transfer_object) objects. For the _simple_ examples the following `Person` DTO will be used.

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

The following query will select all persons which matches a given name _pattern_.

```java
static final Query SELECT = Query.of(
    "SELECT name, email, link " +
    "FROM person " +
    "WHERE name like :name"
);
```

Executing the select query is shown in the following code snippet. It also shows how query parameter are set and how the result rows are parsed. 

```java
final List<Person> persons = SELECT
    .on(value("name", "M%"))
    .as(PARSER.list(), conn);
```

The `Query.on` method is used for filling the query parameters. The variables uses the usual syntax for SQL bind variables. With the `Param.value` factory method it is possible to fill the defined variables. The `Query.as` method executes the query and returns the parsed result rows. For converting the query result to a _DTO_, a `RowParser` is needed. With the `RowParser.list()` method you will tell the query that you expect 0 to _n_ result rows. If only one result is expected, you need to use the `RowParser.single()` or `RowParser.singleOp()` method.

The following code snippet shows the `RowParser` implementation for our `Person` DTO.

```java
static final RowParser<Person> PARSER = (row, conn) -> Person.builder()
    .name(row.getString("name"))
    .email(row.getString("email"))
    .link(row.getString("link"))
    .build();
```

Since the `RowParser` is a _functional_ interface it can be written as shown. The first function parameter represents the actual selected row and second parameter the JDBC `Connection` used for executing the query. In most cases the `conn` will not be used, but it is quite helpful for fetching dependent DTOs in a sub-query.


### Inserting objects

For inserting one new `Person` into the DB an _insert_ query have to be defined. 

```java
static final Query INSERT = Query.of(
    "INSERT INTO person(name, email, link) " +
    "VALUES(:name, :email, :link);"
);
```

When all bind variable has been set, it can be inserted by calling the `execute` method.

```java
final boolean inserted = INSERT
    .on(
        value("name", "foo"),
        value("email", "foo@gmail.com"),
        value("link", "http://google.com"))
    .execute(conn);
```

Setting the bind variables this way is quite tedious, if you already have a filled `Person` DTO. Inserting the `Person` DTO directly you need to define the variable-field mapping. This is done via the `Dctor` (deconstructor) interface. The `Dctor` can be seen as the inverse function of the `RowParser`.

```java
private static final Dctor<Person> DCTOR = Dctor.of(
    field("name", Person::name),
    field("email", Person::email),
    field("link", Person::link)
);
```

Once a deconstructor is defined for your DTO, you can easily insert single `Person` objects.

```java
final Person person = ...;
final boolean inserted = INSERT
    .on(person, DCTOR)
    .execute(conn);
```

If you are interested in the _automatically_ generated primary key of the insertion, you have to use the `executeInsert` method. This method returns an `Optional` in the case no primary key could be generated.

```java
final Optional<Long> inserted = INSERT
    .on(...)
    .executeInsert(conn);
```

or

```java
final Optional<Integer> inserted = INSERT
    .on(...)
    .executeInsert(RowParser.int32(1), conn);
```

if you are need to control the parsing of the generated primary key.

### Batch insertion

If you have a collection of `Person`s, you can insert it in one batch.

```java
final List<Person> persons = ...;
final Batch batch = Batch.of(persons, DCTOR);
final int[] counts = INSERT.executeUpdate(batch, conn);
```

### Selecting/inserting object _graphs_

The previous examples shows the basic usage of the library. It is possible to use this for all needed select and insert queries, as you will do it with plain JDBC. If you need to select or insert _small_ object graphs, this becomes fast tedious as well. 

Lets extend our initial example an convert the _link_ of the `Person` into an object

```java
@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@Accessors(fluent = true)
public final class Person { 
    private final String name;
    private final String email;
    private final Link link;
}
```
and with a `Link` class, which will look like the following.
```java
@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@Accessors(fluent = true)
public final class Link { 
    private final String name;
    private final URI link;
}
```

It is now possible to create one `RowParser<Person>` and one `Dctor<Person>` which automatically takes care about the linked `Link` object. The new parser will look like the following code snippet.

```java
static final RowParser<Person> PERSON_PARSER = (row, conn) -> new Person(
    row.getString("name"),
    row.getString("email"),
    selectLink(row.getLong("link_id"), conn)
);
```
With the shown deconstructor.
```java
static final Dctor<Person> PERSON_DCTOR = Dctor.of(
    field("name", Person::name),
    field("email", Person::email),
    field("link_id", (p, c) -> insertLink(p.link(), c))
);
```

The needed helper methods are responsible for selecting/inserting the `Link` object.

```java
static final RowParser<Link> LINK_PARSER = (row, conn) -> new Link(
    row.getString("name"),
    URI.create(row.getString("url"))
);

static final Dctor<Link> LINK_DCTOR = Dctor.of(
    field("name", Link::name),
    field("url", l -> l.url.toString())
);

static Link selectLink(final Long linkId, final Connection conn)
    throws SQLException
{
    return Query.of("SELECT * FROM link WHERE id = :id")
        .on(value("id", linkId))
        .as(LINK_PARSER.singleNull(), conn);
}

static Long insertLink(final Link link, final Connection conn)
    throws SQLException
{
    return Query.of("INSERT INTO link(name, url) VALUES(:name, :url")
        .on(link, LINK_DCTOR)
        .executeInsert(conn)
        .orElseThrow();
}
```

It is still necessary to implement the sub-inserts and sub-selects, but this can be re-used in other queries, where inserting and selecting of `Link`s is needed. Note that this is not an automatic OR-mapping mechanism. The user is still in charge for the concrete implementation.

> **Note**
>
> Although the described feature is quite expressive and may solve some selection/insertion task in an elegant way, does not mean you have to use it. Just treat it as additional possibility.

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

* Initial release.
