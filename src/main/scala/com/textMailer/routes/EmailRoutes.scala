package com.textMailer.routes

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.util.Timeout
import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq
import scala.concurrent.ExecutionContext
import org.scalatra.{Accepted, AsyncResult, FutureSupport, ScalatraServlet}
import com.textMailer.IO.actors.EmailActor._
import com.textMailer.models.Email

class EmailRoutes(system: ActorSystem, emailActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats.withDouble
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(1000)
  
  before() {
    contentType = formats("json")
  }
  
  get("/:userId/:threadId") {
    val userId = params.get("userId")
    val threadId = params.get("threadId")

    val emails = emailActor ? GetEmailsForTopic(userId, threadId)
    new AsyncResult { val is = emails }
  }
  
  post("/:emailAccountId") {
    val email = parsedBody.extract[Email]
    println(s"################ email $email")
    val emailAccountId = params.get("emailAccountId")

//    val result = emailActor ? SendMail(email, emailAccountId) // TODO: return Try, rather than unit
//    new AsyncResult { val is = result }
  }
}