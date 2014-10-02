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

object SendEmail {
  def send(email: Email, from: String, oAuthToken: String): Unit = {
//    val connectionData = connectToSmtp("smtp.gmail.com", 587, "sportano@gmail.com", oAuthToken)
//    val transport = connectionData._1
//    val session = connectionData._2
//    
//    val message: MimeMessage = new MimeMessage(session);
//    message.setFrom(new InternetAddress(from));
//    email.recipients match {
//      case Some(r) => {
//        r.foreach(r => message.addRecipient(Message.RecipientType.TO, new InternetAddress(r)))
//        EmailTopicIO().write(email) // TODO: figure out what to do with new messages - how do we get the threadid
//
//        message.setSubject(email.subject);
//        message.setText(email.textBody);
//
//        transport.sendMessage(message, message.getAllRecipients());
//      }
//      case None => println(s"NO RECIPIENTS: NOT SENDING EMAIL")
//    }
//
//    transport.close();
  }
  
  def connectToSmtp(host: String, port: Int, from: String, oAuthToken: String): (SMTPTransport, Session) = {
     val props = new Properties();
    
    props.put("mail.smtp.sasl.enable", "true");
   
    props.put("mail.smtp.sasl.enable", "true");
    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");

    props.put("mail.smtp.auth.login.disable", "true");
    props.put("mail.smtp.auth.plain.disable", "true");
    props.put("mail.smtp.port", "587"); //587 465
    props.put("mail.smtp.starttls.enable", "true");
    val session = Session.getInstance(props);
    session.setDebug(true); // TODO: setup env variable so this isn't enabled in prod
    val transport = new SMTPTransport(session, null);
    transport.connect("smtp.gmail.com", from, oAuthToken) //ssl://
    
    (transport, session)
  }
}