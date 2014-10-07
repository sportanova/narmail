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
import com.textMailer.IO.UserEventIO
import com.textMailer.models.UserEvent
import org.joda.time.DateTime
import akka.pattern.pipe

object AccessTokenActor {
  case class RefreshGmailAccessTokens(userId: String)
  case class AddGmailAccount(userId: String, accessCode: String)  
}

class AccessTokenActor extends Actor {
  import com.textMailer.oAuth.tokens.AccessTokenActor._
  implicit val httpClient = new ApacheHttpClient
  
  val gmailOauthRedirect = System.getProperty("gmail_oauth_redirect") match {
    case redirect: String => redirect
    case null => "http://localhost:8080/oauth/oauth2callback"
  }

  def receive = {
    case AddGmailAccount(userId, accessCode) => {  // TODO: will spammers be able to POST /user endpoint and create users, unless we create user in first oAuth transaction? TLDR: Create user via endpoint or when adding first account
      val newAccount = getGmailAccessToken(accessCode).map(tokens => {
        tokens match {
          case Some(ts) => {
            getUserGmailInfo(ts.get("accessToken").get, ts.get("idToken").get).map(gmailInfo => {
              gmailInfo match {
                case Some(info) => EmailAccountIO().write(EmailAccount(userId, info.get("gmailUserId").get, "gmail", info.get("email").get, ts.get("accessToken").get, ts.get("refreshToken").get))
                case None => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> s"userId:$userId", "errorType" -> "gmailUserInfo")))
              }
            })
          }
          case None => UserEventIO().asyncWrite(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> s"userId:$userId", "errorType" -> "getAccessToken")))
        }
      })
      
      newAccount pipeTo sender
    }
    case "recurringRefresh" => {
      val fake_uuid = java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422") // used as signup for all users - need better way to do this
      
      val emailAccountsFutures = UserEventIO().asyncFind(List(Eq("user_id", fake_uuid), Eq("event_type", "userSignup")), 1000).flatMap(ues => { // grab ids from user events, then get email accounts
        Future.sequence(ues.map(_.data.get("userId")).filter(_.isDefined).map(_.get).map(id => {
          EmailAccountIO().asyncFind(List(Eq("user_id",id)), 10)
        }))
      })
      
      for {
        emailAccounts <- emailAccountsFutures
        updatedEAs <- Future.sequence(emailAccounts.flatMap(x => x).map(refreshGmailAccessToken(_)))
      } yield {
        updatedEAs.map(acc => {
          acc match {
            case Success(s) => UserEventIO().write(UserEvent(java.util.UUID.fromString(s.userId), "refreshToken", new DateTime().getMillis, Map()))
            case Failure(ex) => UserEventIO().write(UserEvent(java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422"), "error", new DateTime().getMillis, Map("value" -> s"$ex","errorType" -> "tokenRefreshFailure")))
          }
        })
      }
    }

    case RefreshGmailAccessTokens(userId) => { // TODO: no idea if this works. haven't used it in a while
      val emailAccounts = EmailAccountIO().asyncFind(List(Eq("user_id",userId)), 10)

      (for {
        eas <- emailAccounts
        refreshedEAs <- Future.sequence(eas.map(ea => refreshGmailAccessToken(ea)))
      } yield {
        refreshedEAs.map(acc => { acc match {
          case Success(a) => s"Successfully updated ${a.id} email account"
          case Failure(ex) => s"Failed to update account for user: $userId . Reason: $ex"
        }})
      }) pipeTo sender
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
  
  def refreshGmailAccessToken(emailAccount: EmailAccount): Future[Try[EmailAccount]] = {
    val refreshURL = new URL("https://accounts.google.com/o/oauth2/token")
    val req = POST(refreshURL).addHeaders(("Content-Type", "application/x-www-form-urlencoded"))
      .addBody(s"client_id=${URLEncoder.encode("909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com", "UTF-8")}&client_secret=${URLEncoder.encode("qaCfjCbleg8GpHVeZXljeXT0", "UTF-8")}&grant_type=refresh_token&refresh_token=${emailAccount.refreshToken}")

    req.apply.map(res => {
      getValueFromJson(res.toJValue, "body", "access_token") match {
        case Some(at) => EmailAccountIO().write(emailAccount.copy(accessToken = at))
        case None => Failure(new Throwable("couldn't get accessToken value from json"))
      }
    })
  }

  def getValueFromJson(json: JValue, firstField: String, secondField: String): Option[String] = {
    json.values.asInstanceOf[Map[String,Any]].get(firstField) match {
      case Some(js) => JsonParser.parse(js.toString).values.asInstanceOf[Map[String,String]].get(secondField)
      case None => None
    }
  }
  
  def getGmailAccessToken(reqTok: String): Future[Option[Map[String,String]]] = {
    val oauthURL = new URL("https://accounts.google.com/o/oauth2/token")
    val req = POST(oauthURL).addHeaders(("Content-Type", "application/x-www-form-urlencoded")).addBody(s"code=${URLEncoder.encode(reqTok, "UTF-8")}&redirect_uri=${URLEncoder.encode(gmailOauthRedirect, "UTF-8")}&client_id=${URLEncoder.encode("909952895511-tnpddhu4dc0ju1ufbevtrp9qt2b4s8d6.apps.googleusercontent.com", "UTF-8")}&scope=&client_secret=${URLEncoder.encode("qaCfjCbleg8GpHVeZXljeXT0", "UTF-8")}&grant_type=${URLEncoder.encode("authorization_code", "UTF-8")}")
    req.apply.map(json => {
      println(s"############## json $json")

      for {
        body <- json.toJValue.values.asInstanceOf[Map[String,Any]].get("body")
        innerJSON <- Some(JsonParser.parse(body.toString).values.asInstanceOf[Map[String,String]])
        at <- innerJSON.get("access_token")
        rt <- innerJSON.get("refresh_token")
        idT <- innerJSON.get("id_token")
      } yield(Map("accessToken" -> at, "refreshToken" -> rt, "idToken" -> idT))
    })
  }

  def getUserGmailInfo(accessToken: String, idToken: String): Future[Option[Map[String,String]]] = {
    val url = new URL(s"https://www.googleapis.com/oauth2/v1/tokeninfo?id_token=$idToken")
    val req = GET(url).addHeaders(("authorization", s"Bearer $accessToken"))
    req.apply.map(json => {
      println(s"@@@@@@@@@@ user info $json")

      for {
        body <- json.toJValue.values.asInstanceOf[Map[String,Any]].get("body")
        parsedBody <- Some(JsonParser.parse(body.toString).values.asInstanceOf[Map[String,Any]])
        email <- parsedBody.get("email")
        userId <- parsedBody.get("user_id")
      } yield (Map("gmailUserId" -> userId.toString, "email" -> email.toString))
    })
  }
}