package com.textMailer.routes

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.util.Timeout
import org.scalatra._
import scalate.ScalateSupport
import com.textMailer.TextmailerStack
import java.net.URL
import java.net.URLEncoder._
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import scala.util.Success
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.stackmob.newman.response.HttpResponse
import org.scalatra.util.RicherString
import java.net.URLEncoder
import com.textMailer.oAuth.tokens.AccessTokenActor
import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.DefaultFormats._
import org.json4s.Formats._
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq
import scala.concurrent.ExecutionContext
import org.scalatra.{Accepted, AsyncResult, FutureSupport, ScalatraServlet}
import com.textMailer.IO.actors.ImportEmailActor._
import scala.util.Failure

class ImportEmailRoutes (system: ActorSystem, importEmailActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(100000) 

  get("/:userId") {
    val userId = params.get("userId")
    importEmailActor ! ImportEmail(userId)

    "importing emails"
  }
}