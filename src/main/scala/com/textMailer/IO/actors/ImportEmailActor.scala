package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.UserEventIO
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm
import scala.util.control.Breaks.breakable
import org.joda.time.DateTime
import com.textMailer.IO._
import java.util.UUID
import com.datastax.driver.core.utils.UUIDs
import java.util.Date
import com.datastax.driver.core.BoundStatement
import java.util.Properties
import javax.mail.internet.InternetAddress
import javax.mail.UIDFolder.LASTUID
import com.textMailer.models.Conversation
import java.security._
import java.math.BigInteger
import com.textMailer.models.Email
import com.sun.mail.gimap.GmailMessage
import com.sun.mail.imap.IMAPMessage
import com.sun.mail.gimap.GmailFolder
import com.sun.mail.gimap.GmailSSLStore
import com.textMailer.models.Topic
import com.textMailer.Implicits.ImplicitConversions._
import scala.collection.immutable.TreeSet
import scala.collection.immutable.SortedSet
import com.textMailer.models.UserEvent

object ImportEmailActor {
  case class ImportEmail(userId: Option[String])
  case class RecurringEmailImport
}

class ImportEmailActor extends Actor {
  import com.textMailer.IO.actors.ImportEmailActor._
  // TODO: make this actor into it's own service, that consumes a user id from a queue
  def receive = {
    case "recurringImport" => { // TODO: add unit test??
      val fake_uuid = java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422") // used as signup for all users - need better way to do this
      
      (for {
        userId <- UserEventIO().find(List(Eq("user_id", fake_uuid), Eq("event_type", "userSignup")), 1000).map(ue => ue.data.get("userId")).filter(_.isDefined).map(_.get)
        emailAccount <- EmailAccountIO().find(List(Eq("user_id",userId)), 10)
      } yield(emailAccount)).map(ea => {
        importGmail(ea.userId, ea.username, ea.accessToken)
      })
    }
    case ImportEmail(userId) => { // TODO: Add time as param, so can continually get latest, unchecked emails??
      val emailAccounts = userId match {
        case Some(userId) => {
          EmailAccountIO().find(List(Eq("user_id",userId)), 10).map(ea => {
            ea.provider match {
              case "gmail" => {
                importGmail(ea.userId, ea.username, ea.accessToken)
              }
              case _ =>
            }
          })
        }
        case None => None
      }
      sender ! emailAccounts // why am i sending this back?
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
  
  def importGmail(userId: String, emailAddress: String, accessToken: String): Unit = {
    val props = new Properties();
    props.put("mail.store.protocol", "gimaps");
    props.put("mail.imap.sasl.enable", "true");
   
    props.put("mail.gimaps.sasl.enable", "true");
    props.put("mail.gimaps.sasl.mechanisms", "XOAUTH2");

    props.put("mail.imap.auth.login.disable", "true");
    props.put("mail.imap.auth.plain.disable", "true");

    val session = Session.getInstance(props)

    val store: GmailSSLStore = session.getStore("gimaps").asInstanceOf[GmailSSLStore]
    println(s"####### before connect")
    store.connect("imap.googlemail.com", emailAddress, accessToken) //TODO: make this a try

   // get different folders??
    val folder: GmailFolder = store.getFolder("INBOX").asInstanceOf[GmailFolder]

    val currentDateTime = new DateTime
    val lastEmailUid = (for {
      ue <- UserEventIO().find(List(Eq("user_id", java.util.UUID.fromString(userId)), Eq("event_type", "importEmail")), 1).headOption
      uid <- ue.data.get("uid")
    } yield uid.toLong).getOrElse(14900l) // change this to something reasonable. how far back to we want to go for first time users?
   
    println(s"################### lastEmailUid $lastEmailUid")

    folder.open(Folder.READ_WRITE);
    val messages = folder.getMessagesByUID(lastEmailUid , LASTUID).toSeq
    val newLastUID = folder.getUID(messages(messages.size - 1).asInstanceOf[GmailMessage])
    println(s"@@@@@@@@@@@@@ newLastUID $newLastUID")
    
    newLastUID match {
      case newUID if newUID == lastEmailUid => println(s"NO NEW MESSAGES FOR userId: $userId / email: $emailAddress")
      case _ => writeMessages(messages, folder, userId, emailAddress)
    }

    folder.close(false)
    store.close()
    UserEventIO().write(UserEvent(java.util.UUID.fromString(userId), "importEmail", currentDateTime.getMillis, Map("uid" -> newLastUID.toString)))
  }
  
  def md5Hash(str: String) = {
    val md = MessageDigest.getInstance("MD5")
    
    md.reset()
    md.update(str.getBytes());
    val digest = md.digest()
    val bigInt = new BigInteger(1,digest)
    bigInt.toString(16)
  }
  
  def writeMessages(messages: Seq[javax.mail.Message], folder: GmailFolder, userId: String, emailAddress: String): Unit = {
    messages.map(m => {
      val gm = m.asInstanceOf[GmailMessage]
      val gmId = gm.getMsgId()
      val uid = folder.getUID(gm)
      println(s"########## uid $uid")
      val body = getText(m)
      val text = (for{
        textValue <- body.get("text")
        text <- textValue
      } yield(text)) match {
        case Some(t) => t.toString
        case None => "no text"
      }
      
      val html = (for{
        htmlValue <- body.get("html")
        html <- htmlValue
      } yield(html)) match {
        case Some(h) => h.toString.split("""<div class="gmail_extra">""").toList.head.replace("\n", " ").replace("\r", " ")
        case None => ""
      }
      
//            println(s"<<<<<<<<<<<< text $text")
//            println(s"<<<<<<<<<<<< html $html")

      val emailId = UUIDs.random
      
      val threadId = gm.getThrId()

      val sender = m.getFrom() match {
        case null => ""
        case Array() => ""
        case a => InternetAddress.parse(a(0).toString)(0).getAddress
      }
//            println(s"################### sender $sender")
      
      // TODO: should to cc and bcc be Sets?
      
      val to = m.getRecipients(Message.RecipientType.TO) match {
        case null => Set()
        case Array() => Set()
        case to => InternetAddress.parse(to(0).toString)(0).getAddress.split(",").toSet
      }

      val cc = m.getRecipients(Message.RecipientType.CC) match {
        case null => Set()
        case Array() => Set()
        case cc => InternetAddress.parse(cc(0).toString)(0).getAddress.split(",").toSet
      }

      val bcc = m.getRecipients(Message.RecipientType.BCC) match {
        case null => Set()
        case Array() => Set()
        case bcc => InternetAddress.parse(bcc(0).toString)(0).getAddress.split(",").toSet // fucking retarded
      }

//            println(s"################### to ${to}")
//            println(s"################### cc ${cc}")
//            println(s"################### bcc $bcc")

      val subject = m.getSubject() match {
        case null => ""
        case x: String => x
      }
      println(s"!!!!!!!!!!!! subject: $subject")
      
      val recipients = TreeSet[String]() ++ to ++ bcc ++ cc - emailAddress + sender
      println(s"@@@@@@@@@@@ recipientsSet $recipients")
      val recipientsString = recipients.toString
      println(s"@@@@@@@@@@@ recipientsString $recipientsString")
      val recipientsHash = md5Hash(recipientsString)
      println(s"############## hashText $recipientsHash")
      val ts = m.getSentDate().getMillis
      println(s"############## timestamp $ts \n\n\n")
      val conversation = Conversation(userId, recipientsHash, recipients, ts)
      ConversationIO().write(conversation)
      OrdConversationIO().write(conversation)
      val topic = Topic(userId, recipientsHash, threadId, subject, ts)
      TopicIO().write(topic)
      OrdTopicIO().write(topic)
      val email = Email(gmId, userId, threadId, recipientsHash, ts, subject, sender, "cc", "bcc", text, html)
      EmailIO().write(email)
    })
  }
  
  def getText(m: Message): Map[String, Option[Object]] = {
    println(s"%%%%%%%%%%%%%%%%%%%%%%%%%%% NEW MESSAGE")
    val contentObject = m.getContent()
    if(contentObject.isInstanceOf[Multipart]) {
      val content: Multipart = contentObject.asInstanceOf[Multipart];
      val count = content.getCount() - 1;
      
      var html: Option[Object] = None
      var text: Option[Object] = None
      
      breakable {for(i <- 0 to count) {
        val part = content.getBodyPart(i);
        if(part.isMimeType("text/plain")) {
          text = Some(part.getContent)
        }
        else if(part.isMimeType("text/html"))
        {
          html = Some(part.getContent)
        }
      }}
      Map("html" -> html, "text" -> text)
    } else Map()
  }
}

