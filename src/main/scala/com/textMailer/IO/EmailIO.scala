package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Email
import scala.collection.JavaConverters._
import com.textMailer.models.Model
import org.joda.time.DateTime
import com.datastax.driver.core.ResultSet

object EmailIO {
  val session = SimpleClient().getSession
  private lazy val emailIO = new EmailIO(session)
  def apply() = emailIO
}

class EmailIO(session: Session) extends QueryIO {
  val table = "emails_by_conversation"

  val curriedFind = curryFind(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Email] = {
    curriedFind(clauses)(limit)
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
  
  val preparedStatement = session.prepare(
      "INSERT INTO app.emails_by_conversation " +
      "(id, user_id, subject, recipients_string, time, recipients, cc, bcc, body) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(email: Email): ResultSet = {
    curriedWrite(email)
  }
  
  def break(email: Email, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      email.id,
      email.userId,
      email.subject,
      email.recipients.toString,
      new DateTime().getMillis().toString,
      email.recipients,
      email.cc,
      email.bcc,
      email.body
    )
  }
}