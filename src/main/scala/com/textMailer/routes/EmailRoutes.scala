package com.textMailer.routes

import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq


class EmailRoutes extends ScalatraServlet with JacksonJsonSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  
  before() {
    contentType = formats("json")
  }
  
  get("/") {
    val userId = params.getOrElse("userId", "no userId")
    val subject = params.getOrElse("subject", "no subject")
    val recipients = params.getOrElse("recipients", "recipients")
    println(s"########## subject $subject")
    println(s"########## recipients $recipients")
//    val emails = EmailIO().find(List(Eq("user_id","somethingelse")), 100)
//    println(s"@@@@@@@@@ userId $emails")
//    emails
  }
}