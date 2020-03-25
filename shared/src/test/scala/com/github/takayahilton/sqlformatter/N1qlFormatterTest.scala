package com.github.takayahilton.sqlformatter

class N1qlFormatterTest extends BehavesLikeSqlFormatterTest(SqlDialect.CouchbaseN1QL) {
  test("formats SELECT query with element selection expression") {
    val result = format("SELECT orderlines[0].productId FROM orders;")
    assert(
      result ==
        """|SELECT
           |  orderlines[0].productId
           |FROM
           |  orders;""".stripMargin
    )
  }

  test("formats SELECT query with primary key quering") {
    val result = format(
      "SELECT fname, email FROM tutorial USE KEYS ['dave', 'ian'];"
    )
    assert(
      result ==
        """|SELECT
           |  fname,
           |  email
           |FROM
           |  tutorial
           |USE KEYS
           |  ['dave', 'ian'];""".stripMargin
    )
  }

  test("formats INSERT with {} object literal") {
    val result = format(
      "INSERT INTO heroes (KEY, VALUE) VALUES ('123', {'id':1,'type':'Tarzan'});"
    )
    assert(
      result ==
        """|INSERT INTO
           |  heroes (KEY, VALUE)
           |VALUES
           |  ('123', {'id': 1, 'type': 'Tarzan'});""".stripMargin
    )
  }

  test("formats INSERT with large object and array literals") {
    val result = format(
      "INSERT INTO heroes (KEY, VALUE) VALUES ('123', {'id': 1, 'type': 'Tarzan', " +
        "'array': [123456789, 123456789, 123456789, 123456789, 123456789], 'hello': 'world'});"
    )
    assert(
      result ==
        """|INSERT INTO
           |  heroes (KEY, VALUE)
           |VALUES
           |  (
           |    '123',
           |    {
           |      'id': 1,
           |      'type': 'Tarzan',
           |      'array': [
           |        123456789,
           |        123456789,
           |        123456789,
           |        123456789,
           |        123456789
           |      ],
           |      'hello': 'world'
           |    }
           |  );""".stripMargin
    )
  }

  test("formats SELECT query with UNNEST toplevel reserver word") {
    val result = format(
      "SELECT * FROM tutorial UNNEST tutorial.children c;"
    )
    assert(
      result ==
        """|SELECT
           |  *
           |FROM
           |  tutorial
           |UNNEST
           |  tutorial.children c;""".stripMargin
    )
  }

  test("formats SELECT query with NEST and USE KEYS") {
    val result = format(
      "SELECT * FROM usr " +
        "USE KEYS 'Elinor_33313792' NEST orders_with_users orders " +
        "ON KEYS ARRAY s.order_id FOR s IN usr.shipped_order_history END;"
    )
    assert(
      result ==
        """|SELECT
           |  *
           |FROM
           |  usr
           |USE KEYS
           |  'Elinor_33313792'
           |NEST
           |  orders_with_users orders ON KEYS ARRAY s.order_id FOR s IN usr.shipped_order_history END;""".stripMargin
    )
  }

  test("formats explained DELETE query with USE KEYS and RETURNING") {
    val result = format(
      "EXPLAIN DELETE FROM tutorial t USE KEYS 'baldwin' RETURNING t"
    )
    assert(
      result ==
        """|EXPLAIN DELETE FROM
           |  tutorial t
           |USE KEYS
           |  'baldwin' RETURNING t""".stripMargin
    )
  }

  test("formats UPDATE query with USE KEYS and RETURNING") {
    val result = format(
      "UPDATE tutorial USE KEYS 'baldwin' SET type = 'actor' RETURNING tutorial.type"
    );
    assert(
      result ==
        """|UPDATE
           |  tutorial
           |USE KEYS
           |  'baldwin'
           |SET
           |  type = 'actor' RETURNING tutorial.type""".stripMargin
    )
  }

  test("recognizes $variables") {
    val result = format(
      "SELECT $variable, $'var name', $\"var name\", $`var name`;"
    )
    assert(
      result ==
        """|SELECT
           |  $variable,
           |  $'var name',
           |  $"var name",
           |  $`var name`;""".stripMargin
    )
  }

  test("replaces $variables with param values") {
    val result = format(
      "SELECT $variable, $'var name', $\"var name\", $`var name`;",
      Map(
        "variable" -> "\"variable value\"",
        "var name" -> "'var value'"
      )
    )
    assert(
      result ==
        """|SELECT
           |  "variable value",
           |  'var value',
           |  'var value',
           |  'var value';""".stripMargin
    )
  }

  test("replaces $ numbered placeholders with param values") {
    val result = format(
      "SELECT $1, $2, $0;",
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
}
