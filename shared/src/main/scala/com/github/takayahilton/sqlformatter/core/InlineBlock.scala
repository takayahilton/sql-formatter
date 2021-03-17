package com.github.takayahilton.sqlformatter.core

/** Bookkeeper for inline blocks.
  * <p>
  * Inline blocks are parenthized expressions that are shorter than INLINE_MAX_LENGTH.
  * These blocks are formatted on a single line, unlike longer parenthized
  * expressions where open-parenthesis causes newline and increase of indentation.
  */
private[core] class InlineBlock {
  private[this] var level: Int = 0

  /** Begins inline block when lookahead through upcoming tokens determines
    * that the block would be smaller than INLINE_MAX_LENGTH.
    *
    * @param tokens Array of all tokens
    * @param index  Current token position
    */
  def beginIfPossible(tokens: Vector[Token], index: Int): Unit = {
    if (level == 0 && isInlineBlock(tokens, index)) level = 1
    else if (level > 0) level += 1
    else level = 0
  }

  /** Finishes current inline block.
    * There might be several nested ones.
    */
  def end(): Unit = {
    level -= 1
  }

  /** True when inside an inline block
    *
    * @return {Boolean}
    */
  def isActive: Boolean = level > 0

  // Check if this should be an inline parentheses block
  // Examples are "NOW()", "COUNT(*)", "int(10)", key(`somecolumn`), DECIMAL(7,2)
  private def isInlineBlock(tokens: Vector[Token], index: Int): Boolean = {
    var length = 0
    var level = 0
    for (i <- index until tokens.size) {
      val token = tokens(i)
      length += token.value.length
      // Overran max length
      if (length > InlineBlock.INLINE_MAX_LENGTH) return false
      if (token.tokenType == TokenTypes.OPEN_PAREN) level += 1
      else if (token.tokenType == TokenTypes.CLOSE_PAREN) {
        level -= 1
        if (level == 0) return true
      }
      if (isForbiddenToken(token)) return false
    }
    false
  }

  // Reserved words that cause newlines, comments and semicolons
  // are not allowed inside inline parentheses block
  private def isForbiddenToken(token: Token) = {
    Set(TokenTypes.RESERVED_TOPLEVEL, TokenTypes.RESERVED_NEWLINE, TokenTypes.BLOCK_COMMENT)
      .contains(token.tokenType) ||
    token.value == ";"
  }
}

private[core] object InlineBlock {
  private val INLINE_MAX_LENGTH: Int = 50
}
