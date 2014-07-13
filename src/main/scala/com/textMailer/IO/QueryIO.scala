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
  def curriedFind[T <: CassandraClause](clauses: List[T], limit: Int)(implicit table: String, build: Row => Model, session: Session): List[Model] = {
    val query = QueryBuilder.select().all().from("app",table).limit(limit)
    val queryWithClauses = addWhereClauses(query, clauses)
    session.execute(query).all.asScala.toList.map(row => build(row))
  }
  
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