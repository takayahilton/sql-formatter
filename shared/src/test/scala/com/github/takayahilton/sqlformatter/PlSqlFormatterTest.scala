package com.github.takayahilton.sqlformatter

class PlSqlFormatterTest extends BehavesLikeSqlFormatterTest(SqlDialect.PLSQL) {
  test("formats FETCH FIRST like LIMIT") {
    assert(
      format(
        "SELECT col1 FROM tbl ORDER BY col2 DESC FETCH FIRST 20 ROWS ONLY;"
      ) ==
        """|SELECT
           |  col1
           |FROM
           |  tbl
           |ORDER BY
           |  col2 DESC
           |FETCH FIRST
           |  20 ROWS ONLY;""".stripMargin
    )
  }

  test("formats only -- as a line comment") {
    val result = format(
      "SELECT col FROM\n" +
        "-- This is a comment\n" +
        "MyTable;\n"
    );
    assert(
      result ==
        """|SELECT
           |  col
           |FROM
           |  -- This is a comment
           |  MyTable;""".stripMargin
    )
  }

  test("recognizes _, $, #, . and @ as part of identifiers") {
    val result = format(
      "SELECT my_col$1#, col.2@ FROM tbl\n"
    );
    assert(
      result ==
        """|SELECT
           |  my_col$1#,
           |  col.2@
           |FROM
           |  tbl""".stripMargin
    )
  }

  test("formats short CREATE TABLE") {
    assert(
      format(
        "CREATE TABLE items (a INT PRIMARY KEY, b TEXT);"
      ) ==
        "CREATE TABLE items (a INT PRIMARY KEY, b TEXT);"
    )
  }

  test("formats long CREATE TABLE") {
    assert(
      format(
        "CREATE TABLE items (a INT PRIMARY KEY, b TEXT, c INT NOT NULL, d INT NOT NULL);"
      ) ==
        """|CREATE TABLE items (
           |  a INT PRIMARY KEY,
           |  b TEXT,
           |  c INT NOT NULL,
           |  d INT NOT NULL
           |);""".stripMargin
    )
  }

  test("formats INSERT without INTO") {
    val result = format(
      "INSERT Customers (ID, MoneyBalance, Address, City) VALUES (12,-123.4, 'Skagen 2111','Stv');"
    );
    assert(
      result ==
        """|INSERT
           |  Customers (ID, MoneyBalance, Address, City)
           |VALUES
           |  (12, -123.4, 'Skagen 2111', 'Stv');""".stripMargin
    )
  }

  test("formats ALTER TABLE ... MODIFY query") {
    val result = format(
      "ALTER TABLE supplier MODIFY supplier_name char(100) NOT NULL;"
    );
    assert(
      result ==
        """|ALTER TABLE
           |  supplier
           |MODIFY
           |  supplier_name char(100) NOT NULL;""".stripMargin
    )
  }

  test("formats ALTER TABLE ... ALTER COLUMN query") {
    val result = format(
      "ALTER TABLE supplier ALTER COLUMN supplier_name VARCHAR(100) NOT NULL;"
    );
    assert(
      result ==
        """|ALTER TABLE
           |  supplier
           |ALTER COLUMN
           |  supplier_name VARCHAR(100) NOT NULL;""".stripMargin
    )
  }

  test("recognizes :variables") {
    val result = format(
      "SELECT :variable, :a1_2.3$, :'var name', :\"var name\", :`var name`;"
    )
    assert(
      result ==
        """|SELECT
           |  :variable,
           |  :a1_2.3$,
           |  :'var name',
           |  :"var name",
           |  :`var name`;""".stripMargin
    )
  }

  test("replaces :variables with param values") {
    val result = SqlFormatter.format(
      "SELECT :variable, :a1_2.3$, :'var name', :\"var name\", :`var name`," +
        " :[var name], :'escaped \\'var\\'', :\"^*& weird \\\" var   \";",
      Map(
        "variable" -> "\"variable value\"",
        "a1_2.3$" -> "'weird value'",
        "var name" -> "'var value'",
        "escaped 'var'" -> "'weirder value'",
        "^*& weird \" var   " -> "'super weird value'"
      )
    )
    assert(
      result ==
        """|SELECT
           |  "variable value",
           |  'weird value',
           |  'var value',
           |  'var value',
           |  'var value',
           |  'var value',
           |  'weirder value',
           |  'super weird value';""".stripMargin
    );
  }

