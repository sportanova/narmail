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
import akka.actor.Props
import com.textMailer.IO.actors.SaveEmailActor._

object GetEmailActor {
  case class GetGmailMessages(messageIds: List[String], gmailUserId: String, accessToken: String, emailAddress: String, userId: String, emailAccountId: String)
}

class GetEmailActor extends Actor {
  import com.textMailer.IO.actors.GetEmailActor._
  implicit val httpClient = new ApacheHttpClient
  val saveEmailActor = context.actorOf(Props[SaveEmailActor], "SaveEmailActor")

  def receive = {
    case GetGmailMessages(messageIds, gmailUserId, accessToken, emailAddress, userId, emailAccountId) => {
      println(s"======================== getting to getEmailActor child actor")
      val batchBody = messageIds.foldLeft("")((acc, id) => acc + createBatchPart(gmailUserId, id)) + "--batch_narmalbatch--"
      val batchUrl = new URL("https://www.googleapis.com/batch")
      val batchReq = POST(batchUrl).addHeaders(("authorization", s"Bearer $accessToken"), ("Content-Type", "multipart/mixed; boundary=batch_narmalbatch")).addBody(batchBody)
      val batchRes = batchReq.apply.map(r => {
        val messageBodies = (r.toJValue.values.asInstanceOf[Map[String,String]].get("body") match {
          case Some(b) => findJsonObjects(b).map(json => saveEmailActor ! SaveGmailMessage(json, emailAddress, userId, emailAccountId))
          case None => List()
        })
      })
    }
    case _ => sender ! "didn't match in GetEmailDataActor"
  }
  
  def createBatchPart(gmailUserId: String, messageId: String): String = s"--batch_narmalbatch\nContent-Type: application/http\n\nGET /gmail/v1/users/$gmailUserId/messages/$messageId\n\n"
  
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
}