package com.github.takayahilton.sqlformatter.core

sealed trait Params extends Product with Serializable {

  /**
    * Returns param value that matches given placeholder with param key.
    *
    * @param token token.key Placeholder key
    *              token.value Placeholder value
    * @return param or token.value when params are missing
    */
  def get(token: Token): String
}

object Params {
  case class NamedParams(params: Map[String, String]) extends Params {
    def get(token: Token): String =
      token.key.flatMap(params.get).getOrElse(token.value)
  }
  case class IndexedParams(_params: Seq[String]) extends Params {
    private[this] var params = _params
    def get(token: Token): String = params match {
      case Nil => token.value
      case head +: tail =>
        params = tail
        head
    }
  }

  case object EmptyParams extends Params {
    def get(token: Token): String = token.value
  }
}
