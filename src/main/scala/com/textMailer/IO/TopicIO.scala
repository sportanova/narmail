package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Conversation
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import com.textMailer.models.Conversation
import com.datastax.driver.core.ResultSet
import scala.util.Try
import com.textMailer.models.Topic
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object TopicIO {
  val client = SimpleClient()
  private lazy val topicIO = new TopicIO(client, "topics_by_conversation")
  def apply() = topicIO 
}

object OrdTopicIO {
  val client = SimpleClient()
  private lazy val topicIO = new TopicIO(client, "ordered_topics_by_conversation")
  def apply() = topicIO
}

class TopicIO(client: SimpleClient, table: String) extends QueryIO {
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Topic] = {
    curriedFind(clauses)(limit)
  }
  
  val asyncCurriedFind = asyncCurryFind(keyspace)(table)(build)(session) _

  def asyncFind(clauses: List[CassandraClause], limit: Int): Future[List[Topic]] = {
    asyncCurriedFind(clauses)(limit)
  }
  
  val asyncCurriedWrite = asyncCurryWrite(session)(preparedStatement)(break) _
  
  def asyncWrite(topic: Topic): Future[Topic] = {
    asyncCurriedWrite(topic)
  }
  
  val asyncCurriedCount = asyncCurryCount(keyspace)(table)(build)(session) _

  def asyncCount(clauses: List[CassandraClause], limit: Int): Future[Long] = {
    asyncCurriedCount(clauses)(limit).map(row => {
      row.headOption match {
        case Some(r) => r.getLong(0)
        case None => 0l
      }
    })
  }
  
  def build(row: Row): Topic = {
    val str: java.lang.String = ""
    val set: java.util.Set[String] = setAsJavaSet(Set())

    val id = row.getString("user_id")
    val recipientsHash = row.getString("recipients_hash")
    val threadId = row.getLong("thread_id")
    val subject = row.getString("subject")
    val ts = row.getLong("ts")
    val emailCount = row.getLong("em_cnt")
    
    Topic(id, recipientsHash, threadId, subject, ts, emailCount)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(user_id, recipients_hash, thread_id, subject, ts, em_cnt) " +
    "VALUES (?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(topic: Topic): Try[Topic] = {
    curriedWrite(topic)
  }
  
  def break(topic: Topic, boundStatement: BoundStatement): BoundStatement = {
    val threadId: java.lang.Long = topic.threadId
    val ts: java.lang.Long = topic.ts
    val emailCount: java.lang.Long = topic.emailCount

    boundStatement.bind(
      topic.userId,
      topic.recipientsHash,
      threadId,
      topic.subject,
      ts,
      emailCount
    )
  }
}