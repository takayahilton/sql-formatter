package com.github.takayahilton.sqlformatter

import org.scalatest.funsuite.AnyFunSuite

abstract class BehavesLikeSqlFormatterTest(language: SqlDialect) extends AnyFunSuite {
  test("uses given indent config for indention") {
    val result = SqlFormatter
      .of(language)
      .format(
        "SELECT count(*),Column1 FROM Table1;",
        indent = "    "
      )

    assert(
      result ==
        """|SELECT
           |    count(*),
           |    Column1
           |FROM
           |    Table1;""".stripMargin
    );
  }

  protected def format(query: String) = SqlFormatter.of(language).format(query)
  protected def format(query: String, params: Map[String, _]): String =
    SqlFormatter.of(language).format(query, params)
  protected def format(query: String, params: List[_]): String =
    SqlFormatter.of(language).format(query, params)

  test("formats simple SET SCHEMA queries") {
    val result = format("SET SCHEMA tetrisdb; SET CURRENT SCHEMA bingodb;");
    assert(
      result ==
        """|SET SCHEMA
           |  tetrisdb;
           |SET CURRENT SCHEMA
           |  bingodb;""".stripMargin
    );
  }

  test("formats simple SELECT query") {
    val result = format("SELECT count(*),Column1 FROM Table1;");
    assert(
      result ==
        """|SELECT
           |  count(*),
           |  Column1
           |FROM
           |  Table1;""".stripMargin
    );
  }

  test("formats complex SELECT") {
    val result = format(
      "SELECT DISTINCT name, ROUND(age/7) field1, 18 + 20 AS field2, 'some string' FROM foo;"
    );
    assert(
      result ==
        """|SELECT
           |  DISTINCT name,
           |  ROUND(age / 7) field1,
           |  18 + 20 AS field2,
           |  'some string'
           |FROM
           |  foo;""".stripMargin
    );
  }

  test("formats SELECT with complex WHERE") {
    val result = SqlFormatter.format(
      "SELECT * FROM foo WHERE Column1 = 'testing'" +
        "AND ( (Column2 = Column3 OR Column4 >= NOW()) );"
    );
    assert(
      result ==
        """|SELECT
           |  *
           |FROM
           |  foo
           |WHERE
           |  Column1 = 'testing'
           |  AND (
           |    (
           |      Column2 = Column3
           |      OR Column4 >= NOW()
           |    )
           |  );""".stripMargin
    );
  }

  test("formats SELECT with toplevel reserved words") {
    val result = format(
      "SELECT * FROM foo WHERE name = 'John' GROUP BY some_column " +
        "HAVING column > 10 ORDER BY other_column LIMIT 5;"
    );
    assert(
      result ==
        """|SELECT
           |  *
           |FROM
           |  foo
           |WHERE
           |  name = 'John'
           |GROUP BY
           |  some_column
           |HAVING
           |  column > 10
           |ORDER BY
           |  other_column
           |LIMIT
           |  5;""".stripMargin
    );
  }

  test("formats LIMIT with two comma-separated values on single line") {
    val result = format(
      "LIMIT 5, 10;"
    );
    assert(
      result ==
        """|LIMIT
           |  5, 10;""".stripMargin
    );
  }

  test("formats LIMIT of single value followed by another SELECT using commas") {
    val result = format(
      "LIMIT 5; SELECT foo, bar;"
    );
    assert(
      result ==
        """|LIMIT
           |  5;
           |SELECT
           |  foo,
           |  bar;""".stripMargin
    );
  }

  test("formats LIMIT of single value and OFFSET") {
    val result = format(
      "LIMIT 5 OFFSET 8;"
    );
    assert(
      result ==
        """|LIMIT
           |  5 OFFSET 8;""".stripMargin
    );
  }

  test("recognizes LIMIT in lowercase") {
    val result = format(
      "limit 5, 10;"
    );
    assert(
      result ==
        """|limit
           |  5, 10;""".stripMargin
    );
  }

  test("preserves case of keywords") {
    val result = format(
      "select distinct * frOM foo left join bar WHERe a > 1 and b = 3"
    );
    assert(
      result ==
        """|select
           |  distinct *
           |frOM
           |  foo
           |  left join bar
           |WHERe
           |  a > 1
           |  and b = 3""".stripMargin
    );
  }

  test("formats SELECT query with SELECT query inside it") {
    val result = format(
      "SELECT *, SUM(*) AS sum FROM (SELECT * FROM Posts LIMIT 30) WHERE a > b"
    );
    assert(
      result ==
        """|SELECT
           |  *,
           |  SUM(*) AS sum
           |FROM
           |  (
           |    SELECT
           |      *
           |    FROM
           |      Posts
           |    LIMIT
           |      30
           |  )
           |WHERE
           |  a > b""".stripMargin
    );
  }

