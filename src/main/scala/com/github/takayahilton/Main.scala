package com.github.takayahilton

import com.github.takayahilton.sqlformatter.SqlFormatter
import org.scalajs.dom._

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {

  private[this] val initialSql =
    """|SELECT supplier_name, city FROM suppliers
       |WHERE supplier_id > 500
       |ORDER BY supplier_name ASC, city DESC;
       |""".stripMargin

  def main(args: Array[String]): Unit = {
    document.getElementById("sql-input").asInstanceOf[html.Input].value = initialSql
    formatSqlInput()
  }

  @JSExportTopLevel("formatSqlInput")
  def formatSqlInput(): Unit = {
    val input = document.getElementById("sql-input")
    val formatted = SqlFormatter.format(input.asInstanceOf[html.Input].value)

    document.getElementById("sql-formatted").asInstanceOf[html.Input].value = formatted
  }
}
