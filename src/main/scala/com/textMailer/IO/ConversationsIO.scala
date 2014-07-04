package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.model.Conversation
import scala.collection.JavaConverters._

object ConversationsIO {
  val session = SimpleClient().getSession
  private lazy val conversationsIO = new ConversationsIO(session)
  def apply() = conversationsIO 
}

class ConversationsIO(session: Session) {
  def find(limit: Int): List[Conversation] = {
    val query = QueryBuilder.select().all().from("app","conversations").limit(limit)
    session.execute(query).all.asScala.toList.map(row => build(row))
  }
  
  def build(row: Row): Conversation = {
    val id = row.getString("user_id")
    val subject = row.getString("subject")
    val recipients = row.getString("recipients_string")
    
    Conversation(id, subject, recipients)
  }
}