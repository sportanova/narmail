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

object TopicIO {
  val client = SimpleClient()
  private lazy val topicIO = new TopicIO(client)
  def apply() = topicIO 
}

class TopicIO(client: SimpleClient) extends QueryIO {
  val table = "topics_by_conversation"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Topic] = {
    curriedFind(clauses)(limit)
  }
  
  def build(row: Row): Topic = {
    val str: java.lang.String = ""
    val set: java.util.Set[String] = setAsJavaSet(Set())

    val id = row.getString("user_id")
    val recipientsHash = row.getString("recipients_hash")
    val threadId = row.getLong("thread_id")
    val subject = row.getString("subject")
    
    Topic(id, recipientsHash, threadId, subject)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.topics_by_conversation " +
    "(user_id, recipients_hash, thread_id, subject) " +
    "VALUES (?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(topic: Topic): Try[Topic] = {
    curriedWrite(topic)
  }
  
  def break(topic: Topic, boundStatement: BoundStatement): BoundStatement = {
//    val set: java.util.Set[String] = conversation.recipients
    val threadId: java.lang.Long = topic.threadId

    boundStatement.bind(
      topic.userId,
      topic.recipientsHash,
      threadId,
      topic.subject
    )
  }
}