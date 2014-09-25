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
      
      // get futures started // TODO: move these into ImportEmailActor, get started earlier?
      val topicExists = TopicIO().asyncFind(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100).map(topic => {topic.headOption})
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

//      val topic = Topic(userId, recipientsHash, threadId, subject, ts)
//      TopicIO().asyncWrite(topic)
//      OrdTopicIO().asyncWrite(topic)
      val email = Email(gmId, userId, threadId, recipientsHash, Some(recipients), ts, subject, sender, cc.toString, bcc.toString, textBody, htmlBody)
      EmailTopicIO().asyncWrite(email)
      EmailConversationIO().asyncWrite(email)
      
      for {
        te <- topicExists
        tc <- topicCount
        etc <- emailsByTopicCount
        ecc <- emailsByConversationCount
      } {
        println(s"\n\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! emailsByTopicCount $etc")
        println(s"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! emailsByConversationCount $ecc")
        val trueTopicCount = te match {
          case Some(t) => tc
          case None => tc + 1l
        }
      }

      val conversation = Conversation(userId, recipientsHash, recipients, ts, emailAccountId) // do this last, give time to get topic count
      ConversationIO().asyncWrite(conversation)
      OrdConversationIO().asyncWrite(conversation)
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