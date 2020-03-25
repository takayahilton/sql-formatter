package com.github.takayahilton.sqlformatter.core

import java.util.regex.Pattern

import scala.collection.immutable.VectorBuilder

/**
  * @param cfg {String[]} cfg.reservedWords Reserved words in SQL
  *            {String[]} cfg.reservedToplevelWords Words that are set to new line separately
  *            {String[]} cfg.reservedNewlineWords Words that are set to newline
  *            {String[]} cfg.stringTypes String types to enable: "", '', ``, [], N''
  *            {String[]} cfg.openParens Opening parentheses to enable, like (, [
  *            {String[]} cfg.closeParens Closing parentheses to enable, like ), ]
  *            {String[]} cfg.indexedPlaceholderTypes Prefixes for indexed placeholders, like ?
  *            {String[]} cfg.namedPlaceholderTypes Prefixes for named placeholders, like @ and :
  *            {String[]} cfg.lineCommentTypes Line comments to enable, like # and --
  *            {String[]} cfg.specialWordChars Special chars that can be found inside of words, like @ and #
  */
class Tokenizer(val cfg: DialectConfig) {
  private val WHITESPACE_REGEX = "^(\\s+)"
  private val NUMBER_REGEX =
    "^((-\\s*)?[0-9]+(\\.[0-9]+)?|0x[0-9a-fA-F]+|0b[01]+)\\b"
  private val OPERATOR_REGEX =
    "^(!=|<>|==|<=|>=|!<|!>|\\|\\||::|->>|->|~~\\*|~~|!~~\\*|!~~|~\\*|!~\\*|!~|.)"
  private val LINE_COMMENT_REGEX = createLineCommentRegex(cfg.lineCommentTypes)
  private val RESERVED_TOPLEVEL_REGEX = createReservedWordRegex(
    cfg.reservedToplevelWords
  )
  private val RESERVED_NEWLINE_REGEX = createReservedWordRegex(
    cfg.reservedNewlineWords
  )
  private val RESERVED_PLAIN_REGEX = createReservedWordRegex(cfg.reservedWords)
  private val WORD_REGEX = createWordRegex(cfg.specialWordChars)
  private val STRING_REGEX = createStringRegex(cfg.stringTypes)
  private val OPEN_PAREN_REGEX = createParenRegex(cfg.openParens)
  private val CLOSE_PAREN_REGEX = createParenRegex(cfg.closeParens)
  private val INDEXED_PLACEHOLDER_REGEX =
    Tokenizer.createPlaceholderRegex(cfg.indexedPlaceholderTypes, "[0-9]*")
  private val IDENT_NAMED_PLACEHOLDER_REGEX =
    Tokenizer.createPlaceholderRegex(
      cfg.namedPlaceholderTypes,
      "[a-zA-Z0-9._$]+"
    )
  private val STRING_NAMED_PLACEHOLDER_REGEX =
    Tokenizer.createPlaceholderRegex(
      cfg.namedPlaceholderTypes,
      createStringPattern(cfg.stringTypes)
    )

  private def createLineCommentRegex(lineCommentTypes: List[String]) =
    String.format(
      "^((?:%s).*?(?:\n|$))",
      lineCommentTypes.map(util.escapeRegExp).mkString("|")
    )

  private def createReservedWordRegex(reservedWords: List[String]) = {
    val reservedWordsPattern =
      reservedWords.mkString("|").replaceAll(" ", "\\\\s+")
    "(?i)" + "^(" + reservedWordsPattern + ")\\b"
  }

  private def createWordRegex(specialChars: List[String]) =
    "^([\\w" + specialChars.mkString + "]+)"

  private def createStringRegex(stringTypes: List[String]) =
    "^(" + createStringPattern(stringTypes) + ")"

  // This enables the following string patterns:
  // 1. backtick quoted string using `` to escape
  // 2. square bracket quoted string (SQL Server) using ]] to escape
  // 3. double quoted string using "" or \" to escape
  // 4. single quoted string using '' or \' to escape
  // 5. national character quoted string using N'' or N\' to escape
  private def createStringPattern(stringTypes: List[String]) = {
    val patterns = Map(
      ("``", "((`[^`]*($|`))+)"),
      ("[]", "((\\[[^\\]]*($|\\]))(\\][^\\]]*($|\\]))*)"),
      ("\"\"", "((\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*(\"|$))+)"),
      ("''", "(('[^'\\\\]*(?:\\\\.[^'\\\\]*)*('|$))+)"),
      ("N''", "((N'[^N'\\\\]*(?:\\\\.[^N'\\\\]*)*('|$))+)")
    )
    stringTypes.flatMap(patterns.get).mkString("|")
  }

  private def createParenRegex(parens: List[String]) =
    "(?i)" + "^(" + parens.map(Tokenizer.escapeParen).mkString("|") + ")"

  /**
    * Takes a SQL string and breaks it into tokens.
    * Each token is an object with type and value.
    *
    * @param input input The SQL string
    * @return {Object[]} tokens An array of tokens.
    */
  def tokenize(input: String): Vector[Token] = {

    @scala.annotation.tailrec
    def go(input: String, previousToken: Option[Token], tokens: VectorBuilder[Token]): Vector[Token] = input match {
      case "" =>
        tokens.result()
      case _ =>
        val token = getNextToken(input, previousToken).get // if token is None, something is wrong
        go(input.substring(token.value.length), Some(token), tokens += token)
    }

    go(input, None, new VectorBuilder)
  }

