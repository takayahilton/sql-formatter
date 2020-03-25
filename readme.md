# sql-formatter

[![Build Status](https://travis-ci.org/takayahilton/sql-formatter.png?branch=master)](https://travis-ci.org/takayahilton/sql-formatter)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.takayahilton/sql-formatter_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.takayahilton%22%20AND%20a:%22sql-formatter_2.12%22)

Scala port of great SQL formatter <https://github.com/zeroturnaround/sql-formatter>, <https://github.com/vertical-blank/sql-formatter>.

Written with only Scala Standard Library, without dependencies.


## Usage

### Scala (on JVM)

```sbt
libraryDependencies += "com.github.takayahilton" %% "sql-formatter" % "1.0.0"
```

### Scala.js

```sbt
libraryDependencies += "com.github.takayahilton" %%% "sql-formatter" % "1.0.0"
```

### Examples

You can easily use `com.github.takayahilton.sqlformatter.SqlFormatter` :

```scala
import com.github.takayahilton.sqlformatter._

SqlFormatter.format("SELECT * FROM table1")
```

This will output:

```sql
SELECT
  *
FROM
  table1
```

### Dialect

You can pass dialect name to SqlFormatter.of :

```scala
import com.github.takayahilton.sqlformatter._

SqlFormatter.of(SqlDialect.CouchbaseN1QL).format("SELECT *")
```

Currently just four SQL dialects are supported:

- StandardSQL - [Standard SQL](https://en.wikipedia.org/wiki/SQL:2011)
- CouchbaseN1QL - [Couchbase N1QL](http://www.couchbase.com/n1ql)
- DB2 - [IBM DB2](https://www.ibm.com/analytics/us/en/technology/db2/)
- PLSQL - [Oracle PL/SQL](http://www.oracle.com/technetwork/database/features/plsql/index.html)

### Format

Defaults to two spaces.
You can pass indent string to `format` :

```scala
import com.github.takayahilton.sqlformatter._

SqlFormatter.format(
  "SELECT * FROM table1",
  indent = "    ")
```

This will output:

```sql
SELECT
    *
FROM
    table1
```

### Placeholders replacement

You can pass `Seq` or `Map` to `format` :

```scala
import com.github.takayahilton.sqlformatter._

// Named placeholders
SqlFormatter.format("SELECT * FROM tbl WHERE foo = @foo", params = Map("foo" -> "'bar'"))

// Indexed placeholders
SqlFormatter.format("SELECT * FROM tbl WHERE foo = ?", params = Seq("'bar'"))
```

Both result in:

```sql
SELECT
  *
FROM
  tbl
WHERE
  foo = 'bar'
```
