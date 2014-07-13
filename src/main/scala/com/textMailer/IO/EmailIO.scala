package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Email
import scala.collection.JavaConverters._

object EmailIO {
  val session = SimpleClient().getSession
  private lazy val emailIO = new EmailIO(session)
  def apply() = emailIO 
}

class EmailIO(session: Session) {
  def find(limit: Int): List[Email] = {
    val query = QueryBuilder.select().all().from("app","emails_by_conversation").limit(limit)
    session.execute(query).all.asScala.toList.map(row => build(row))
  }
  
  def build(row: Row): Email = {
    val id = row.getString("user_id")
    val userId = row.getString("user_id")
    val subject = row.getString("subject")
    val recipients = row.getString("recipients_string")
    val time = row.getString("time")
    val cc = row.getString("cc")
    val bcc = row.getString("bcc")
    val body = row.getString("body")
    
    Email(id, userId, subject, recipients, time, cc, bcc, body)
  }
}