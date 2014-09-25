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
import scala.concurrent.Future
import com.textMailer.Implicits.ImplicitConversions._

object ConversationIO {
  val client = SimpleClient()
  private lazy val conversationIO = new ConversationIO(client, "conversations_by_user")
  def apply() = conversationIO 
}

object OrdConversationIO {
  val client = SimpleClient()
  private lazy val conversationIO = new ConversationIO(client, "ordered_conversations_by_user")
  def apply() = conversationIO 
}

class ConversationIO(client: SimpleClient, table: String) extends QueryIO {
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Conversation] = {
    curriedFind(clauses)(limit)
  }
  
  val asyncCurriedFind = asyncCurryFind(keyspace)(table)(build)(session) _

  def asyncFind(clauses: List[CassandraClause], limit: Int): Future[List[Conversation]] = {
    asyncCurriedFind(clauses)(limit)
  }
  
  val asyncCurriedWrite = asyncCurryWrite(session)(preparedStatement)(break) _
  
  def asyncWrite(conversation: Conversation): Future[Conversation] = {
    asyncCurriedWrite(conversation)
  }
  
  def build(row: Row): Conversation = {
    val str: java.lang.String = ""

    val userId = row.getString("user_id")
    val recipientsHash = row.getString("recipients_hash")
    val recipients = row.getSet("recipients", str.getClass).asScala.toSet[String]
    val ts = row.getLong("ts")
    val emailAccountId = row.getString("email_account_id")
    val topicCount = row.getLong("tp_cnt")
    val emailCount = row.getLong("em_cnt")
    
    Conversation(userId, recipientsHash, recipients, ts, emailAccountId, topicCount, emailCount)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(user_id,recipients_hash, recipients, ts, email_account_id, tp_cnt, em_cnt) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(conversation: Conversation): Try[Conversation] = {
    curriedWrite(conversation)
  }
  
  def break(conversation: Conversation, boundStatement: BoundStatement): BoundStatement = {
    val topicCount: java.lang.Long = conversation.topicCount
    val emailCount: java.lang.Long = conversation.emailCount

    boundStatement.bind(
      conversation.userId,
      conversation.recipientsHash,
      setAsJavaSet(conversation.recipients),
      conversation.ts.asInstanceOf[java.lang.Long],
      conversation.emailAccountId,
      topicCount,
      emailCount
    )
  }
}