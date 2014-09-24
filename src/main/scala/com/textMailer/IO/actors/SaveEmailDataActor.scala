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

object SaveEmailDataActor {
  case class SaveData(userId: String, to: Set[String], cc: Set[String], bcc: Set[String], emailAddress: String, sender: String, subject: String, ts: Long, threadId: Long,
    gmId: Long, emailAccountId: String, textBody: String, htmlBody: String
  )
}

class SaveEmailDataActor extends Actor {
  import com.textMailer.IO.actors.SaveEmailDataActor._
  def receive = {
    case SaveData(userId, to, cc, bcc, emailAddress, sender, subject, ts, threadId, gmId, emailAccountId, textBody, htmlBody) => {
      val recipients: TreeSet[String] = TreeSet[String]() ++ to.map(_.toString) ++ bcc.map(_.toString) ++ cc.map(_.toString) - emailAddress + sender
      println(s"@@@@@@@@@@@ recipientsSet $recipients")
      val recipientsString = recipients.toString
      println(s"@@@@@@@@@@@ recipientsString $recipientsString")
      val recipientsHash = md5Hash(recipientsString)
      println(s"############## hashText $recipientsHash")
      println(s"############## timestamp $ts \n\n\n")
      println(s"!!!!!!!!!!!! subject: $subject")
      println(s"!!!!!!!!!!!! threadId: $threadId")
      val topic = Topic(userId, recipientsHash, threadId, subject, ts)
      TopicIO().asyncWrite(topic)
      OrdTopicIO().asyncWrite(topic)
      val email = Email(gmId, userId, threadId, recipientsHash, Some(recipients), ts, subject, sender, "cc", "bcc", textBody, htmlBody)
      EmailIO().asyncWrite(email)
//      val topicCount = TopicIO().asyncCount(List(Eq("user_id",email.userId), Eq("recipients_hash", recipientsHash)), 100).map(c => )
      val emailCount = EmailIO().asyncCount(List(Eq("user_id",email.userId), Eq("recipients_hash", recipientsHash)), 100).map(c => c)
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