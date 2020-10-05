package com.github.takayahilton.sqlformatter.core

/**
  * @param cfg       cfg.indent cfg.params
  * @param tokenizer Tokenizer
  */
class Formatter(cfg: FormatConfig, tokenizer: Tokenizer) {
  private[this] val indentation = new Indentation(cfg.indent)
  private[this] val inlineBlock = new InlineBlock
  private[this] val params = cfg.params
  private[this] var previousReservedWord: Option[Token] = None
  private[this] var tokens = Vector.empty[Token]
  private[this] var index = 0

  /**
    * Formats whitespaces in a SQL string to make it easier to read.
    *
    * @param query The SQL query string
    * @return formatted query
    */
  def format(query: String): String = {
    tokens = tokenizer.tokenize(query)
    val formattedQuery = getFormattedQueryFromTokens
    formattedQuery.trim
  }

  private def getFormattedQueryFromTokens = {
    var formattedQuery = ""

    tokens.zipWithIndex.foreach { case (token, _index) =>
      index = _index

      token.tokenType match {
        case TokenTypes.WHITESPACE =>
        // ignore (we do our own whitespace formatting)
        case TokenTypes.LINE_COMMENT =>
          formattedQuery = formatLineComment(token, formattedQuery)
        case TokenTypes.BLOCK_COMMENT =>
          formattedQuery = formatBlockComment(token, formattedQuery)
        case TokenTypes.RESERVED_TOPLEVEL =>
          formattedQuery = formatToplevelReservedWord(token, formattedQuery)
          previousReservedWord = Some(token)
        case TokenTypes.RESERVED_NEWLINE =>
          formattedQuery = formatNewlineReservedWord(token, formattedQuery)
          previousReservedWord = Some(token)
        case TokenTypes.RESERVED =>
          formattedQuery = formatWithSpaces(token, formattedQuery)
          previousReservedWord = Some(token)
        case TokenTypes.OPEN_PAREN =>
          formattedQuery = formatOpeningParentheses(token, formattedQuery)
        case TokenTypes.CLOSE_PAREN =>
          formattedQuery = formatClosingParentheses(token, formattedQuery)
        case TokenTypes.PLACEHOLDER =>
          formattedQuery = formatPlaceholder(token, formattedQuery)
        case _ if token.value == "," =>
          formattedQuery = formatComma(token, formattedQuery)
        case _ if token.value == ":" =>
          formattedQuery = formatWithSpaceAfter(token, formattedQuery)
        case _ if token.value == "." =>
          formattedQuery = formatWithoutSpaces(token, formattedQuery)
        case _ if token.value == ";" =>
          formattedQuery = formatQuerySeparator(token, formattedQuery)
        case _ =>
          formattedQuery = formatWithSpaces(token, formattedQuery)
      }
    }
    formattedQuery
  }

  private def formatLineComment(token: Token, query: String) =
    addNewline(query + token.value)

  private def formatBlockComment(token: Token, query: String) =
    addNewline(addNewline(query) + indentComment(token.value))

  private def indentComment(comment: String) =
    comment.replaceAll("\n", "\n" + indentation.getIndent)

  private def formatToplevelReservedWord(token: Token, query: String) = {
    indentation.decreaseTopLevel()
    val addedNewlineQuery = addNewline(query)
    indentation.increaseToplevel()
    val _query = addedNewlineQuery + equalizeWhitespace(token.value)
    addNewline(_query)
  }

  private def formatNewlineReservedWord(token: Token, query: String) =
    addNewline(query) + equalizeWhitespace(token.value) + " "

  // Replace any sequence of whitespace characters with single space
  private def equalizeWhitespace(string: String) =
    string.replaceAll("\\s+", " ")

  // Opening parentheses increase the block indent level and start a new line
  private def formatOpeningParentheses(token: Token, query: String) = {
    var _query = query
    // Take out the preceding space unless there was whitespace there in the original query
    // or another opening parens or line comment
    val preserveWhitespaceFor =
      Set(TokenTypes.WHITESPACE, TokenTypes.OPEN_PAREN, TokenTypes.LINE_COMMENT)
    if (!previousToken.exists(t => preserveWhitespaceFor.contains(t.tokenType))) {
      _query = util.trimEnd(_query)
    }
    _query += token.value
    inlineBlock.beginIfPossible(tokens, index)
    if (!inlineBlock.isActive) {
      indentation.increaseBlockLevel()
      _query = addNewline(_query)
    }
    _query
  }

  // Closing parentheses decrease the block indent level
  private def formatClosingParentheses(token: Token, query: String) =
    if (inlineBlock.isActive) {
      inlineBlock.end()
      formatWithSpaceAfter(token, query)
    } else {
      indentation.decreaseBlockLevel()
      formatWithSpaces(token, addNewline(query))
    }

  private def formatPlaceholder(token: Token, query: String) =
    query + params.get(token) + " "

  // Commas start a new line (unless within inline parentheses or SQL "LIMIT" clause)
  private def formatComma(token: Token, query: String) = {
    val trimmed = trimTrailingWhitespace(query) + token.value + " "
    if (inlineBlock.isActive) trimmed
    else if (previousReservedWord.exists(_.value.matches("(?i)^LIMIT$"))) trimmed
    else addNewline(trimmed)
  }

  private def formatWithSpaceAfter(token: Token, query: String) =
    trimTrailingWhitespace(query) + token.value + " "

  private def formatWithoutSpaces(token: Token, query: String) =
    trimTrailingWhitespace(query) + token.value

  private def formatWithSpaces(token: Token, query: String) =
    query + token.value + " "

  private def formatQuerySeparator(token: Token, query: String) =
    trimTrailingWhitespace(query) + token.value + "\n"

  private def addNewline(query: String) =
    util.trimEnd(query) + "\n" + indentation.getIndent

  private def trimTrailingWhitespace(query: String) = {
    val token = previousNonWhitespaceToken
    if (token.exists(_.tokenType == TokenTypes.LINE_COMMENT))
      util.trimEnd(query) + "\n"
    else
      util.trimEnd(query)
  }

  private def previousNonWhitespaceToken =
    tokens.take(index).reverseIterator.find(_.tokenType != TokenTypes.WHITESPACE)

  private def previousToken: Option[Token] =
    if (index - 1 < 0)
      None
    else
      Some(tokens(index - 1))
}
