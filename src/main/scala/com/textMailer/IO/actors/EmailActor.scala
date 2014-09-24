package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq
import scala.concurrent.Future
import com.textMailer.models.Email
import com.textMailer.IO.SendEmail
import com.textMailer.IO.EmailAccountIO
import scala.util.Success
import scala.util.Failure

object EmailActor {
  case class GetEmailsForTopic(userId: Option[String], threadId: Option[String])
  case class SendMail(email: Email, emailAccountId: Option[String])
}

class EmailActor extends Actor {
  import com.textMailer.IO.actors.EmailActor._
  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.pattern.pipe

  def receive = {
    case GetEmailsForTopic(userId, threadId) => {
      val emails =  (for {
        uid <- userId
        tid <- threadId
      } yield(uid, tid)) match {
        case Some(ids) => EmailIO().asyncFind(List(Eq("user_id", ids._1), Eq("thread_id", ids._2.toLong)), 40)
        case None => Future(List())
      }

      emails pipeTo sender
    }

    case SendMail(email, emailAccountId) => {
      val result = EmailAccountIO().asyncFind(List(Eq("id",emailAccountId.getOrElse("none")), Eq("user_id",email.userId)), 1).map(ea => ea.headOption match {
        case Some(acc) => SendEmail.send(email, acc.username, acc.accessToken) // TODO: return TRY
        case None => // TODO: return TRY - failure due to no email account
      })
      
      result pipeTo sender
    }

    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