  test("formats SELECT query with INNER JOIN") {
    val result = format(
      "SELECT customer_id.from, COUNT(order_id) AS total FROM customers " +
        "INNER JOIN orders ON customers.customer_id = orders.customer_id;"
    );
    assert(
      result ==
        """|SELECT
           |  customer_id.from,
           |  COUNT(order_id) AS total
           |FROM
           |  customers
           |  INNER JOIN orders ON customers.customer_id = orders.customer_id;""".stripMargin
    );
  }

  test("formats SELECT query with different comments") {
    val result = format(
      "SELECT\n" +
        "/*\n" +
        " * This is a block comment\n" +
        " */\n" +
        "* FROM\n" +
        "-- This is another comment\n" +
        "MyTable # One final comment\n" +
        "WHERE 1 = 2;"
    );
    assert(
      result ==
        """|SELECT
           |  /*
           |   * This is a block comment
           |   */
           |  *
           |FROM
           |  -- This is another comment
           |  MyTable # One final comment
           |WHERE
           |  1 = 2;""".stripMargin
    );
  }

  test("formats simple INSERT query") {
    val result = format(
      "INSERT INTO Customers (ID, MoneyBalance, Address, City) VALUES (12,-123.4, 'Skagen 2111','Stv');"
    );
    assert(
      result ==
        """|INSERT INTO
           |  Customers (ID, MoneyBalance, Address, City)
           |VALUES
           |  (12, -123.4, 'Skagen 2111', 'Stv');""".stripMargin
    );
  }

  test("keeps short parenthized list with nested parenthesis on single line") {
    val result = format(
      "SELECT (a + b * (c - NOW()));"
    );
    assert(
      result ==
        """|SELECT
           |  (a + b * (c - NOW()));""".stripMargin
    );
  }

  test("breaks long parenthized lists to multiple lines") {
    val result = format(
      "INSERT INTO some_table (id_product, id_shop, id_currency, id_country, id_registration) (" +
        "SELECT IF(dq.id_discounter_shopping = 2, dq.value, dq.value / 100)," +
        "IF (dq.id_discounter_shopping = 2, 'amount', 'percentage') FROM foo);"
    )
    assert(
      result ==
        """|INSERT INTO
           |  some_table (
           |    id_product,
           |    id_shop,
           |    id_currency,
           |    id_country,
           |    id_registration
           |  ) (
           |    SELECT
           |      IF(
           |        dq.id_discounter_shopping = 2,
           |        dq.value,
           |        dq.value / 100
           |      ),
           |      IF (
           |        dq.id_discounter_shopping = 2,
           |        'amount',
           |        'percentage'
           |      )
           |    FROM
           |      foo
           |  );""".stripMargin
    );
  }

  test("formats simple UPDATE query") {
    val result = format(
      "UPDATE Customers SET ContactName='Alfred Schmidt', City='Hamburg' WHERE CustomerName='Alfreds Futterkiste';"
    );
    assert(
      result ==
        """|UPDATE
           |  Customers
           |SET
           |  ContactName = 'Alfred Schmidt',
           |  City = 'Hamburg'
           |WHERE
           |  CustomerName = 'Alfreds Futterkiste';""".stripMargin
    )
  }

  test("formats simple DELETE query") {
    val result = format(
      "DELETE FROM Customers WHERE CustomerName='Alfred' AND Phone=5002132;"
    );
    assert(
      result ==
        """|DELETE FROM
           |  Customers
           |WHERE
           |  CustomerName = 'Alfred'
           |  AND Phone = 5002132;""".stripMargin
    )
  }

  test("formats simple DROP query") {
    val result = format(
      "DROP TABLE IF EXISTS admin_role;"
    );
    assert(
      result ==
        "DROP TABLE IF EXISTS admin_role;"
    )
  }

  test("formats uncomplete query") {
    val result = format("SELECT count(");
    assert(
      result ==
        """|SELECT
           |  count(""".stripMargin
    );
  }

  test("formats query that ends with open comment") {
    val result = format("SELECT count(*)\n/*Comment");
    assert(
      result ==
        """|SELECT
           |  count(*)
           |  /*Comment""".stripMargin
    );
  }

  test("formats UPDATE query with AS part") {
    val result = format(
      "UPDATE customers SET totalorders = ordersummary.total  FROM ( SELECT * FROM bank) AS ordersummary"
    );
    assert(
      result ==
        """|UPDATE
           |  customers
           |SET
           |  totalorders = ordersummary.total
           |FROM
           |  (
           |    SELECT
           |      *
           |    FROM
           |      bank
           |  ) AS ordersummary""".stripMargin
    );
  }

