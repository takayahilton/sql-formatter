package com.github.takayahilton.sqlformatter.core

import java.util.regex.Pattern

package object util {
  def trimEnd(s: String): String = s.replaceAll("[ |\\n|\\r]*$", "")

  def escapeRegExp(s: String): String = {
    val regexp = List(
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

    Pattern.compile(regexp).matcher(s).replaceAll("\\\\$0")
  }
}
