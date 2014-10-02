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
import scala.util.Try
import annotation.tailrec
import com.textMailer.IO.UserEventIO
import com.textMailer.models.UserEvent
import org.joda.time.DateTime
import net.liftweb.json._
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object SaveEmailActor {
  case class SaveGmailMessage(messageJson: JValue, emailAddress: String, userId: String)
}

class SaveEmailActor extends Actor {
  import com.textMailer.IO.actors.SaveEmailActor._
  implicit val httpClient = new ApacheHttpClient
  implicit val fmt = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");

  def receive = {
    case SaveGmailMessage(json, emailAddress, userId) => {
      println(s"======================== getting to SaveEmail child actor")
      val bodies = findMessageBodies(json)
      val metaData = findMessageMetaData(json, emailAddress)
      
      val recipientsHash = metaData._9
  
//      val topicExists = TopicIO().asyncFind(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100).map(topic => {topic.headOption}) // get futures started => => =>
//      val topicCount = TopicIO().asyncCount(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100)
//      val emailsByTopicCount = EmailTopicIO().asyncCount(List(Eq("user_id", userId), Eq("thread_id", threadId)), 100).map(c => c + 1l)
//      val emailsByConversationCount = EmailConversationIO().asyncCount(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100).map(c => c + 1l)
//      
//      val textBody = (for{
//        textValue <- body.get("text")
//        text <- textValue
//      } yield(text)) match {
//        case Some(t) => t.toString
//        case None => ""
//      }
//      
//      val htmlBody = (for{
//        htmlValue <- body.get("html")
//        html <- htmlValue
//      } yield(html)) match {
//        case Some(h) => h.toString.split("""<div class="gmail_extra">""").toList.head.replace("\n", " ").replace("\r", " ")
//        case None => ""
//      }
//
//      val email = Email(gmId, userId, threadId, recipientsHash, Some(recipients), ts, subject, sender, cc.toString, bcc.toString, textBody, htmlBody)
//      EmailTopicIO().asyncWrite(email)
//      EmailConversationIO().asyncWrite(email)
//      
//      (for {
//        te <- topicExists
//        tc <- topicCount
//        etc <- emailsByTopicCount
//        ecc <- emailsByConversationCount
//      } yield(te, tc, etc, ecc)) onComplete {
//        case Success(c) => {
//          val trueTopicCount = c._1 match {
//            case Some(t) => c._2
//            case None => c._2 + 1l
//          }
//        
//          val conversation = Conversation(userId, recipientsHash, recipients, ts, emailAccountId, trueTopicCount, c._4) // do this last, give time to get topic count
//          ConversationIO().asyncWrite(conversation)
//          OrdConversationIO().asyncWrite(conversation)
//          
//          val topic = Topic(userId, recipientsHash, threadId, subject, ts, c._3)
//          TopicIO().asyncWrite(topic)
//          OrdTopicIO().asyncWrite(topic)
//        }
//        case Failure(ex) => {
//          println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FAILED TO GET COUNTS")
//          val conversation = Conversation(userId, recipientsHash, recipients, ts, emailAccountId, 0l, 0l) // do this last, give time to get topic count
//          ConversationIO().asyncWrite(conversation)
//          OrdConversationIO().asyncWrite(conversation)
//          
//          val topic = Topic(userId, recipientsHash, threadId, subject, ts, 0l)
//          TopicIO().asyncWrite(topic)
//          OrdTopicIO().asyncWrite(topic)
//        }
    }
    case _ => sender ! "didn't match in SaveEmailDataActor"
  }

  type Subject = Option[String]; type Recipients = Option[List[Map[String,String]]]; type Time = Option[Long]; type Id = Option[String]; type RecipientsHash = String

  def findMessageMetaData(json: JValue, emailAddress: String)(implicit fmt: DateTimeFormatter): (Subject, Recipients, Recipients, Recipients, Recipients, Id, Id, Time, RecipientsHash) = {
    val payload = for {
      payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
    } yield payload
    
    payload match {
      case Some(x) =>
      case None => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> pretty(render(json)), "errorType" -> "payloadNotFound")))
    }
    
    val headers = for {
      payload <- payload
      headers <- payload.asInstanceOf[Map[String,Any]].get("headers")
    } yield headers
    
    val subject = for {
      headers <- headers
      subjectOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Subject"))
      subject <- subjectOpt.get("value")
    } yield subject
//    println(s"############## subject $subject")
    
    val to = (for {
      headers <- headers
      toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("To"))
      to <- toOpt.get("value")
    } yield to) match {
      case Some(s) => Some(getRecipientInfo(s))
      case None => {
        (for {
          headers <- headers
          toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Delivered-To"))
          to <- toOpt.get("value")
        } yield getRecipientInfo(to))
      }
    }
//    println(s"############## to $to")
    
    val from = for {
      headers <- headers
      fromOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("From"))
      from <- fromOpt.get("value")
    } yield getRecipientInfo(from)
