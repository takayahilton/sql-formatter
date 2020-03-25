package com.github.takayahilton.sqlformatter

trait SqlParamable[A] {
  def apply(a: A): String
}

object SqlParamable {
  implicit val stringSqlParamable: SqlParamable[String] = new SqlParamable[String] {
    def apply(a: String): String = a
  }

  implicit def numericSqlParamable[A: Numeric]: SqlParamable[A] = new SqlParamable[A] {
    def apply(a: A): String = a.toString
  }
}
