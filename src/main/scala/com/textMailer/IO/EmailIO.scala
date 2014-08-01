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
import scala.util.Try

object EmailIO {
  val client = SimpleClient()
  private lazy val emailIO = new EmailIO(client)
  def apply() = emailIO
}

class EmailIO(client: SimpleClient) extends QueryIO {
  val table = "emails_by_conversation"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

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
    s"INSERT INTO $keyspace.emails_by_conversation " +
    "(id, user_id, subject, recipients_string, time, recipients, cc, bcc, body) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(email: Email): Try[Email] = {
    curriedWrite(email)
  }
  
  def break(email: Email, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      email.id,
      email.userId,
      email.subject,
      email.recipients.toString,
      email.time,
      email.recipients,
      email.cc,
      email.bcc,
      email.body
    )
  }
}