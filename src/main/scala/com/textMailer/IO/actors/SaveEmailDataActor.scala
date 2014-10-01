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

object SaveEmailDataActor {
  case class SaveData(userId: String, to: Set[String], cc: Set[String], bcc: Set[String], emailAddress: String, sender: String, subject: String, ts: Long, threadId: Long,
    gmId: Long, emailAccountId: String, body: Map[String, Option[Object]]
  )
  case class GetGmailMessage(messageIds: List[String], gmailUserId: String, accessToken: String)
}

class SaveEmailDataActor extends Actor {
  import com.textMailer.IO.actors.SaveEmailDataActor._
  implicit val httpClient = new ApacheHttpClient

  def receive = {
    case GetGmailMessage(messageIds, gmailUserId, accessToken) => {
      println(s"======================== getting to saveemaildata child actor")
      val batchBody = messageIds.foldLeft("")((acc, id) => acc + createBatchPart(gmailUserId, id)) + "--batch_narmalbatch--"
      val batchUrl = new URL("https://www.googleapis.com/batch")
      val batchReq = POST(batchUrl).addHeaders(("authorization", s"Bearer $accessToken"), ("Content-Type", "multipart/mixed; boundary=batch_narmalbatch")).addBody(batchBody)
      val batchRes = batchReq.apply.map(r => {
        val messageBodies = (r.toJValue.values.asInstanceOf[Map[String,String]].get("body") match {
          case Some(b) => findJsonObjects(b).map(json => {
            val metaData = Try{findMessageMetaData(json)} match {
              case Success(s) => s.map({case (a,b) => b match {
                case Some(d) => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "metaDataIssue", new DateTime().getMillis, Map("json" -> pretty(render(json))))); None
                case None => println(s"")
              }})
//                s match {
//                case Some(m) => // m.map(d => println(s"====================== , $d"))
//                case None => println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! NOTHING THERE ${pretty(render(json))}")
//              }
              case Failure(ex) => println(s"!!!!!!!!!!!!!!!!!!!!!! ex $ex")
            }
//            println(s"############## metaData ${}");
            getMessageBodies(json) match {
              case Some(m) => Some(m)
              case None => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "noBody", new DateTime().getMillis, Map("json" -> pretty(render(json))))); None
            }
          })
          case None => List()
        })
        // iterate and write messages
//        println(s"############# messageBodies $messageBodies")
      })
    }
    case _ => sender ! "didn't match in SavEmailDataActor"
  }
  
  def createBatchPart(gmailUserId: String, messageId: String): String = s"--batch_narmalbatch\nContent-Type: application/http\n\nGET /gmail/v1/users/$gmailUserId/messages/$messageId\n\n"
  
  def findMessageMetaData(json: JValue) = {
    val payload = for {
      payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
    } yield payload
    
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
      case Some(s) => Some(s)
      case None => {
        (for {
          headers <- headers
          toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Delivered-To"))
          to <- toOpt.get("value")
        } yield to)
      }
    }
//    println(s"############## to $to")
    
    val from = for {
      headers <- headers
      fromOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("From"))
      from <- fromOpt.get("value")
    } yield from
//    println(s"############## from $from")
    
    val threadId = for {
      threadId <- json.values.asInstanceOf[Map[String,Any]].get("threadId")
    } yield threadId
//    println(s"############## threadId $threadId")
    
    val messageId = for {
      messageId <- json.values.asInstanceOf[Map[String,Any]].get("id")
    } yield messageId
//    println(s"############## messageId $messageId")
    
    val cc = for {
      headers <- headers
      ccOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Cc"))
      cc <- ccOpt.get("value")
    } yield cc
//    println(s"############## cc $cc")
    
    val time = (for {
      headers <- headers
      timeOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("Received"))
      time <- timeOpt.get("value")
    } yield time) match {
      case Some(s) => Some(s)
      case None => {
        (for {
          headers <- headers
          toOpt <- headers.asInstanceOf[List[Map[String,String]]].find(_.get("name") == Some("X-Received"))
          to <- toOpt.get("value")
        } yield to)
      }
    }
    println(s"############## time $time")
 
    Map("subject" -> subject, "to" -> to, "from" -> from, "threadId" -> threadId, "messageId" -> messageId, "cc" -> cc, "time" -> time)
  }
  
  @tailrec private def findJsonObjects(str: String, json: List[JValue] = List(), currentIndex: Int = 0, startingIndex: Int = 0, openingBrackets: Int = 0, closingBrackets: Int = 0): List[JValue] = {
    str.slice(currentIndex, currentIndex + 1) match {
      case "{" if openingBrackets == 0 => findJsonObjects(str, json, currentIndex + 1, currentIndex, 1, 0)
      case "{" if openingBrackets > 0 => findJsonObjects(str, json, currentIndex + 1, startingIndex, openingBrackets + 1, closingBrackets)
      case "}" if closingBrackets < (openingBrackets - 1) => findJsonObjects(str, json, currentIndex + 1, startingIndex, openingBrackets, closingBrackets + 1)
      case "}" if closingBrackets == (openingBrackets - 1) => findJsonObjects(str, JsonParser.parse(str.slice(startingIndex, currentIndex + 1)) :: json, currentIndex + 1)
      case x if currentIndex == str.length => json
      case x  => findJsonObjects(str, json, currentIndex + 1, startingIndex, openingBrackets, closingBrackets)
    }
  }
  
  def getMessageBodies(json: JValue): Option[Map[String,String]] = {
    getBodies1(json) match {
      case Some(b) => Some(b)
      case None => getBodies2(json)
    }
  }
  
  def getBodies1(json: JValue): Option[Map[String,String]] = {
    (for {
      payload <- json.values.asInstanceOf[Map[String,Any]].get("payload")
      body <- payload.asInstanceOf[Map[String,Any]].get("body")
//          headers <- payload.asInstanceOf[Map[String,Any]].get("headers")
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
      
  def getBodies2(json: JValue): Option[Map[String,String]] = {
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