  test("formats top-level and newline multi-word reserved words with inconsistent spacing") {
    val result = format("SELECT * FROM foo LEFT \t OUTER  \n JOIN bar ORDER \n BY blah");
    assert(
      result ==
        """|SELECT
           |  *
           |FROM
           |  foo
           |  LEFT OUTER JOIN bar
           |ORDER BY
           |  blah""".stripMargin
    );
  }

  test("formats long double parenthized queries to multiple lines") {
    val result = format("((foo = '0123456789-0123456789-0123456789-0123456789'))");
    assert(
      result ==
        """|(
           |  (
           |    foo = '0123456789-0123456789-0123456789-0123456789'
           |  )
           |)""".stripMargin
    );
  }

  test("formats short double parenthized queries to one line") {
    val result = format("((foo = 'bar'))");
    assert(result == "((foo = 'bar'))");
  }

  test("formats single-char operators") {
    assert(format("foo = bar") == "foo = bar");
    assert(format("foo < bar") == "foo < bar");
    assert(format("foo > bar") == "foo > bar");
    assert(format("foo + bar") == "foo + bar");
    assert(format("foo - bar") == "foo - bar");
    assert(format("foo * bar") == "foo * bar");
    assert(format("foo / bar") == "foo / bar");
    assert(format("foo % bar") == "foo % bar");
  }

  test("formats multi-char operators") {
    assert(format("foo != bar") == "foo != bar");
    assert(format("foo <> bar") == "foo <> bar");
    assert(format("foo == bar") == "foo == bar"); // N1QL
    assert(format("foo || bar") == "foo || bar"); // Oracle, Postgres, N1QL string concat

    assert(format("foo <= bar") == "foo <= bar");
    assert(format("foo >= bar") == "foo >= bar");

    assert(format("foo !< bar") == "foo !< bar");
    assert(format("foo !> bar") == "foo !> bar");
  }

  test("formats logical operators") {
    assert(format("foo ALL bar") == "foo ALL bar");
    assert(format("foo = ANY (1, 2, 3)") == "foo = ANY (1, 2, 3)");
    assert(format("EXISTS bar") == "EXISTS bar");
    assert(format("foo IN (1, 2, 3)") == "foo IN (1, 2, 3)");
    assert(format("foo LIKE 'hello%'") == "foo LIKE 'hello%'");
    assert(format("foo IS NULL") == "foo IS NULL");
    assert(format("UNIQUE foo") == "UNIQUE foo");
  }

  test("formats AND/OR operators") {
    assert(format("foo BETWEEN bar AND baz") == "foo BETWEEN bar\nAND baz");
    assert(format("foo AND bar") == "foo\nAND bar");
    assert(format("foo OR bar") == "foo\nOR bar");
  }

  test("recognizes strings") {
    assert(format("\"foo JOIN bar\"") == "\"foo JOIN bar\"");
    assert(format("'foo JOIN bar'") == "'foo JOIN bar'");
    assert(format("`foo JOIN bar`") == "`foo JOIN bar`");
  }

  test("recognizes escaped strings") {
    assert(format("\"foo \\\" JOIN bar\"") == "\"foo \\\" JOIN bar\"");
    assert(format("'foo \\' JOIN bar'") == "'foo \\' JOIN bar'");
    assert(format("`foo `` JOIN bar`") == "`foo `` JOIN bar`");
  }

  test("formats postgres specific operators") {
    assert(format("column::int") == "column :: int");
    assert(format("v->2") == "v -> 2");
    assert(format("v->>2") == "v ->> 2");
    assert(format("foo ~~ 'hello'") == "foo ~~ 'hello'");
    assert(format("foo !~ 'hello'") == "foo !~ 'hello'");
    assert(format("foo ~* 'hello'") == "foo ~* 'hello'");
    assert(format("foo ~~* 'hello'") == "foo ~~* 'hello'");
    assert(format("foo !~~ 'hello'") == "foo !~~ 'hello'");
    assert(format("foo !~* 'hello'") == "foo !~* 'hello'");
    assert(format("foo !~~* 'hello'") == "foo !~~* 'hello'");
  }

  test("keeps separation between multiple statements") {
    assert(format("foo;bar;") == "foo;\nbar;");
    assert(format("foo\n;bar;") == "foo;\nbar;");
    assert(format("foo\n\n\n;bar;\n\n") == "foo;\nbar;");

    val result = format("SELECT count(*),Column1 FROM Table1;\nSELECT count(*),Column1 FROM Table2;");
    assert(
      result ==
        """|SELECT
           |  count(*),
           |  Column1
           |FROM
           |  Table1;
           |SELECT
           |  count(*),
           |  Column1
           |FROM
           |  Table2;""".stripMargin
    );
  }
}
