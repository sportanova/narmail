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
  // https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com&access_type=offline&redirect_uri=http://localhost:8080/oauth/oauth2callback&state=bbe1131d-3be5-4997-a1ee-295f6f2c9dbf&scope=https://mail.google.com/ email
  // https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com&access_type=offline&redirect_uri=http://ec2-54-183-167-43.us-west-1.compute.amazonaws.com:8080/oauth/oauth2callback&state=3144e5d3-7f6d-44ac-8541-c051ba35364e&scope=https://mail.google.com/ email
  // 3144e5d3-7f6d-44ac-8541-c051ba35364e
  // refreshToken = 1/roJI5cuO89mcZgj1e3N67kAxmSA1IBf5KEYZM7voWOo
  //https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com&access_type=offline&redirect_uri=http://localhost:8080/oauth/oauth2callback&state=test_account_id&scope=https://mail.google.com/ email

  put("/accessToken/:userId") {
    val userId = params.getOrElse("userId", "no userId")
    val refreshToken = accessTokenActor ? RefreshGmailAccessTokens(userId)

    new AsyncResult { val is = refreshToken }
  }
  
  get("/oauth2callback") {
    (for {
      userId <- params.get("state")
      accessCode <- params.get("code")
    } yield(userId, accessCode)) match {
      case Some(ids) => {
        val newUser = accessTokenActor ? AddGmailAccount(ids._1, ids._2)
        new AsyncResult { val is = newUser }
      }
      case None => new AsyncResult { val is = Future{"didn't provide userid or accesscode"} }
    }
  }
}