package com.textMailer.IO

import com.datastax.driver.core.Session
import com.textMailer.models.Conversation
import com.datastax.driver.core.querybuilder._
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => EqOp}
import com.datastax.driver.core.Session
import scala.collection.JavaConverters._
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Model

trait QueryIO {
  def curryFind[T <: CassandraClause, A <: Model](table: String)( build: Row => A)(session: Session)(clauses: List[T])( limit: Int): List[A] = {
    val query = QueryBuilder.select().all().from("app",table).limit(limit)
    val queryWithClauses = addWhereClauses(query, clauses)
    session.execute(query).all.asScala.toList.map(row => build(row))
  }
  val curryFindFn: String => (Row => Model) => Session => List[CassandraClause] => Int => List[Model] = curryFind _
  
  def addWhereClauses(query: Select, allClauses: List[CassandraClause]): Select = {
    allClauses match {
      case clause :: clauses => {
        query.where.and(clause.toClause)
        addWhereClauses(query, clauses)
      }
      case Nil => query
    }
  }
}