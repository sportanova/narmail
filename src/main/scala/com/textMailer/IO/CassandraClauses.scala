package com.textMailer.IO

import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => EqOp}
import com.datastax.driver.core.querybuilder.QueryBuilder.{gt => GtOp}
import com.datastax.driver.core.querybuilder.QueryBuilder.{lt => LtOp}
import com.datastax.driver.core.querybuilder.Clause

sealed trait CassandraClause {
  def toClause: Clause
}

case class Eq(name: String, value: Any) extends CassandraClause {
  def toClause: Clause = {
    EqOp(this.name, this.value)
  }
}

case class Gt(name: String, value: Any) extends CassandraClause {
  def toClause: Clause = {
    GtOp(this.name, this.value)
  }
}

case class Lt(name: String, value: Any) extends CassandraClause {
  def toClause: Clause = {
    LtOp(this.name, this.value)
  }
}