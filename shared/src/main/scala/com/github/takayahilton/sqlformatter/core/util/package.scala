package com.github.takayahilton.sqlformatter.core

import java.util.regex.Pattern

package object util {
  def trimEnd(s: String): String = s.replaceAll("[ |\\n|\\r]*$", "")

  private[this] val regexp = List(
    "^",
    "$",
    "\\",
    ".",
    "*",
    "+",
    "*",
    "?",
    "(",
    ")",
    "[",
    "]",
    "{",
    "}",
    "|"
  ).map(spChr => "(\\" + spChr + ")")
    .mkString("|")

  def escapeRegExp(s: String): String =
    Pattern.compile(regexp).matcher(s).replaceAll("\\\\$0")
}
