package com.textMailer.routes

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.util.Timeout
import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import scala.concurrent.ExecutionContext
import org.scalatra.{Accepted, AsyncResult, FutureSupport, ScalatraServlet}
import com.textMailer.IO.actors.TopicActor._


class TopicRoutes(system: ActorSystem, topicActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10000)
  
  before() {
    contentType = formats("json")
  }
  
  get("/:userId/:recipientsHash") {
    val userId = params.getOrElse("userId", "no userId")
    val recipientsHash = params.getOrElse("recipientsHash", "no recipientsHash")
    val topics = topicActor ? GetTopicsByConversation(userId, recipientsHash)
    new AsyncResult { val is = topics }
  }
}