package com.textMailer.IO

import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => EqOp}
import com.datastax.driver.core.querybuilder.Clause

trait CassandraClause {
  def toClause: Clause
}

case class Eq(name: String, value: Any) extends CassandraClause {
  def toClause: Clause = {
    EqOp(this.name, this.value)
  }
}
