package com.github.takayahilton.sqlformatter.core

final case class DialectConfig(
    lineCommentTypes: List[String],
    reservedToplevelWords: List[String],
    reservedNewlineWords: List[String],
    reservedWords: List[String],
    specialWordChars: List[String],
    stringTypes: List[String],
    openParens: List[String],
    closeParens: List[String],
    indexedPlaceholderTypes: List[String],
    namedPlaceholderTypes: List[String]
)
