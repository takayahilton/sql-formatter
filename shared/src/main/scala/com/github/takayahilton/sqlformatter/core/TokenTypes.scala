package com.github.takayahilton.sqlformatter.core

private[core] sealed trait TokenTypes extends Product with Serializable

private[core] object TokenTypes {
  case object WHITESPACE extends TokenTypes
  case object WORD extends TokenTypes
  case object STRING extends TokenTypes
  case object RESERVED extends TokenTypes
  case object RESERVED_TOPLEVEL extends TokenTypes
  case object RESERVED_NEWLINE extends TokenTypes
  case object OPERATOR extends TokenTypes
  case object OPEN_PAREN extends TokenTypes
  case object CLOSE_PAREN extends TokenTypes
  case object LINE_COMMENT extends TokenTypes
  case object BLOCK_COMMENT extends TokenTypes
  case object NUMBER extends TokenTypes
  case object PLACEHOLDER extends TokenTypes
}
