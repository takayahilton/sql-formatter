package com.github.takayahilton.sqlformatter

import com.github.takayahilton.sqlformatter.languages._
import com.github.takayahilton.sqlformatter.core.FormatConfig

object SqlFormatter {

  /** FormatConfig whitespaces in a query to make it easier to read.
    *
    * @param query sql
    * @param cfg   cfg.indent Characters used for indentation, default is "  " (2 spaces)
    *              cfg.params Collection of params for placeholder replacement
    * @return {String}
    */
  def format(query: String, cfg: FormatConfig): String =
    standard.format(query, cfg)

  def format[A: SqlParamable](query: String, indent: String, params: Seq[A]): String =
    standard.format(query, indent, params)

  def format[A: SqlParamable](query: String, params: Seq[A]): String =
    standard.format(query, params)

  def format[A: SqlParamable](query: String, indent: String, params: Map[String, A]): String =
    standard.format(query, indent, params)

  def format[A: SqlParamable](query: String, params: Map[String, A]): String =
    standard.format(query, params)

  def format(query: String, indent: String): String =
    standard.format(query, indent)

  def format(query: String): String = standard.format(query)

  def standard: AbstractFormatter = of(SqlDialect.StandardSQL)

  def of(sqlDialect: SqlDialect): AbstractFormatter =
    sqlDialect match {
      case SqlDialect.DB2 =>
        new Db2Formatter
      case SqlDialect.CouchbaseN1QL =>
        new N1qlFormatter
      case SqlDialect.PLSQL =>
        new PlSqlFormatter
      case SqlDialect.StandardSQL =>
        new StandardSqlFormatter
    }
}
