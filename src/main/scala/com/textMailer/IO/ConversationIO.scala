package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.model.Conversation
import scala.collection.JavaConverters._

object ConversationIO {
  val session = SimpleClient().getSession
  private lazy val conversationIO = new ConversationIO(session)
  def apply() = conversationIO 
}

class ConversationIO(session: Session) {
  def find(limit: Int): List[Conversation] = {
    val query = QueryBuilder.select().all().from("app","conversations_by_user").limit(limit)
    session.execute(query).all.asScala.toList.map(row => build(row))
  }
  
  def build(row: Row): Conversation = {
    val id = row.getString("user_id")
    val subject = row.getString("subject")
    val recipients = row.getString("recipients_string")
    
    Conversation(id, subject, recipients)
  }
}