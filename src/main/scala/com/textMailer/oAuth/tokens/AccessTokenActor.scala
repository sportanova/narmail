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
import com.datastax.driver.core.utils.UUIDs
import com.textMailer.models.EmailAccount
import com.textMailer.IO.EmailAccountIO
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object AccessTokenActor {
  case class RefreshGmailAccessTokens(userId: String)
  case class AddGmailAccount(userId: Option[String], accessCode: Option[String])  
}

class AccessTokenActor extends Actor {
  import com.textMailer.oAuth.tokens.AccessTokenActor._
  implicit val httpClient = new ApacheHttpClient
  

  def receive = {
    // TODO: will spammers be able to POST /user endpoint and create users, unless we create user in first oAuth transaction? TLDR: Create user via endpoint or when adding first account
    case AddGmailAccount(userId, accessCode) => {
      val newAccount = (for {
        id <- userId
        ac <- accessCode
        tokens <- getGmailAccessToken(ac)
        at <- tokens.get("accessToken")
        rt <- tokens.get("refreshToken")
        email <- getGmailAddress(at)
      } yield (Map("userId" -> id, "accessToken" -> at, "email" -> email, "refreshToken" -> rt))) match {
        // TODO: Actually test this
        case Some(userInfo) => EmailAccountIO().write(EmailAccount(userInfo.get("userId").get, UUIDs.random().toString, "gmail", userInfo.get("email").get, userInfo.get("accessToken").get, userInfo.get("refreshToken").get))
        case None => Failure(new Throwable("Couldn't create account: Missing id || ac || tokens || at || rt || email"))
      }
      
      sender ! newAccount
    }
    case RefreshGmailAccessTokens(userId) => {
      val refreshedAccounts = (for {
        // TODO: MAKE THIS NOT GMAIL SPECIFIC. UPDATE ALL ACCOUNTS
        ea <- EmailAccountIO().find(List(Eq("user_id",userId)), 10)
        refreshedEA <- Some(refreshGmailAccessToken(ea))
      } yield(refreshedEA))
      sender ! refreshedAccounts.map(acc => {
        acc match {
          case Success(a) => s"Successfully updated ${a.id} email account"
          case Failure(ex) => s"Failed to update account for user $userId"
        }
      })
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
  
  def refreshGmailAccessToken(emailAccount: EmailAccount): Try[EmailAccount] = {
    val refreshURL = new URL("https://accounts.google.com/o/oauth2/token")
    val req = POST(refreshURL).addHeaders(("Content-Type", "application/x-www-form-urlencoded"))
      .addBody(s"client_id=${URLEncoder.encode("909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com", "UTF-8")}&client_secret=${URLEncoder.encode("qaCfjCbleg8GpHVeZXljeXT0", "UTF-8")}&grant_type=refresh_token&refresh_token=${emailAccount.refreshToken}")

    Try{Await.result(req.apply, 10.second).toJValue} match {
      case Success(json) => {
        getValueFromJson(json, "body", "access_token") match {
          case Some(at) => EmailAccountIO().write(emailAccount.copy(accessToken = at))
          case None => Failure(new Throwable("couldn't get accessToken value from json"))
        }
      }
      case Failure(ex) => Failure(ex)
    }
  }

  def getValueFromJson(json: JValue, firstField: String, secondField: String): Option[String] = {
    json.values.asInstanceOf[Map[String,Any]].get(firstField) match {
      case Some(js) => JsonParser.parse(js.toString).values.asInstanceOf[Map[String,String]].get(secondField)
      case None => None
    }
  }
  
  def getGmailAccessToken(reqTok: String): Option[Map[String,String]] = {
    val redirectURL = "http://localhost:8080/oauth/oauth2callback"
    val oauthURL = new URL("https://accounts.google.com/o/oauth2/token")
    val req = POST(oauthURL).addHeaders(("Content-Type", "application/x-www-form-urlencoded")).addBody(s"code=${URLEncoder.encode(reqTok, "UTF-8")}&redirect_uri=${URLEncoder.encode(redirectURL, "UTF-8")}&client_id=${URLEncoder.encode("909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com", "UTF-8")}&scope=&client_secret=${URLEncoder.encode("qaCfjCbleg8GpHVeZXljeXT0", "UTF-8")}&grant_type=${URLEncoder.encode("authorization_code", "UTF-8")}")
    val json = Await.result(req.apply, 10.second).toJValue

    for {
      body <- json.values.asInstanceOf[Map[String,Any]].get("body")
      innerJSON <- Some(JsonParser.parse(body.toString).values.asInstanceOf[Map[String,String]])
      at <- innerJSON.get("access_token")
      rt <- innerJSON.get("refresh_token")
    } yield(Map("accessToken" -> at, "refreshToken" -> rt))
  }
  
  def getGmailAddress(accessToken: String): Option[String] = {
    val url = new URL("https://www.googleapis.com/userinfo/email?alt=json")
    val req = GET(url).addHeaders(("authorization", s"Bearer $accessToken"))
    val res = Await.result(req.apply, 10.second).toJValue

    for {
      body <- res.values.asInstanceOf[Map[String,Any]].get("body")
      data <- JsonParser.parse(body.toString).values.asInstanceOf[Map[String,Any]].get("data")
      email <- data.asInstanceOf[Map[String,String]].get("email")
    } yield(email)
  }
}