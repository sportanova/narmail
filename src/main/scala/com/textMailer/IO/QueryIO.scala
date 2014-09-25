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
import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.BoundStatement
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.TimeUnit.SECONDS
import scala.concurrent.Future
import com.textMailer.TypeClass.TypeClass.MostRecentItems

trait QueryIO {
  import cassandra.resultset._
  import scala.concurrent.ExecutionContext.Implicits.global

  // TODO: wrap in Try?
  def curryFind[T <: CassandraClause, A <: Model](keyspace: String)(table: String)(build: Row => A)(session: Session)(clauses: List[T])( limit: Int): List[A] = {
    val query = QueryBuilder.select().all().from(keyspace,table).limit(limit)
    val queryWithClauses = addWhereClauses(query, clauses)
    session.executeAsync(query).getUninterruptibly(10l, SECONDS).asScala.toList.map(row => build(row))
  }

  def asyncCurryFind[T <: CassandraClause, A <: Model](keyspace: String)(table: String)(build: Row => A)(session: Session)(clauses: List[T])( limit: Int): Future[List[A]] = {
    val query = QueryBuilder.select().all().from(keyspace,table).limit(limit)
    val queryWithClauses = addWhereClauses(query, clauses)
    session.executeAsync(query).map(_.all().asScala.toList.map(row => build(row))) 
  }
  
  def asyncCurryCount[T <: CassandraClause, A <: Model](keyspace: String)(table: String)(build: Row => A)(session: Session)(clauses: List[T])( limit: Int): Future[List[Row]] = {
    val query = QueryBuilder.select().countAll().from(keyspace,table).limit(limit)
    val queryWithClauses = addWhereClauses(query, clauses)
    session.executeAsync(query).map(_.all().asScala.toList) 
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
  
  def curryWrite[A <: Model](session: Session)(preparedStatement: PreparedStatement)(break: (A,BoundStatement) => BoundStatement)(model: A): Try[A] = {
    val unboundBoundStatement = new BoundStatement(preparedStatement);
    val boundStatement = break(model, unboundBoundStatement)
    Try(session.execute(boundStatement)) match {
      case Success(s) => Success(model)
      case Failure(ex) => Failure(ex)
    }
  }
  
  def asyncCurryWrite[A <: Model](session: Session)(preparedStatement: PreparedStatement)(break: (A,BoundStatement) => BoundStatement)(model: A): Future[A] = {
    val unboundBoundStatement = new BoundStatement(preparedStatement);
    val boundStatement = break(model, unboundBoundStatement)
    session.executeAsync(boundStatement).map(resultSet => {model})
  }
}

object QueryIO {
  def mostRecent[T](models: List[T])(implicit model: MostRecentItems[T]): Seq[T] = { // if there are more than one models with the same property, take the one with the most recent timestamp
    models.groupBy(item => model.member(item)).map(tuple => tuple match {
      case t if t._2.size > 1 => t._2.reduceLeft((conv1, conv2) => if(model.ts(conv1) > model.ts(conv2)) conv1 else conv2)
      case t => t._2.head
    }).toSeq.sortBy(item => model.ts(item)).reverse
  }
}