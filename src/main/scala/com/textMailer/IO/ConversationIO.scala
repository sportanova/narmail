package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Conversation
import scala.collection.JavaConverters._
import com.textMailer.models.Conversation

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
    val id = row.getString("user_id")
    val subject = row.getString("subject")
    val recipients = row.getString("recipients_string")
    
    Conversation(id, subject, recipients)
  }
}