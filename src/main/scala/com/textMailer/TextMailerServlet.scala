package com.textMailer

import org.scalatra._
import scalate.ScalateSupport
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

class TextMailerServlet extends TextmailerStack {
  val client = SimpleClient();
      client.connect("127.0.0.1");
      
      client.createSchema();
//      client.close();
  
  def doShit(): Unit = {
//    ya29.IwDdT4p21h1eFB8AAADfh6GfMGDUnpBxm5SouEStbHcoYtUQZerfRGd8CXAx7w
   val props = new Properties();
   props.put("mail.store.protocol", "imaps");
   props.put("mail.imap.ssl.enable", "true"); // required for Gmail
//   props.put("mail.imaps.port", "587");
   props.put("mail.imap.sasl.enable", "true");
	 props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
	 props.put("mail.imap.auth.login.disable", "true");
	 props.put("mail.imap.auth.plain.disable", "true");

    val session = Session.getInstance(props)

    val store = session.getStore("imap")
    println(s"####### before connect")
    store.connect("imap.googlemail.com","sportano@gmail.com", "ya29.OADcpn6EkcaM7SAAAAA0Q-ZC3UXyWL3qKqiVGcaGLbnmMHX-4pyq_pyoD9Bm3g");
  println(s"####### before folder")
  val folder = store.getFolder("Inbox"); // This doesn't work for other email account
  println(s"####### before date")
  val date = (new DateTime).minusDays(1).toDate()
  val olderThan = new ReceivedDateTerm(ComparisonTerm.GT, date);
  
  println(s"<<<<<<<<<< working")


//          if(!folder.isOpen())
          folder.open(Folder.READ_WRITE);
          val messages = folder.search(olderThan)
          // folder.getMessages()
          var a = 0;
          val userId = UUIDs.timeBased

          messages.map(m => {
            val textOpt = getText(m)
            val emailId = UUIDs.timeBased

            val sender = m.getFrom() match {
              case null => "no_sender"
              case Array() => "no_sender"
              case a => InternetAddress.parse(a(0).toString)(0).getAddress // fucking retarded
            }
            println(s"################### sender $sender")

            val subject = m.getSubject() match {
              case null => "no_subject"
              case x: String => x
            }
            println(s"<<<<<<<<<<<< text $textOpt")
            val text = textOpt match {
              case Some(t) => t.toString
              case None => "no body"
            }
            
//            name.replaceAll("[^\\p{L}\\p{Nd}]", "").replaceAll(" ", "").toLowerCase
            
            val statement = client.session.prepare(
      "INSERT INTO app.emails_by_conversation " +
      "(id, user_id, subject, recipients_string, time, recipients, cc, bcc, body) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            
      val boundStatement = new BoundStatement(statement);
      
      client.session.execute(boundStatement.bind(
      emailId.toString,
      userId.toString,
      subject,
      "recipients_string",
      new Date().toString,
      "recipients_set",
      "cc",
      "bcc",
      text) );
      
      val statementConv = client.session.prepare(
      "INSERT INTO app.conversations_by_user " +
      "(user_id, subject, recipients_string_hash, recipients_string) " +
      "VALUES (?, ?, ?, ?);");
            
      val boundStatementConv = new BoundStatement(statementConv);
      
      client.session.execute(boundStatementConv.bind(
      userId.toString,
      subject,
      "recipients_string_hash",
      "recipients_string"));
            
//            val statement = s"INSERT INTO simplex.imported_emails (user_id, subject, recipients_string, time, recipients, cc, bcc, body) VALUES (uuid, subject, recipients_string, n, recipients, cc, bcc, text);"

//           println(s"######### statement $statement")
//            client.session.execute(statement);
//            println(s"<<<<<<<<<<<< subject ${m.getSubject()}")
//            println(s"<<<<<<<<<<<< body ${m.getContent()}")
            a += 1;
//            println(s"<<<<<<<<<<< count $a")
          })
  }
  
  doShit()
//  client.close();

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
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
