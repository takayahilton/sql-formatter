package com.github.takayahilton.sqlformatter

class Db2FormatterTest extends BehavesLikeSqlFormatterTest(SqlDialect.DB2) {

  test("formats FETCH FIRST like LIMIT") {
    val formatted =
      """|SELECT
         |  col1
         |FROM
         |  tbl
         |ORDER BY
         |  col2 DESC
         |FETCH FIRST
         |  20 ROWS ONLY;""".stripMargin
    assert(
      format(
        "SELECT col1 FROM tbl ORDER BY col2 DESC FETCH FIRST 20 ROWS ONLY;"
      ) == formatted
    )
  }

  test("formats only -- as a line comment") {
    val result = format(
      """|SELECT col FROM
         |-- This is a comment
         |MyTable;""".stripMargin
    )
    assert(
      result ==
        """|SELECT
         |  col
         |FROM
         |  -- This is a comment
         |  MyTable;""".stripMargin
    )
  }

  test("recognizes @ and # as part of identifiers") {
    val result = format(
      "SELECT col#1, @col2 FROM tbl"
    )
    assert(
      result ==
        """|SELECT
           |  col#1,
           |  @col2
           |FROM
           |  tbl""".stripMargin
    )
  }

  test("recognizes :variables") {
    assert(
      format("SELECT :variable;") ==
        """|SELECT
           |  :variable;""".stripMargin
    )
  }

  test("replaces :variables with param values") {
    val result = format(
      "SELECT :variable",
      Map("variable" -> "\"variable value\"")
    )
    assert(
      result ==
        """|SELECT
           |  "variable value"""".stripMargin
    )
  }
}
