package com.github.takayahilton.sqlformatter.core

final case class Token(
    tokenType: TokenTypes,
    value: String,
    key: Option[String]
) {

  def withKey(key: String): Token = copy(key = Some(key))

  override def toString: String =
    s"(type: $tokenType, value [$value]" + ")"
}

object Token {
  def apply(tokenType: TokenTypes, value: String): Token =
    new Token(tokenType, value, None)
}