  test("recognizes ?[0-9]* placeholders") {
    val result = format("SELECT ?1, ?25, ?;");
    assert(
      result ==
        """|SELECT
           |  ?1,
           |  ?25,
           |  ?;""".stripMargin
    );
  }

  test("replaces ? numbered placeholders with param values") {
    val result = format(
      "SELECT ?1, ?2, ?0;",
      Map(
        "0" -> "first",
        "1" -> "second",
        "2" -> "third"
      )
    )
    assert(
      result ==
        """|SELECT
           |  second,
           |  third,
           |  first;""".stripMargin
    )
  }

  test("replaces ? indexed placeholders with param values") {
    val result = format("SELECT ?, ?, ?;", List("first", "second", "third"))

    assert(
      result ==
        """|SELECT
           |  first,
           |  second,
           |  third;""".stripMargin
    );
  }

  test("formats SELECT query with CROSS JOIN") {
    val result = format("SELECT a, b FROM t CROSS JOIN t2 on t.id = t2.id_t");
    assert(
      result ==
        """|SELECT
           |  a,
           |  b
           |FROM
           |  t
           |  CROSS JOIN t2 on t.id = t2.id_t""".stripMargin
    );
  }

  test("formats SELECT query with CROSS APPLY") {
    val result = format("SELECT a, b FROM t CROSS APPLY fn(t.id)")
    assert(
      result ==
        """|SELECT
           |  a,
           |  b
           |FROM
           |  t
           |  CROSS APPLY fn(t.id)""".stripMargin
    );
  }

  test("formats simple SELECT") {
    val result = format("SELECT N, M FROM t");
    assert(
      result ==
        """|SELECT
           |  N,
           |  M
           |FROM
           |  t""".stripMargin
    );
  }

  test("formats simple SELECT with national characters") {
    val result = format("SELECT N'value'");
    assert(
      result ==
        """|SELECT
           |  N'value'""".stripMargin
    );
  }

  test("formats SELECT query with OUTER APPLY") {
    val result = format("SELECT a, b FROM t OUTER APPLY fn(t.id)");
    assert(
      result ==
        """|SELECT
           |  a,
           |  b
           |FROM
           |  t
           |  OUTER APPLY fn(t.id)""".stripMargin
    );
  }

  test("formats CASE ... WHEN with a blank expression") {
    val result = format(
      "CASE WHEN option = 'foo' THEN 1 WHEN option = 'bar' THEN 2 WHEN option = 'baz' THEN 3 ELSE 4 END;"
    );

    assert(
      result ==
        """|CASE
           |  WHEN option = 'foo' THEN 1
           |  WHEN option = 'bar' THEN 2
           |  WHEN option = 'baz' THEN 3
           |  ELSE 4
           |END;""".stripMargin
    );
  }

  test("formats CASE ... WHEN inside SELECT") {
    val result = format(
      "SELECT foo, bar, CASE baz WHEN 'one' THEN 1 WHEN 'two' THEN 2 ELSE 3 END FROM table"
    );

    assert(
      result ==
        """|SELECT
           |  foo,
           |  bar,
           |  CASE
           |    baz
           |    WHEN 'one' THEN 1
           |    WHEN 'two' THEN 2
           |    ELSE 3
           |  END
           |FROM
           |  table""".stripMargin
    );
  }

  test("formats CASE ... WHEN with an expression") {
    val result = format(
      "CASE toString(getNumber()) WHEN 'one' THEN 1 WHEN 'two' THEN 2 WHEN 'three' THEN 3 ELSE 4 END;"
    );

    assert(
      result ==
        """|CASE
           |  toString(getNumber())
           |  WHEN 'one' THEN 1
           |  WHEN 'two' THEN 2
           |  WHEN 'three' THEN 3
           |  ELSE 4
           |END;""".stripMargin
    )
  }
}
