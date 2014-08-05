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

object ConversationIO {
  val client = SimpleClient()
  private lazy val conversationIO = new ConversationIO(client)
  def apply() = conversationIO 
}

class ConversationIO(client: SimpleClient) extends QueryIO {
  val table = "conversations_by_user"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Conversation] = {
    curriedFind(clauses)(limit)
  }
  
  def build(row: Row): Conversation = {
    val str: java.lang.String = ""
    val set: java.util.Set[String] = setAsJavaSet(Set())

    val id = row.getString("user_id")
    val subject = row.getString("subject")
    val recipientsHash = row.getString("recipients_hash")
    val recipients = row.getSet("recipients", str.getClass).asScala.toSet[String]
    
    Conversation(id, subject, recipientsHash, recipients)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.conversations_by_user " +
    "(user_id, subject, recipients_hash, recipients) " +
    "VALUES (?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(conversation: Conversation): Try[Conversation] = {
    curriedWrite(conversation)
  }
  
  def break(conversation: Conversation, boundStatement: BoundStatement): BoundStatement = {
//    val set: java.util.Set[String] = conversation.recipients

    boundStatement.bind(
      conversation.userId,
      conversation.subject,
      conversation.recipientsHash,
      setAsJavaSet(conversation.recipients)
    )
  }
}