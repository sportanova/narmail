package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.textMailer.models.Conversation
import scala.collection.JavaConverters._
import com.textMailer.models.Conversation
import com.datastax.driver.core.ResultSet
import com.textMailer.models.User
import com.textMailer.models.UserEvent
import scala.util.Try
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.utils.UUIDs
import scala.concurrent.Future
import org.joda.time.DateTime
import com.textMailer.models.Email
import java.util.Properties
import javax.mail.Session
import com.sun.mail.smtp.SMTPTransport
import javax.mail.URLName
import com.sun.mail.util.BASE64EncoderStream
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.Message
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import scala.collection.immutable.StringOps._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.ByteArrayOutputStream
import java.net.URL
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.HttpResponse
import scala.concurrent.ExecutionContext.Implicits.global

object SendEmail {
  implicit val formats = net.liftweb.json.DefaultFormats
  implicit val httpClient = new ApacheHttpClient

  def send(email: Email, gmailUserId: String, accessToken: String): Unit = {
    val body = createSendHTTPBody(email)
    val url = new URL(s"https://www.googleapis.com/upload/gmail/v1/users/$gmailUserId/messages/send?uploadType=multipart") // 100030981325891290860
    val req = POST(url).addHeaders(("authorization", s"Bearer $accessToken"), ("Content-Type", "multipart/related; boundary=narmal_send")).addBody(body)
    req.apply.map(res => {
      val resMap = res.toJValue.values.asInstanceOf[Map[String,Any]]
      val id = resMap.get("id")
      val threadId = resMap.get("threadId")
      
      (for {
        id <- resMap.get("id")
        threadId <- resMap.get("threadId")
      } yield (id, threadId)) match {
        case Some(ids) => {
          val updatedEmail = email.copy(id = ids._1.toString, threadId = Some(ids._2.toString))
          EmailConversationIO().asyncWrite(updatedEmail)
          EmailTopicIO().asyncWrite(updatedEmail)
        }
        case None => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "messageFailedToSend", new DateTime().getMillis, Map("gmailUserId" -> gmailUserId, "email" -> compact(render(decompose(email))), "errorType" -> "payloadNotFound")))
      }
    })
  }
  
  def createSendHTTPBody(email: Email): String = {
    val start = "--narmal_send\nContent-Type: application/json; charset=UTF-8\n\n"
    val end = "\n\n--narmal_send\nContent-Type: message/rfc822\n\n--narmal_send--"

    val rawEmail = createRawMessage(email)
    val headers = createHeaders(email)
    val payload = Map("headers" -> headers, "mimeType" -> "text/plain")
    val json = compact(render(decompose(Map("raw" -> rawEmail, "payload" -> payload))))
    
    start + json + end
  }
  
  def createHeaders(email: Email): List[Map[String,String]] = {
    val formattedTo = email.recipients.get.foldLeft("")((acc, recipient) => {
      acc + " " + recipient._1 + " " + recipient._2 + ","
    }) match {
      case x => x.slice(0, x.length - 1)
    }
    val to = Map("name" -> "to", "value" -> formattedTo)
    
    val inReplyTo = email.inReplyTo match {
      case Some(r) => Some(Map("name" -> "In-Reply-To", "value" -> r))
      case None => None
    }
    
    val reference = email.references match {
      case Some(r) => Some(Map("name" -> "References", "value" -> r))
      case None => None
    }

    val formattedFrom = email.sender.toList.headOption match {
      case Some(sender) => sender._1 + " " + sender._2
      case None => throw new IllegalArgumentException("this message has no sender"); ""
    }
    val from = Map("name" -> "from", "value" -> formattedFrom)
    
    val subject = Map("name" -> "subject", "value" -> email.subject)
    
    List(Some(to), Some(from), Some(subject), inReplyTo, reference).filter(_.isDefined).map(_.get)
  }

  def createRawMessage(email: Email): String = {
    val fmt = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")

    val from = s"From: ${formatRawMessageRecipients(email.sender)}\n"
    val to = s"To: ${formatRawMessageRecipients(email.recipients.get)}\n"
    val subject = "Subject: " + email.subject + "\n"
    val date = "Date: " + fmt.print(new DateTime(email.ts)) + "\n\n"
    val message = email.textBody
    
    Base64.encodeBase64URLSafeString((from + to + subject + date + message).getBytes)
  }

  def formatRawMessageRecipients(recipients: Map[String,String]): String = {
    val str = recipients.foldLeft("")((acc, recipients) => {
      acc + " " + recipients._1 + " <" + recipients._2 + ">," 
    })
    str.slice(0, str.length - 1)
  }
}