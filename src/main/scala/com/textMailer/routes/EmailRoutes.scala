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


class EmailRoutes(system: ActorSystem, emailActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10)
  
  before() {
    contentType = formats("json")
  }
  
  get("/") {
//     new AsyncResult { val is = emailActor ? GetEmailsForConversation("","","") }
    new AsyncResult { val is = emailActor ? "Do stuff and give me an answer" }
//    val futureStuff = emailActor ? "Do stuff and give me an answer"
//    for {
//      stuff <- emailActor ? "Do stuff and give me an answer"
//    } yield {
//      println(s"@@@@@@@@@@ stuff $stuff")
//      new AsyncResult {stuff}
//    }
//    val userId = params.getOrElse("userId", "no userId")
//    val subject = params.getOrElse("subject", "no subject")
//    val recipients = params.getOrElse("recipients", "recipients")
//    println(s"########## subject $subject")
//    println(s"########## recipients $recipients")
//    val emails = EmailIO().find(List(Eq("user_id","somethingelse")), 100)
//    println(s"@@@@@@@@@ userId $emails")
//    emails
  }
}