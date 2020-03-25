package com.github.takayahilton.sqlformatter.core

case class Token(
    tokenType: TokenTypes,
    value: String,
    regex: Option[String],
    key: Option[String]
) {

  def withKey(key: String): Token = copy(key = Some(key))

  override def toString: String =
    s"(type: $tokenType, value [$value]" + regex.fold(")")(r => s"regex: /$r/)")
}

object Token {
  def apply(tokenType: TokenTypes, value: String, regex: String): Token =
    new Token(tokenType, value, Some(regex), None)

  def apply(tokenType: TokenTypes, value: String): Token =
    new Token(tokenType, value, None, None)
}