//    println(s"############## from $from")
    
    val cc = for {
      headers <- headers
      ccOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Cc"))
      cc <- ccOpt.get("value")
    } yield getRecipientInfo(cc)
//    println(s"############## cc $cc")
    
    val allRecipients = Some((to.getOrElse(List()) ++ from.getOrElse(List()) ++ cc.getOrElse(List())).filterNot(_.get("eAddress") == Some(emailAddress)))
//    println(s"@@@@@@@@@@@@@@ allRecipients $allRecipients")

    val recipientsTreeSet = TreeSet[String]() ++ allRecipients.get.flatMap(x => {
      x.map {case (k,v) => v}
    }).toSet    
    //    println(s"@@@@@@@@@@@@@@ recipientsTreeSet $recipientsTreeSet")

    val recipientsString = recipientsTreeSet.toString
    val recipientsHash = md5Hash(recipientsString)
    
    val threadId = for {
      threadId <- json.values.asInstanceOf[Map[String,Any]].get("threadId")
    } yield threadId.toString
//    println(s"############## threadId $threadId")
    
    val messageId = for {
      messageId <- json.values.asInstanceOf[Map[String,Any]].get("id")
    } yield messageId.toString
//    println(s"############## messageId $messageId")
    
    val time = (for {
      headers <- headers
      timeOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Date"))
      time <- timeOpt.get("value")
      timeLong <- findTime(time)
    } yield timeLong)
//    println(s"############## time $time")
 
    (subject, allRecipients, to, from, cc, threadId, messageId, time, recipientsHash)
  }
  
  def findTime(time: String)(implicit fmt: DateTimeFormatter): Option[Long] = {
    val sanitizedTime = time.split("\\(")
    Try{DateTime.parse(sanitizedTime(0).trim, fmt).getMillis} match {
      case Success(l) => Some(l)
      case Failure(ex) => {
        UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> time, "errorType" -> "timeIssue", "error" -> ex.toString)))
        None
      }
    }
  }
  
  def getRecipientInfo(recipients: String): List[Map[String,String]] = {
    val tryRecipients = recipients.split(",").toList.map(u => u.split(" <").toList.map(c => c.replaceAll(">", "").trim)).map(list => {
      list match {
        case xs if xs.size == 2 => Try{Map("name" -> xs(0), "eAddress" -> xs(1))}
        case xs if xs.size == 1 => Try{Map("name" -> xs(0), "eAddress" -> xs(0))}
        case _ => {
          UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> recipients, "errorType" -> "recipientsIssue")))
          Failure(new IllegalArgumentException("Couldn't get Recipients"))
        }
      }
    })

    tryRecipients.map(recipient => {
      recipient match {
        case Success(s) => Some(s)
        case Failure(ex) => {
          UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> recipients, "errorType" -> "recipientsIssue", "errorValue" -> ex.toString)))
          None
        }
      }
    }).filter(_.isDefined).map(_.get)
  }
  
  def findMessageBodies(json: JValue): Option[Map[String,String]] = {
    findBodies1(json) match {
      case Some(b) => Some(b)
      case None => findBodies2(json) match {
        case Some(b2) => Some(b2)
        case None => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "noMessageBody", new DateTime().getMillis, Map("json" -> pretty(render(json))))); None
      }
    }
  }
  
  def findBodies1(json: JValue): Option[Map[String,String]] = {
    (for {
      payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
      body <- payload.asInstanceOf[Map[String,Any]].get("body")
    } yield(body, payload)) match {
    case Some(m) => {
      (for {
        parts <- m._2.asInstanceOf[Map[String,Any]].get("parts")
        textMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/plain"))
        text <- textMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
        htmlMap <- parts.asInstanceOf[List[Map[String,Any]]].find(part => part.get("mimeType") == Some("text/html"))
        html <- htmlMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
      } yield(text, html)) match {
        case Some(b) => Some(Map("text" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._1)), "html" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._2))))
        case None => None
      }
    }
    case None => None
    }
  }
      
  def findBodies2(json: JValue): Option[Map[String,String]] = {
    (for {
      payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
      parts1 <- payload.asInstanceOf[Map[String,Any]].get("parts")
      parts2Option = parts1.asInstanceOf[List[Map[String,Any]]].find(_.get("parts").isDefined).flatMap(_.get("parts"))
      parts2 <- parts2Option
    } yield(parts2.asInstanceOf[List[Map[String,Any]]])) match {
      case Some(np) => {
        (for {
          textMap <- np.find(_.get("mimeType") == Some("text/plain"))
          text <- textMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
          htmlMap <- np.find(_.get("mimeType") == Some("text/html"))
          html <- htmlMap.get("body").get.asInstanceOf[Map[String,Any]].get("data").map(_.toString)
        } yield(text, html)) match {
          case Some(b) => Some(Map("textBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._1)), "htmlBody" -> StringUtils.newStringUtf8(Base64.decodeBase64(b._2))))
          case None => None
        }
      }
      case None => None
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