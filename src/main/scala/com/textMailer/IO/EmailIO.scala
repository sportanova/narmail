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
import scala.concurrent.Future

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
  
  val asyncCurriedFind = asyncCurryFind(keyspace)(table)(build)(session) _

  def asyncFind(clauses: List[CassandraClause], limit: Int): Future[List[Email]] = {
    asyncCurriedFind(clauses)(limit)
  }
  
  val asyncCurriedWrite = asyncCurryWrite(session)(preparedStatement)(break) _
  
  def asyncWrite(email: Email): Future[Email] = {
    asyncCurriedWrite(email)
  }
  
  def build(row: Row): Email = {
    val id: java.lang.Long = row.getLong("id")
    val userId = row.getString("user_id")
    val threadId: java.lang.Long = row.getLong("thread_id")
    val subject = row.getString("subject")
    val sender = row.getString("sender")
    val recipientsHash = row.getString("recipients_hash")
    val ts: java.lang.Long = row.getLong("ts")
    val cc = row.getString("cc")
    val bcc = row.getString("bcc")
    val textBody = row.getString("text_body")
    val htmlBody = row.getString("html_body")
    
    Email(id, userId, threadId, recipientsHash, ts, subject, sender, cc, bcc, textBody, htmlBody)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(id, user_id, thread_id, recipients_hash, ts, subject, sender, cc, bcc, text_body, html_body) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(email: Email): Try[Email] = {
    curriedWrite(email)
  }
  
  def break(email: Email, boundStatement: BoundStatement): BoundStatement = {
    val threadId: java.lang.Long = email.threadId
    val ts: java.lang.Long = email.ts
    val id: java.lang.Long = email.id

    boundStatement.bind(
      id,
      email.userId,
      threadId,
      email.recipientsHash,
      ts,
      email.subject,
      email.sender,
      email.cc,
      email.bcc,
      email.textBody,
      email.htmlBody
    )
  }
}