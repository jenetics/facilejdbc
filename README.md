**_UNDER CONSTRUCTION_**

# Facile JDBC

SQL is still the best abstraction for querying relational databases. JDBC, as standard way for reading and writing relational data is also well known and for most use cases sufficient. A common complaint about JDBC might be it's verbosity, when it comes to read/write data to/from the _entity_ objects.  This small library tries to make the usage of SQL/JDBC less verbose.

The `facilejdbc` library is inspired by the Scala [Anorm](https://playframework.github.io/anorm/) library.

**`facilejdbc` is not**
* An OR-Mapper
* A type safe query language

## Executing SQL queries

The main entry point for using the library is the `Query` interface.

```java
final DataSource ds = ...;
final Query query = Query.of("SELECT 1");
final boolean result = DB.transaction(ds, query::execute);
```
