package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Conversation
import scala.collection.JavaConverters._
import com.textMailer.models.Conversation
import com.datastax.driver.core.ResultSet
import com.textMailer.models.User
import scala.util.Try
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.utils.UUIDs
import scala.concurrent.Future
import com.textMailer.models.UserEvent

object UserEventIO {
  val client = SimpleClient()
  private lazy val UserEventIO = new UserEventIO(client)
  def apply() = UserEventIO 
}

class UserEventIO(client: SimpleClient) extends QueryIO {
  val table = "user_events"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[UserEvent] = {
    curriedFind(clauses)(limit)
  }
  
  val asyncCurriedFind = asyncCurryFind(keyspace)(table)(build)(session) _

  def asyncFind(clauses: List[CassandraClause], limit: Int): Future[List[UserEvent]] = {
    asyncCurriedFind(clauses)(limit)
  }
  
  val asyncCurriedWrite = asyncCurryWrite(session)(preparedStatement)(break) _
  
  def asyncWrite(userEvent: UserEvent): Future[UserEvent] = {
    asyncCurriedWrite(userEvent)
  }
  
  def build(row: Row): UserEvent = {
    val userId = row.getUUID("user_id")
    val eventType = row.getString("event_type")
    val ts = row.getLong("ts")
    
    UserEvent(userId, eventType, ts)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(user_id, event_type, ts) " +
    "VALUES (?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(userEvent: UserEvent): Try[UserEvent] = {
    curriedWrite(userEvent)
  }
  
  def break(userEvent: UserEvent, boundStatement: BoundStatement): BoundStatement = {
    val ts: java.lang.Long = userEvent.ts

    boundStatement.bind(
      userEvent.userId,
      userEvent.eventType,
      ts
    )
  }
}