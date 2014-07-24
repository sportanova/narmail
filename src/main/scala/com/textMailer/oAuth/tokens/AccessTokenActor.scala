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
import java.net.URL
import net.liftweb.json.JNothing
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonAST.JString
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonParser

object AccessTokenActor {
  case class RefreshGmailAccessToken(userId: String)  
}

class AccessTokenActor extends Actor {
  import com.textMailer.oAuth.tokens.AccessTokenActor._
  implicit val httpClient = new ApacheHttpClient
  

  def receive = {
    case RefreshGmailAccessToken(userId) => {
      val accessToken = (for {
        u <- UserIO().find(List(Eq("id",userId)), 1).headOption
        token <- refreshAccessToken(u.refreshToken)
      } yield(token)) match {
        case Some(at) => at
        case None => "Failed to refresh AccessToken"
      }

      sender ! accessToken
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
  
  def refreshAccessToken(refreshToken: String): Option[String] = {
    val refreshURL = new URL("https://accounts.google.com/o/oauth2/token")
    val req = POST(refreshURL).addHeaders(("Content-Type", "application/x-www-form-urlencoded"))
      .addBody(s"client_id=${URLEncoder.encode("909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com", "UTF-8")}&client_secret=${URLEncoder.encode("qaCfjCbleg8GpHVeZXljeXT0", "UTF-8")}&grant_type=refresh_token&refresh_token=$refreshToken")

    val json = Await.result(req.apply, 10.second).toJValue
    getAccessTokenFromJson(json)
  }
  
  def getAccessTokenFromJson(json: JValue): Option[String] = {
    json.values.asInstanceOf[Map[String,Any]].get("body") match {
      case Some(js) => JsonParser.parse(js.toString).values.asInstanceOf[Map[String,String]].get("access_token")
      case None => None
    }
  }
}

