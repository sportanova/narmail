package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Email
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import com.textMailer.models.Model
import org.joda.time.DateTime
import com.datastax.driver.core.ResultSet
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object EmailTopicIO {
  val client = SimpleClient()
  private lazy val emailIO = new EmailIO(client, "emails_by_topic")
  def apply() = emailIO
}

object EmailConversationIO {
  val client = SimpleClient()
  private lazy val emailIO = new EmailIO(client, "emails_by_conversation")
  def apply() = emailIO
}

class EmailIO(client: SimpleClient, table: String) extends QueryIO {
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
  
  val asyncCurriedCount = asyncCurryCount(keyspace)(table)(build)(session) _

  def asyncCount(clauses: List[CassandraClause], limit: Int): Future[Long] = {
    asyncCurriedCount(clauses)(limit).map(row => {
      row.headOption match {
        case Some(r) => r.getLong(0)
        case None => 0l
      }
    })
  }
  
  def build(row: Row): Email = {
    val str: java.lang.String = ""

    val id = row.getString("id")
    val userId = row.getString("user_id")
    val threadId = row.getString("thread_id") match {
      case null => None
      case x => Some(x)
    }
    val subject = row.getString("subject")
    val sender: Map[String,String] = row.getMap("sender", str.getClass, str.getClass).toMap
    val recipientsHash = row.getString("recipients_hash")
    val recipients: Option[Map[String,String]] = row.getMap("recipients", str.getClass, str.getClass).toMap match {
      case x: Map[String,String] => Some(x)
      case null => None
    }
    val ts: java.lang.Long = row.getLong("ts")
    val cc = row.getString("cc")
    val bcc = row.getString("bcc")
    val textBody = row.getString("text_body")
    val htmlBody = row.getString("html_body")
    val msgId = row.getString("msg_id")
    val inReplyTo: Option[String] = row.getString("reply_to") match {
      case x: String => Some(x)
      case null => None
    }
    val references: Option[String] = row.getString("references") match {
      case x: String => Some(x)
      case null => None
    }
    
    Email(id, userId, threadId, recipientsHash, recipients, ts, subject, sender, cc, bcc, textBody, htmlBody, msgId, inReplyTo, references)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(id, user_id, thread_id, recipients_hash, recipients, ts, subject, sender, cc, bcc, text_body, html_body, msg_id, reply_to, references) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(email: Email): Try[Email] = {
    curriedWrite(email)
  }
  
  def break(email: Email, boundStatement: BoundStatement): BoundStatement = {
    val ts: java.lang.Long = email.ts
    val recipients = email.recipients match {
      case Some(r) => mapAsJavaMap(r)
      case None => null
    }
    val inReplyTo = email.inReplyTo match {
      case Some(r) => r
      case None => null
    }
    val references = email.references match {
      case Some(r) => r
      case None => null
    }
    val threadId = email.threadId match {
      case Some(x) => x
      case None => null
    } 

    boundStatement.bind(
      email.id,
      email.userId,
      threadId,
      email.recipientsHash,
      recipients,
      ts,
      email.subject,
      mapAsJavaMap(email.sender),
      email.cc,
      email.bcc,
      email.textBody,
      email.htmlBody,
      email.messageId,
      inReplyTo,
      references
    )
  }
}