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
import com.textMailer.oAuth.tokens.AccessTokenActor._
import scala.util.Failure

class OAuthRoutes (system: ActorSystem, accessTokenActor: ActorRef) extends ScalatraServlet with JacksonJsonSupport with FutureSupport with MethodOverride {
  implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10000) 
  // https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com&access_type=offline&redirect_uri=http://localhost:8080/oauth/oauth2callback&state=a109ae1e-d9c5-4fc9-9c7d-33d7907dd63f&scope=https://mail.google.com/ email
  // refreshToken = 1/roJI5cuO89mcZgj1e3N67kAxmSA1IBf5KEYZM7voWOo

  put("/accessToken/:userId") {
    val userId = params.getOrElse("userId", "no userId")
    val refreshToken = accessTokenActor ? RefreshGmailAccessTokens(userId)

    new AsyncResult { val is = refreshToken }
  }
  
  get("/oauth2callback") {
    val userId = params.get("state")
    val accessCode = params.get("code")
    val newUser = accessTokenActor ? AddGmailAccount(userId, accessCode)
    new AsyncResult { val is = newUser }
  }
}