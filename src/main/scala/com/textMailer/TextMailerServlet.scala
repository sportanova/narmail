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
  client.connect("127.0.0.1"); // 54.183.164.178       127.0.0.1    // eip 54.183.66.201
  client.setKeyspace("app")
  client.createSchema();
//      client.close();
}
