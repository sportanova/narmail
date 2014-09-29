package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import java.security.MessageDigest
import java.math.BigInteger
import scala.collection.immutable.TreeSet
import com.textMailer.models.Conversation
import scala.collection.immutable.SortedSet
import com.textMailer.models.Topic
import com.textMailer.IO.TopicIO
import com.textMailer.IO.OrdTopicIO
import com.textMailer.models.Email
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq
import scala.concurrent.ExecutionContext.Implicits.global
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.OrdConversationIO
import com.textMailer.IO.EmailTopicIO
import com.textMailer.IO.EmailConversationIO
import org.scalatra.Get
import scala.util.Success
import scala.util.Failure
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.HttpResponse
import java.net.URL
import net.liftweb.json.JsonParser
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import scala.concurrent.Future
import net.liftweb.json.JsonAST.JValue

object SaveEmailDataActor {
  case class SaveData(userId: String, to: Set[String], cc: Set[String], bcc: Set[String], emailAddress: String, sender: String, subject: String, ts: Long, threadId: Long,
    gmId: Long, emailAccountId: String, body: Map[String, Option[Object]]
  )
  case class GetGmailMessage(messageData: Map[String,String], gmailUserId: String, accessToken: String)
}

class SaveEmailDataActor extends Actor {
  import com.textMailer.IO.actors.SaveEmailDataActor._
  implicit val httpClient = new ApacheHttpClient

  def receive = {
    case GetGmailMessage(messageData, gmailUserId, accessToken) => {
      val messageUrl = new URL(s"https://www.googleapis.com/gmail/v1/users/$gmailUserId/messages/${messageData.get("id").get}")
      val messageReq = GET(messageUrl).addHeaders(("authorization", s"Bearer $accessToken"))
      val messageRes: Future[Map[String,String]] = messageReq.apply.map(m => {
        val bodies = getMessageBodies(m.toJValue)
        println(s"%%%%%%%%%%%%%%%%%%%% bodiesText ${bodies.get("textBody").get.length} bodiesHtml ${bodies.get("textBody").get.length}")
        bodies
      })
      
//      messageRes onComplete {
//        case Success(s) => println(s"############# s $s")
//        case Failure(ex) => println(s"@@@@@@@@ failure $ex")
//      }
    }
    case SaveData(userId, to, cc, bcc, emailAddress, sender, subject, ts, threadId, gmId, emailAccountId, body) => {
      val recipients: TreeSet[String] = TreeSet[String]() ++ to ++ bcc ++ cc - emailAddress + sender
      println(s"@@@@@@@@@@@ recipientsSet $recipients")
      val recipientsString = recipients.toString
      val recipientsHash = md5Hash(recipientsString)
      
      val topicExists = TopicIO().asyncFind(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash), Eq("thread_id", threadId)), 100).map(topic => {topic.headOption}) // get futures started => => =>
      val topicCount = TopicIO().asyncCount(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100)
      val emailsByTopicCount = EmailTopicIO().asyncCount(List(Eq("user_id", userId), Eq("thread_id", threadId)), 100).map(c => c + 1l)
      val emailsByConversationCount = EmailConversationIO().asyncCount(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100).map(c => c + 1l)

      val textBody = (for{
        textValue <- body.get("text")
        text <- textValue
      } yield(text)) match {
        case Some(t) => t.toString
        case None => ""
      }
      
      val htmlBody = (for{
        htmlValue <- body.get("html")
        html <- htmlValue
      } yield(html)) match {
        case Some(h) => h.toString.split("""<div class="gmail_extra">""").toList.head.replace("\n", " ").replace("\r", " ")
        case None => ""
      }

      val email = Email(gmId, userId, threadId, recipientsHash, Some(recipients), ts, subject, sender, cc.toString, bcc.toString, textBody, htmlBody)
      EmailTopicIO().asyncWrite(email)
      EmailConversationIO().asyncWrite(email)
      
      (for {
        te <- topicExists
        tc <- topicCount
        etc <- emailsByTopicCount
        ecc <- emailsByConversationCount
      } yield(te, tc, etc, ecc)) onComplete {
        case Success(c) => {
          val trueTopicCount = c._1 match {
            case Some(t) => c._2
            case None => c._2 + 1l
          }
        
          val conversation = Conversation(userId, recipientsHash, recipients, ts, emailAccountId, trueTopicCount, c._4) // do this last, give time to get topic count
          ConversationIO().asyncWrite(conversation)
          OrdConversationIO().asyncWrite(conversation)
          
          val topic = Topic(userId, recipientsHash, threadId, subject, ts, c._3)
          TopicIO().asyncWrite(topic)
          OrdTopicIO().asyncWrite(topic)
        }
        case Failure(ex) => {
          println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FAILED TO GET COUNTS")
          val conversation = Conversation(userId, recipientsHash, recipients, ts, emailAccountId, 0l, 0l) // do this last, give time to get topic count
          ConversationIO().asyncWrite(conversation)
          OrdConversationIO().asyncWrite(conversation)
          
          val topic = Topic(userId, recipientsHash, threadId, subject, ts, 0l)
          TopicIO().asyncWrite(topic)
          OrdTopicIO().asyncWrite(topic)
        }
      }
    }
  }
  
  def getMessageBodies(json: JValue): Map[String,String] = {
    (for {
      body <- json.values.asInstanceOf[Map[String,Any]].get("body")
      payload <- JsonParser.parse(body.toString).values.asInstanceOf[Map[String,Any]].get("payload")
      headers <- payload.asInstanceOf[Map[String,Any]].get("headers")
    } yield(body, payload, headers)) match {
      case Some(m) => {
        (for {
          parts <- m._2.asInstanceOf[Map[String,Any]].get("parts")
          textMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/plain"))
          text <- textMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
          htmlMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/html"))
          html <- htmlMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
        } yield(text, html)) match {
          case Some(b) => Map("textBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._1)), "htmlBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._2)))
          case None => {
            (for {
              body <- m._1.asInstanceOf[Map[String,Any]].get("body")
              text <- body.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
            } yield(text, "")) match {
              case Some(b) => Map("textBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._1)), "htmlBody" -> "")
              case None => Map()
            }
          }
        }
      }
      case None => Map()
    }
  }
  
  def md5Hash(str: String) = {
    val md = MessageDigest.getInstance("MD5")
    md.reset()
    md.update(str.getBytes());
    val digest = md.digest()
    val bigInt = new BigInteger(1,digest)
    bigInt.toString(16)
  }
}