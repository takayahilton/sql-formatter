package com.github.takayahilton.sqlformatter.languages

import com.github.takayahilton.sqlformatter.core.{DialectConfig, FormatConfig, Formatter, Params, Tokenizer}
import FormatConfig.DEFAULT_INDENT

abstract class AbstractFormatter {
  def dialectConfig: DialectConfig

  /**
    * Formats DB2 query to make it easier to read
    *
    * @param query query string
    * @param cfg   FormatConfig
    * @return formatted string
    */
  def format(query: String, cfg: FormatConfig): String = {
    val tokenizer = new Tokenizer(dialectConfig)
    new Formatter(cfg, tokenizer).format(query)
  }

  def format(query: String, indent: String, params: Seq[_]): String =
    format(
      query,
      FormatConfig(indent = indent, params = Params.IndexedParams(params))
    )

  def format(query: String, params: Seq[_]): String =
    format(query, DEFAULT_INDENT, params)

  def format(query: String, indent: String, params: Map[String, _]): String =
    format(
      query,
      FormatConfig(indent = indent, params = Params.NamedParams(params))
    )

  def format(query: String, params: Map[String, _]): String =
    format(query, DEFAULT_INDENT, params)

  def format(query: String, indent: String): String =
    format(query, FormatConfig(indent = indent, params = Params.EmptyParams))

  def format(query: String): String = format(query, DEFAULT_INDENT)
}
