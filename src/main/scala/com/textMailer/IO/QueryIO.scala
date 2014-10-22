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
import com.textMailer.models.UserEvent
import org.joda.time.DateTime

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
  def mostRecent[T](models: List[T])(implicit MostRecentItemTC: MostRecentItems[T]): Seq[T] = { // if there are more than one models with the same property, take the one with the most recent timestamp
    val x = models.groupBy(model => MostRecentItemTC.member(model)).map(tuple => tuple match { // TODO: will be problem if n + models have same .member -> will mean every refresh will result in most recent model: store least recent timestamp to use as "NEXT TS VALUE"
      case t if t._2.size > 1 => t._2.reduceLeft((model1, model2) => if(MostRecentItemTC.ts(model1) > MostRecentItemTC.ts(model2)) model1 else model2)
      case t => t._2.head
    }).toSeq.sortBy(model => MostRecentItemTC.ts(model)).reverse

    val y = models.groupBy(model => MostRecentItemTC.member(model)).map(_._2.headOption).filter(_.isDefined).map(_.get).toSeq.sortBy(model => MostRecentItemTC.ts(model)).reverse // TODO If this keeps working - get rid of below line
    if(x == y) println(s"@@@@@@@@@@ x & y same") else UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> "x & y not same", "errorType" -> "refactorFail", "error" -> "x & y not same")))
    y
  }
}