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
import scala.util.Failure
import com.textMailer.IO.actors.UserActor.CreateUser
import com.textMailer.models.User
import scala.util.Try
import net.liftweb.json.JNothing
import net.liftweb.json.JsonAST.JObject

class UserRoutes (system: ActorSystem, userActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher
  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10000)

  post("/") {
    val userInfo = parsedBody.values match {
      case x: JObject =>  x.asInstanceOf[Map[String,String]]
      case JNothing => Map("" -> "")
      case x: Map[String,String] => x.asInstanceOf[Map[String,String]]
      case _ => Map("" -> "")
    }

    val firstName = userInfo.getOrElse("firstName", "")
    val lastName = userInfo.getOrElse("lastName", "")
    val password = userInfo.getOrElse("password", "")

    val newUser = userActor ? CreateUser(firstName, lastName, password)
    new AsyncResult { val is = newUser }
  }
}