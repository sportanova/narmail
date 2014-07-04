package com.textMailer.services

import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import com.textMailer.IO.ConversationsIO


class ConversationsService extends ScalatraServlet with JacksonJsonSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  
  before() {
    contentType = formats("json")
  }

  case class hello(hello: String)
  
  get("/") {
    
    val convos = ConversationsIO().find(20)
    println(s"@@@@@@@@@@@@@@@@@@@@@@@ $convos")
    convos
  }
}