package com.github.takayahilton.sqlformatter.core

import FormatConfig.DEFAULT_INDENT

final case class FormatConfig(indent: String = DEFAULT_INDENT, params: Params)

object FormatConfig {
  def apply(indent: String): FormatConfig =
    new FormatConfig(indent, Params.EmptyParams)

  val DEFAULT_INDENT = "  "
}
