package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
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
import com.textMailer.models.Conversation
import java.security._
import java.math.BigInteger
import com.textMailer.models.Email
import com.sun.mail.gimap.GmailMessage
import com.sun.mail.imap.IMAPMessage
import com.sun.mail.gimap.GmailFolder
import com.sun.mail.gimap.GmailSSLStore

object ImportEmailActor {
  case class ImportEmail(userId: Option[String])  
}

class ImportEmailActor extends Actor {
  import com.textMailer.IO.actors.ImportEmailActor._
  // TODO: differentiate between first import and routine imports. Routine imports should go by date, rather than int range?
  def receive = {
    case ImportEmail(userId) => {
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
      sender ! emailAccounts
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

    val folder: GmailFolder = store.getFolder("INBOX").asInstanceOf[GmailFolder]
    println(s"####### before date")
    val date = (new DateTime).minusDays(1).toDate()
    val olderThan = new ReceivedDateTerm(ComparisonTerm.GT, date);


//          if(!folder.isOpen())
          folder.open(Folder.READ_WRITE);
          val messages = folder.search(olderThan)
          // folder.getMessages()
          var a = 0;
          
          val md = MessageDigest.getInstance("MD5")

          messages.map(m => {
            val gm = m.asInstanceOf[GmailMessage]
            val textOpt = getText(m)
            val emailId = UUIDs.random
            
            val thriftId = gm.getThrId()
            println(s"@@@@@@@@@@@@@@@@ thriftId $thriftId")

            val sender = m.getFrom() match {
              case null => ""
              case Array() => ""
              case a => InternetAddress.parse(a(0).toString)(0).getAddress
            }
            println(s"################### sender $sender")
            
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

            println(s"################### to ${to}")
            println(s"################### cc ${cc}")
            println(s"################### bcc $bcc")

            val subject = m.getSubject() match {
              case null => "no_subject"
              case x: String => x
            }
            println(s"!!!!!!!!!!!! subject: $subject")
            println(s"<<<<<<<<<<<< text $textOpt")
            val text = textOpt match {
              case Some(t) => t.toString
              case None => "no body"
            }
            
            val recipients = to ++ bcc ++ cc - emailAddress + sender
            println(s"@@@@@@@@@@@ recipientsSet $recipients")
            val recipientsString = recipients.toString
            println(s"@@@@@@@@@@@ recipientsString $recipientsString")
            md.reset()
            md.update(recipientsString.getBytes());
            val digest = md.digest()
            val bigInt = new BigInteger(1,digest)
            val recipientsHash = bigInt.toString(16)
            println(s"############## hashText $recipientsHash")
            
            val conversation = Conversation(userId, recipientsHash, recipients)
            ConversationIO().write(conversation)
            val email = Email(UUIDs.random.toString, userId, subject, recipientsHash, "time", "cc", "bcc", text)
            EmailIO().write(email)
            
//            name.replaceAll("[^\\p{L}\\p{Nd}]", "").replaceAll(" ", "").toLowerCase
            
//            val statement = s"INSERT INTO simplex.imported_emails (user_id, subject, recipients_string, time, recipients, cc, bcc, body) VALUES (uuid, subject, recipients_string, n, recipients, cc, bcc, text);"

//           println(s"######### statement $statement")
//            client.session.execute(statement);
//            println(s"<<<<<<<<<<<< subject ${m.getSubject()}")
//            println(s"<<<<<<<<<<<< body ${m.getContent()}")
            a += 1;
//            println(s"<<<<<<<<<<< count $a")
          })
  }
  
  def getText(m: Message): Option[Object] = {
    println(s"%%%%%%%%%%%%%%%%%%%%%%%%%%% NEW MESSAGE")
    val contentObject = m.getContent()
    if(contentObject.isInstanceOf[Multipart]) {
      val content: Multipart = contentObject.asInstanceOf[Multipart];
      val count = content.getCount() - 1;
      
      breakable {for(i <- 0 to count) {
        val part = content.getBodyPart(i);
        if(part.isMimeType("text/plain")) {
          return Some(part.getContent)
        }
        else if(part.isMimeType("text/html"))
        {
          return Some(part.getContent)
        }
      }}
    }
    None
  }
}

