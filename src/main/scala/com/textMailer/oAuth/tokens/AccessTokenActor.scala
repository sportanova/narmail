package com.textMailer.oAuth.tokens

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.UserIO
import com.textMailer.IO.Eq
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.stackmob.newman.response.HttpResponse
import org.scalatra.util.RicherString
import java.net.URLEncoder

object AccessTokenActor {
  case class RefreshGmailAccessToken(userId: String)  
}

class AccessTokenActor extends Actor {
  import com.textMailer.oAuth.tokens.AccessTokenActor._

  def receive = {
    case RefreshGmailAccessToken(userId) => {
      val user = UserIO().find(List(Eq("",userId)), 1)
      sender ! user
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

