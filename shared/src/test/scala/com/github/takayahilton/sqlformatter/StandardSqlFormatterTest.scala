package com.github.takayahilton.sqlformatter

class StandardSqlFormatterTest extends BehavesLikeSqlFormatterTest(SqlDialect.StandardSQL) {

  test("formats short CREATE TABLE") {
    assert(
      SqlFormatter.format("CREATE TABLE items (a INT PRIMARY KEY, b TEXT);") ==
        "CREATE TABLE items (a INT PRIMARY KEY, b TEXT);"
    )
  }

  test("formats long CREATE TABLE") {
    assert(
      SqlFormatter.format(
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
    val result = SqlFormatter.format(
      "INSERT Customers (ID, MoneyBalance, Address, City) VALUES (12,-123.4, 'Skagen 2111','Stv');"
    )
    assert(
      result ==
        """|INSERT
           |  Customers (ID, MoneyBalance, Address, City)
           |VALUES
           |  (12, -123.4, 'Skagen 2111', 'Stv');""".stripMargin
    )
  }

  test("formats ALTER TABLE ... MODIFY query") {
    val result = SqlFormatter.format(
      "ALTER TABLE supplier MODIFY supplier_name char(100) NOT NULL;"
    )
    assert(
      result ==
        """|ALTER TABLE
           |  supplier
           |MODIFY
           |  supplier_name char(100) NOT NULL;""".stripMargin
    )
  }

  test("formats ALTER TABLE ... ALTER COLUMN query") {
    val result = SqlFormatter.format(
      "ALTER TABLE supplier ALTER COLUMN supplier_name VARCHAR(100) NOT NULL;"
    )
    assert(
      result ==
        """|ALTER TABLE
           |  supplier
           |ALTER COLUMN
           |  supplier_name VARCHAR(100) NOT NULL;""".stripMargin
    )
  }

  test("recognizes [] strings") {
    assert(SqlFormatter.format("[foo JOIN bar]") == "[foo JOIN bar]")
    assert(SqlFormatter.format("[foo ]] JOIN bar]") == "[foo ]] JOIN bar]")
  }

  test("recognizes @variables") {
    val result = SqlFormatter.format(
      "SELECT @variable, @a1_2.3$, @'var name', @\"var name\", @`var name`, @[var name];"
    )
    assert(
      result ==
        """|SELECT
           |  @variable,
           |  @a1_2.3$,
           |  @'var name',
           |  @"var name",
           |  @`var name`,
           |  @[var name];""".stripMargin
    )
  }

  test("replaces @variables with param values") {
    val result = SqlFormatter.format(
      "SELECT @variable, @a1_2.3$, @'var name', @\"var name\", @`var name`, @[var name], @'var\\name';",
      Map(
        "variable" -> "\"variable value\"",
        "a1_2.3$" -> "'weird value'",
        "var name" -> "'var value'",
        "var\\name" -> "'var\\ value'"
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
           |  'var\ value';""".stripMargin
    )
  }

  test("recognizes :variables") {
    val result = SqlFormatter.format(
      "SELECT :variable, :a1_2.3$, :'var name', :\"var name\", :`var name`, :[var name];"
    )
    assert(
      result ==
        """|SELECT
           |  :variable,
           |  :a1_2.3$,
           |  :'var name',
           |  :"var name",
           |  :`var name`,
           |  :[var name];""".stripMargin
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
    val result = SqlFormatter.format("SELECT ?1, ?25, ?;");
    assert(
      result ==
        """|SELECT
           |  ?1,
           |  ?25,
           |  ?;""".stripMargin
    )
  }

  test("replaces ? numbered placeholders with param values") {
    val result = SqlFormatter.format(
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
    val result = SqlFormatter.format(
      "SELECT ?, ?, ?;",
      List("first", "second", "third")
    )
    assert(
      result ==
        """|SELECT
           |  first,
           |  second,
           |  third;""".stripMargin
    )
  }

  test("formats query with GO batch separator") {
    val result = SqlFormatter.format(
      "SELECT 1 GO SELECT 2",
      List("first", "second", "third")
    )
    assert(
      result ==
        """|SELECT
           |  1
           |GO
           |SELECT
           |  2""".stripMargin
    )
  }

  test("formats SELECT query with CROSS JOIN") {
    val result =
      SqlFormatter.format("SELECT a, b FROM t CROSS JOIN t2 on t.id = t2.id_t");
    assert(
      result ==
        """|SELECT
           |  a,
           |  b
           |FROM
           |  t
           |  CROSS JOIN t2 on t.id = t2.id_t""".stripMargin
    )
  }

  test("formats SELECT query with CROSS APPLY") {
    val result = SqlFormatter.format("SELECT a, b FROM t CROSS APPLY fn(t.id)");
    assert(
      result ==
        """|SELECT
           |  a,
           |  b
           |FROM
           |  t
           |  CROSS APPLY fn(t.id)""".stripMargin
    )
  }

  test("formats simple SELECT") {
    val result = SqlFormatter.format("SELECT N, M FROM t");
    assert(
      result ==
        """|SELECT
           |  N,
           |  M
           |FROM
           |  t""".stripMargin
    )
  }

  test("formats simple SELECT with national characters (MSSQL)") {
    val result = SqlFormatter.format("SELECT N'value'");
    assert(
      result ==
        """|SELECT
           |  N'value'""".stripMargin
    )
  }

  test("formats SELECT query with OUTER APPLY") {
    val result = SqlFormatter.format("SELECT a, b FROM t OUTER APPLY fn(t.id)");
    assert(
      result ==
        """|SELECT
           |  a,
           |  b
           |FROM
           |  t
           |  OUTER APPLY fn(t.id)""".stripMargin
    )
  }

  test("formats FETCH FIRST like LIMIT") {
    val result = SqlFormatter.format(
      "SELECT * FETCH FIRST 2 ROWS ONLY;"
    )
    assert(
      result ==
        """|SELECT
           |  *
           |FETCH FIRST
           |  2 ROWS ONLY;""".stripMargin
    )
  }

  test("formats CASE ... WHEN with a blank expression") {
    val result = SqlFormatter.format(
      "CASE WHEN option = 'foo' THEN 1 WHEN option = 'bar' THEN 2 WHEN option = 'baz' THEN 3 ELSE 4 END;"
    )

    assert(
      result ==
        """|CASE
           |  WHEN option = 'foo' THEN 1
           |  WHEN option = 'bar' THEN 2
           |  WHEN option = 'baz' THEN 3
           |  ELSE 4
           |END;""".stripMargin
    )
  }

  test("formats CASE ... WHEN inside SELECT") {
    val result = SqlFormatter.format(
      "SELECT foo, bar, CASE baz WHEN 'one' THEN 1 WHEN 'two' THEN 2 ELSE 3 END FROM table"
    )

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
    )
  }

  test("formats CASE ... WHEN with an expression") {
    val result = SqlFormatter.format(
      "CASE toString(getNumber()) WHEN 'one' THEN 1 WHEN 'two' THEN 2 WHEN 'three' THEN 3 ELSE 4 END;"
    )

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

  test("recognizes lowercase CASE ... END") {
    val result = SqlFormatter.format(
      "case when option = 'foo' then 1 else 2 end;"
    )

    assert(
      result ==
        """|case
           |  when option = 'foo' then 1
           |  else 2
           |end;""".stripMargin
    )
  }

  // Regression test for issue #43
  test("ignores words CASE and END inside other strings") {
    val result = SqlFormatter.format(
      "SELECT CASEDATE, ENDDATE FROM table1;"
    )

    assert(
      result ==
        """|SELECT
           |  CASEDATE,
           |  ENDDATE
           |FROM
           |  table1;""".stripMargin
    )
  }

  test("formats tricky line comments") {
    assert(
      SqlFormatter.format("SELECT a#comment, here\nFROM b--comment") ==
        """|SELECT
           |  a #comment, here
           |FROM
           |  b --comment""".stripMargin
    )
  }

  test("formats line comments followed by semicolon") {
    assert(
      SqlFormatter.format("SELECT a FROM b\n--comment\n;") ==
        """|SELECT
           |  a
           |FROM
           |  b --comment
           |;""".stripMargin
    )
  }

  test("formats line comments followed by comma") {
    assert(
      SqlFormatter.format("SELECT a --comment\n, b") ==
        """|SELECT
           |  a --comment
           |,
           |  b""".stripMargin
    )
  }

  test("formats line comments followed by close-paren") {
    assert(
      SqlFormatter.format("SELECT ( a --comment\n )") ==
        """|SELECT
           |  (a --comment
           |)""".stripMargin
    )
  }

  test("formats line comments followed by open-paren") {
    assert(
      SqlFormatter.format("SELECT a --comment\n()") ==
        """|SELECT
           |  a --comment
           |  ()""".stripMargin
    )
  }

  test("formats lonely semicolon") {
    assert(SqlFormatter.format(";") == ";")
  }
}
