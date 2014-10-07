package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.UserEventIO
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm
import scala.util.control.Breaks.breakable
import org.joda.time.DateTime
import com.textMailer.IO._
import java.util.UUID
import com.datastax.driver.core.utils.UUIDs
import java.util.Date
import com.datastax.driver.core.BoundStatement
import java.util.Properties
import javax.mail.internet.InternetAddress
import javax.mail.UIDFolder.LASTUID
import com.textMailer.models.Conversation
import java.security._
import java.math.BigInteger
import com.textMailer.models.Email
import com.sun.mail.gimap.GmailMessage
import com.sun.mail.imap.IMAPMessage
import com.sun.mail.gimap.GmailFolder
import com.sun.mail.gimap.GmailSSLStore
import com.textMailer.models.Topic
import com.textMailer.Implicits.ImplicitConversions._
import scala.collection.immutable.TreeSet
import scala.collection.immutable.SortedSet
import com.textMailer.models.UserEvent
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import akka.actor.Props
import javax.mail.FetchProfile
import javax.mail.search.FlagTerm
import javax.mail.Flags
import javax.mail.Flags.Flag
import javax.mail.Part
import com.stackmob.newman.{ETagAwareHttpClient, ApacheHttpClient}
import com.stackmob.newman._
import com.stackmob.newman.caching.InMemoryHttpResponseCacher
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.HttpResponse
import java.net.URL
import net.liftweb.json.JsonParser
import com.textMailer.IO.actors.GetEmailActor.GetGmailMessages

object ImportEmailActor {
  case class ImportEmail(userId: Option[String])
  case class RecurringEmailImport
}

class ImportEmailActor extends Actor { // TODO: make this actor into it's own service, that consumes a user id from a queue
  import com.textMailer.IO.actors.ImportEmailActor._
  implicit val httpClient = new ApacheHttpClient
  implicit val timeout = Timeout(8000)
  val getEmailActor = context.actorOf(Props[GetEmailActor], "GetEmailActor")

  def receive = {
    case "recurringImport" => { // TODO: add unit test??
      val fake_uuid = java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422") // used as signup for all users - need better way to do this - not how events_table schema works
      
      (for {
        userId <- UserEventIO().find(List(Eq("user_id", fake_uuid), Eq("event_type", "userSignup")), 1000).map(ue => ue.data.get("userId")).filter(_.isDefined).map(_.get)
        emailAccount <- EmailAccountIO().find(List(Eq("user_id",userId)), 10)
      } yield(emailAccount)).map(ea => importGmailHTTP("100030981325891290860", ea.username, ea.accessToken, ea.id, ea.userId, 40))
    }
    case ImportEmail(userId) => {
      val emailAccounts = userId match {
        case Some(userId) => {
          EmailAccountIO().find(List(Eq("user_id",userId)), 10).map(ea => {
            ea.provider match {
              case "gmail" => importGmailHTTP("100030981325891290860", ea.username, ea.accessToken, ea.id, ea.userId, 100)
              case _ =>
            }
         })
        }
        case None => None
      }
      sender ! emailAccounts // why am i sending this back?
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
  
  def importGmailHTTP(gmailUserId: String, emailAddress: String, accessToken: String, emailAccountId: String, userId: String, count: Int): Unit = { // if number of new messages is > count, will miss those messages
    val lastImportedEmailIdFuture = UserEventIO().asyncFind(List(Eq("user_id", java.util.UUID.fromString(userId)), Eq("event_type", "importEmail")), 1)

    val messageListUrl = new URL(s"https://www.googleapis.com/gmail/v1/users/$gmailUserId/messages?maxResults=$count")
    val messageListReq = GET(messageListUrl).addHeaders(("authorization", s"Bearer $accessToken"))
    val messageListRes = messageListReq.apply.map(res => {
      JsonParser.parse(res.toJValue.values.asInstanceOf[Map[String,Any]].get("body").get.toString).values.asInstanceOf[Map[String,Any]].get("messages") match {
        case Some(ms) => {
          lastImportedEmailIdFuture.map(events => { // unwrap future
            val messageIds = ((for {
              e <- events.headOption
              gmId <- e.data.get("gmailId")
            } yield gmId)) match {
              case Some(id) => { // recurring import
                val unwrittenMessageIds = ms.asInstanceOf[List[Map[String,String]]].takeWhile(messageInfo => messageInfo.get("id") != Some(id)).map(mInfo => mInfo.get("id")).filter(_.isDefined).map(_.get) // take elements until one equals the last imported email's id
                
                getEmailActor ! GetGmailMessages(unwrittenMessageIds, gmailUserId, accessToken, emailAddress, userId, emailAccountId) // filter the http response into a list of gmail message ids
                println(s"############## unwrittenMessages.size ${unwrittenMessageIds.size}")
                println(s"############## unwrittenMessages ${unwrittenMessageIds}")
                println(s"############## totalMessages ${ms.asInstanceOf[List[Map[String,String]]]}")
                println(s"############## totalMessages.size ${ms.asInstanceOf[List[Map[String,String]]].size}")
                
                unwrittenMessageIds.headOption match { // only write event if there's an unwritten message
                  case Some(id) => UserEventIO().write(UserEvent(java.util.UUID.fromString(userId), "importEmail", new DateTime().getMillis, Map("gmailId" -> id)))
                  case None => // no unwritten messages
                }
              }
              case None => { //first time import
                val ids = ms.asInstanceOf[List[Map[String,String]]].map(mInfo => mInfo.get("id")).filter(_.isDefined).map(_.get)
                println(s"@@@@@@@@@@ ids $ids")
                getEmailActor ! GetGmailMessages(ids, gmailUserId, accessToken, emailAddress, userId, emailAccountId)
                
                ids.headOption match {
                  case Some(id) => // UserEventIO().write(UserEvent(java.util.UUID.fromString(userId), "importEmail", new DateTime().getMillis, Map("gmailId" -> id)))
                  case None =>
                }
              }
            }
          })
        }
        case None => println(s"############ didn't find any messageids for userId:$userId (access token probably expired)")
      }
    })
  }
}