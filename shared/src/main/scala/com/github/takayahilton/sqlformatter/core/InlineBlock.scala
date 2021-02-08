package com.github.takayahilton.sqlformatter.core

import scala.annotation.tailrec

/**
  * Bookkeeper for inline blocks.
  * <p>
  * Inline blocks are parenthized expressions that are shorter than INLINE_MAX_LENGTH.
  * These blocks are formatted on a single line, unlike longer parenthized
  * expressions where open-parenthesis causes newline and increase of indentation.
  */
private[core] class InlineBlock {
  import InlineBlock._

  private[this] var level: Int = 0

  /**
    * Begins inline block when lookahead through upcoming tokens determines
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

  /**
    * Finishes current inline block.
    * There might be several nested ones.
    */
  def end(): Unit = {
    level -= 1
  }

  /**
    * True when inside an inline block
    *
    * @return {Boolean}
    */
  def isActive: Boolean = level > 0
}

private[core] object InlineBlock {
  private val INLINE_MAX_LENGTH: Int = 50

  // Check if this should be an inline parentheses block
  // Examples are "NOW()", "COUNT(*)", "int(10)", key(`somecolumn`), DECIMAL(7,2)
  private def isInlineBlock(tokens: Vector[Token], index: Int): Boolean = {
    @tailrec
    def go(tokens: List[Token], length: Int, level: Int): Boolean = tokens match {
      // Overran max length
      case _ if length > InlineBlock.INLINE_MAX_LENGTH => false
      case Nil                                         => false
      case token :: tail if token.tokenType == TokenTypes.OPEN_PAREN =>
        go(tail, length + token.value.length, level + 1)
      case token :: tail if token.tokenType == TokenTypes.CLOSE_PAREN =>
        level < 2 || go(tail, length + token.value.length, level - 1)
      case token :: _ if isForbiddenToken(token) => false
      case token :: tail                         => go(tail, length + token.value.length, level)
    }
    go(tokens.drop(index).toList, 0, 0)
  }

  // Reserved words that cause newlines, comments and semicolons
  // are not allowed inside inline parentheses block
  private def isForbiddenToken(token: Token) = {
    Set(TokenTypes.RESERVED_TOPLEVEL, TokenTypes.RESERVED_NEWLINE, TokenTypes.BLOCK_COMMENT)
      .contains(token.tokenType) ||
    token.value == ";"
  }
}
