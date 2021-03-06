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
import scala.util.Try
import scala.util.Success
import scala.util.Failure

class EmailRoutes(system: ActorSystem, emailActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats.withDouble
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10000)
  
  before() {
    contentType = formats("json")
  }
  
  get("/:userId/:threadId") {
    val userId = params.get("userId")
    val threadId = params.get("threadId")
    val ts = params.get("ts")

    val emails = emailActor ? GetEmailsForTopic(userId, threadId, ts)
    new AsyncResult { val is = emails }
  }
  
  post("/send/:emailAccountId") {
    Try{parsedBody.extract[Email]} match {
      case Success(email) => {
        params.get("emailAccountId") match {
          case Some(eaId) => {
            val result = emailActor ! SendMail(email, eaId) // TODO: return Try, rather than unit
          }
          case None => // no email account id provided
        }
      }
      case Failure(ex) => println(s"@@@@@@@ email not properly formed $ex")// TODO - return message about email not properly formed
    }
  }
}