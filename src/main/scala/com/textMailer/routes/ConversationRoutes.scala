package com.textMailer.routes

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.util.Timeout
import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import com.textMailer.IO.ConversationIO
import scala.concurrent.ExecutionContext
import org.scalatra.{Accepted, AsyncResult, FutureSupport, ScalatraServlet}
import com.textMailer.IO.actors.ConversationActor._
import com.textMailer.Implicits.ImplicitConversions._

class ConversationRoutes(system: ActorSystem, conversationActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10000)
  
  before() {
    contentType = formats("json")
    response.setHeader("cache-control", "max-age=0, no-cache")
  }
  
  get("/:userId") {
    val userId = params.getOrElse("userId", "no userId")
    val conversations = conversationActor ? GetConversationsByUser(userId)
    new AsyncResult { val is = conversations }
  }
  
  get("/ordered/:userId") {
    val userId = params.getOrElse("userId", "no userId")
    val ts = params.get("ts")
    val conversations = conversationActor ? GetOrderedConversationsByUser(userId, ts)
    new AsyncResult { val is = conversations }
  }
}