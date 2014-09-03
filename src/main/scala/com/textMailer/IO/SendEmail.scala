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

object SendEmailIO {
  def send(email: Email): Unit = {
//    connectToSmtp("smtp.gmail.com", 587, "sportano@gmail.com", "ya29.dQAjrTVHXpUPJCIAAADQ471bD9ol295op76DzbujyPGprSVIL6bDoTw0yn7PPb-iHPEkkUNCbbhphY2qFQw")
    val props = new Properties();
//    props.put("mail.smtp.auth", "true");
//    props.put("mail.smtp.starttls.enable", "true");
//    props.put("mail.smtp.host", "smtp.gmail.com");
//    props.put("mail.smtp.port", "587");
    
//    props.put("mail.smtp.ssl.enable", "true"); // required for Gmail
//    props.put("mail.smtp.sasl.enable", "true");
//    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
//    props.put("mail.smtp.auth.login.disable", "true");
//    props.put("mail.smtp.auth.plain.disable", "true");
    val oauthToken = "ya29.dQAEpiEXWQisqyIAAABJcWe726uYa7_FaNDA7ojPp8iF8xrCEr9RtKHmLzrZCiWOS2KqljERAnVaxCvcMf0"
    
    props.put("mail.smtp.sasl.enable", "true");
   
    props.put("mail.smtp.sasl.enable", "true");
    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");

    props.put("mail.smtp.auth.login.disable", "true");
    props.put("mail.smtp.auth.plain.disable", "true");
    props.put("mail.smtp.port", "587"); //587 465
    props.put("mail.smtp.starttls.enable", "true");
    val session = Session.getInstance(props);
    session.setDebug(true);
    val transport = new SMTPTransport(session, null);
    transport.connect("smtp.gmail.com", "sportano@gmail.com", oauthToken) //ssl://
    
    val message: MimeMessage = new MimeMessage(session);
    message.setFrom(new InternetAddress("sportano@gmail.com"));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress("sportano@gmail.com"));
    message.setSubject("This is the Subject Line!");
    message.setText("This is actual message");
    
//    transport.issueCommand("AUTH XOAUTH2 " + oauthToken, 235);
    transport.sendMessage(message, message.getAllRecipients());
    transport.close();
  }
  
  def connectToSmtp(host: String, port: Int, userEmail: String, oauthToken: String): SMTPTransport = {
    val props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

//    props.put("mail.smtp.starttls.enable", "true");
//    props.put("mail.smtp.starttls.required", "true");
//    props.put("mail.smtp.sasl.enable", "false");
    
    val session = Session.getInstance(props) 
    session.setDebug(true);

    val unusedUrlName: URLName = null;
    val transport = new SMTPTransport(session, unusedUrlName);
    // If the password is non-null, SMTP tries to do AUTH LOGIN.
    val emptyPassword: String = null;
    transport.connect(host, port, userEmail, emptyPassword);

    val response = String.format("user=%s\1auth=Bearer %s\1\1", userEmail, oauthToken).getBytes();
    val textResponse = BASE64EncoderStream.encode(response);
    println(s"########### textResponse $textResponse")

    transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);

    return transport;
  }
}