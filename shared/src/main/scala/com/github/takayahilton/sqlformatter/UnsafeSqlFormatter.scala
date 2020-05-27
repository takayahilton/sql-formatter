package com.github.takayahilton.sqlformatter

import com.github.takayahilton.sqlformatter.core.FormatConfig
import com.github.takayahilton.sqlformatter.languages._

object UnsafeSqlFormatter {

  /**
    * FormatConfig whitespaces in a query to make it easier to read.
    *
    * @param query sql
    * @param cfg   cfg.indent Characters used for indentation, default is "  " (2 spaces)
    *              cfg.params Collection of params for placeholder replacement
    * @return {String}
    */
  def format(query: String, cfg: FormatConfig): String =
    standard.format(query, cfg)

  def format[A](query: String, indent: String, params: Seq[A]): String =
    standard.formatUnsafe(query, indent, params)

  def format[A](query: String, params: Seq[A]): String =
    standard.formatUnsafe(query, params)

  def format[A](query: String, indent: String, params: Map[String, A]): String =
    standard.formatUnsafe(query, indent, params)

  def format[A](query: String, params: Map[String, A]): String =
    standard.formatUnsafe(query, params)

  def format(query: String, indent: String): String =
    standard.format(query, indent)

  def format(query: String): String = standard.format(query)

  def standard: AbstractFormatter = SqlFormatter.of(SqlDialect.StandardSQL)

  def of(name: String): AbstractFormatter = {
    name match {
      case "db2" =>
        new Db2Formatter
      case "n1ql" =>
        new N1qlFormatter
      case "pl/sql" =>
        new PlSqlFormatter
      case "sql" =>
        new StandardSqlFormatter
      case _ =>
        throw new Exception(s"Unsupported SQL dialect: $name")
    }
  }
}
