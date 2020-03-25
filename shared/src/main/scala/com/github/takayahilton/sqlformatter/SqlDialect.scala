package com.github.takayahilton.sqlformatter

sealed abstract class SqlDialect(val name: String) extends Product with Serializable
object SqlDialect {
  case object StandardSQL extends SqlDialect("sql")
  case object CouchbaseN1QL extends SqlDialect("n1ql")
  case object DB2 extends SqlDialect("db2")
  case object PLSQL extends SqlDialect("pl/sql")
}
