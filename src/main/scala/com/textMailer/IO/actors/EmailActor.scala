package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq
import scala.concurrent.Future
import com.textMailer.models.Email
import com.textMailer.IO.SendEmail
import com.textMailer.IO.EmailAccountIO

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
      val emailAccount = EmailAccountIO().asyncFind(List(Eq("id",emailAccountId)), 1)
      val result = for {
        ea <- emailAccount
      } yield {
        ea.headOption match {
          case Some(acc) => SendEmail.send(email, acc.username, acc.accessToken)
          case None => println(s"No emailAccount found")
        } 
      }
      
      result pipeTo sender
    }

    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