  private def getNextToken(input: String, previousToken: Option[Token]) =
    getWhitespaceToken(input) orElse
      getCommentToken(input) orElse
      getStringToken(input) orElse
      getOpenParenToken(input) orElse
      getCloseParenToken(input) orElse
      getPlaceholderToken(input) orElse
      getNumberToken(input) orElse
      getReservedWordToken(input, previousToken) orElse
      getWordToken(input) orElse
      getOperatorToken(input)

  private def getWhitespaceToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.WHITESPACE, WHITESPACE_REGEX)

  private def getCommentToken(input: String) =
    getLineCommentToken(input) orElse getBlockCommentToken(input)

  private def getLineCommentToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.LINE_COMMENT, LINE_COMMENT_REGEX)

  private def getBlockCommentToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.BLOCK_COMMENT, BLOCK_COMMENT_REGEX)

  private def getStringToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.STRING, STRING_REGEX)

  private def getOpenParenToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.OPEN_PAREN, OPEN_PAREN_REGEX)

  private def getCloseParenToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.CLOSE_PAREN, CLOSE_PAREN_REGEX)

  private def getPlaceholderToken(input: String) =
    getIdentNamedPlaceholderToken(input) orElse
      getStringNamedPlaceholderToken(input) orElse
      getIndexedPlaceholderToken(input)

  private def getIdentNamedPlaceholderToken(input: String) =
    IDENT_NAMED_PLACEHOLDER_REGEX.flatMap(
      getPlaceholderTokenWithKey(input, _, _.substring(1))
    )

  private def getStringNamedPlaceholderToken(input: String) =
    STRING_NAMED_PLACEHOLDER_REGEX.flatMap(
      getPlaceholderTokenWithKey(
        input,
        _,
        v =>
          getEscapedPlaceholderKey(
            v.substring(2, v.length - 1),
            v.substring(v.length - 1)
          )
      )
    )

  private def getIndexedPlaceholderToken(input: String) =
    INDEXED_PLACEHOLDER_REGEX.flatMap(
      getPlaceholderTokenWithKey(input, _, _.substring(1))
    )

  private def getPlaceholderTokenWithKey(
      input: String,
      regex: String,
      parseKey: String => String
  ) = {
    val token = getTokenOnFirstMatch(input, TokenTypes.PLACEHOLDER, regex)
    token.map(t => t.withKey(parseKey(t.value)))
  }

  private def getEscapedPlaceholderKey(key: String, quoteChar: String) =
    key.replaceAll(util.escapeRegExp("\\") + quoteChar, quoteChar)

  // Decimal, binary, or hex numbers
  private def getNumberToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.NUMBER, NUMBER_REGEX)

  // Punctuation and symbols
  private def getOperatorToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.OPERATOR, OPERATOR_REGEX)

  private def getReservedWordToken(
      input: String,
      previousToken: Option[Token]
  ): Option[Token] = {
    // A reserved word cannot be preceded by a "."
    // this makes it so in "mytable.from", "from" is not considered a reserved word
    if (previousToken.exists(_.value == "."))
      None
    else
      getToplevelReservedToken(input) orElse getNewlineReservedToken(input) orElse getPlainReservedToken(
        input
      )
  }

  private def getToplevelReservedToken(input: String) =
    getTokenOnFirstMatch(
      input,
      TokenTypes.RESERVED_TOPLEVEL,
      RESERVED_TOPLEVEL_REGEX
    )

  private def getNewlineReservedToken(input: String) =
    getTokenOnFirstMatch(
      input,
      TokenTypes.RESERVED_NEWLINE,
      RESERVED_NEWLINE_REGEX
    )

  private def getPlainReservedToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.RESERVED, RESERVED_PLAIN_REGEX)

  private def getWordToken(input: String) =
    getTokenOnFirstMatch(input, TokenTypes.WORD, WORD_REGEX)

  private def getFirstMatch(input: String, regex: String): Option[String] = {
    val matcher = Pattern.compile(regex).matcher(input)
    if (matcher.find)
      Some(matcher.group)
    else
      None
  }

  private def getTokenOnFirstMatch(input: String, tokenType: TokenTypes, regex: String): Option[Token] = {
    val matches = getFirstMatch(input, regex)
    matches.map(Token(tokenType, _))
  }
}

object Tokenizer {
  private def escapeParen(paren: String) =
    if (paren.length == 1) { // A single punctuation character
      util.escapeRegExp(paren)
    } else { // longer word
      "\\b" + paren + "\\b"
    }

  private def createPlaceholderRegex(
      types: List[String],
      pattern: String
  ): Option[String] = {
    if (types.isEmpty)
      None
    else {
      val typesRegex = types.map(util.escapeRegExp).mkString("|")
      Some(String.format("^((?:%s)(?:%s))", typesRegex, pattern))
    }
  }
}
