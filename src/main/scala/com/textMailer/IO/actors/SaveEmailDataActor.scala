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

object SaveEmailDataActor {
  case class SaveData(userId: String, to: Set[String], cc: Set[String], bcc: Set[String], emailAddress: String, sender: String, subject: String, ts: Long, threadId: Long,
    gmId: Long, emailAccountId: String, body: Map[String, Option[Object]]
  )
}

class SaveEmailDataActor extends Actor {
  import com.textMailer.IO.actors.SaveEmailDataActor._
  def receive = {
    case SaveData(userId, to, cc, bcc, emailAddress, sender, subject, ts, threadId, gmId, emailAccountId, body) => {
      val recipients: TreeSet[String] = TreeSet[String]() ++ to ++ bcc ++ cc - emailAddress + sender
      println(s"@@@@@@@@@@@ recipientsSet $recipients")
      val recipientsString = recipients.toString
      val recipientsHash = md5Hash(recipientsString)
      
      val topicExists = TopicIO().asyncFind(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100).map(topic => {topic.headOption}) // get futures started => => =>
      val topicCount = TopicIO().asyncCount(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100)
      val emailsByTopicCount = EmailTopicIO().asyncCount(List(Eq("user_id", userId), Eq("thread_id", threadId)), 100).map(c => c + 1l)
      val emailsByConversationCount = EmailConversationIO().asyncCount(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100).map(c => c + 1l)
      
      val textBody = body.get("text") match {
        case Some(t) => t.toString
        case None => ""
      }
      
      val htmlBody = body.get("html") match {
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
  
  def md5Hash(str: String) = {
    val md = MessageDigest.getInstance("MD5")
    md.reset()
    md.update(str.getBytes());
    val digest = md.digest()
    val bigInt = new BigInteger(1,digest)
    bigInt.toString(16)
  }
}