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
  val table = "emails_by_topic"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Email] = {
    curriedFind(clauses)(limit)
  }
  
  def build(row: Row): Email = {
    val id = row.getString("user_id")
    val userId = row.getString("user_id")
    val threadId: java.lang.Long = row.getLong("thread_id")
    val subject = row.getString("subject")
    val recipientsHash = row.getString("recipients_hash")
    val time = row.getString("time")
    val cc = row.getString("cc")
    val bcc = row.getString("bcc")
    val bodyText = row.getString("body_text")
    val bodyHtml = row.getString("body_html")
    
    Email(id, userId, threadId, recipientsHash, time, subject, cc, bcc, bodyText, bodyHtml)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(id, user_id, thread_id, recipients_hash, time, subject, cc, bcc, body_text, body_html) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(email: Email): Try[Email] = {
    curriedWrite(email)
  }
  
  def break(email: Email, boundStatement: BoundStatement): BoundStatement = {
    val threadId: java.lang.Long = email.threadId

    boundStatement.bind(
      email.id,
      email.userId,
      threadId,
      email.recipientsHash,
      email.time,
      email.subject,
      email.cc,
      email.bcc,
      email.bodyText,
      email.bodyHtml
    )
  }
}