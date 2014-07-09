package com.textMailer.routes

import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import com.textMailer.IO.ConversationIO


class ConversationRoutes extends ScalatraServlet with JacksonJsonSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  
  before() {
    contentType = formats("json")
  }
  
  get("/") { 
    val convos = ConversationIO().find(20)
    println(s"@@@@@@@@@@@@@@@@@@@@@@@ $convos")
    convos
  }
}