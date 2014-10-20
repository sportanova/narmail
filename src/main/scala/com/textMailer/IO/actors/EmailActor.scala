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
import com.textMailer.IO.EmailTopicIO
import com.textMailer.IO.CassandraClause
import com.textMailer.IO.Lt

object EmailActor {
  case class GetEmailsForTopic(userId: Option[String], threadId: Option[String], ts: Option[String])
  case class SendMail(email: Email, emailAccountId: String)
}

class EmailActor extends Actor {
  import com.textMailer.IO.actors.EmailActor._
  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.pattern.pipe

  def receive = {
    case GetEmailsForTopic(userId, threadId, ts) => {
      val emails =  (for {
        uid <- userId
        tid <- threadId
      } yield(uid, tid)) match {
        case Some(ids) => {
          val clauses: List[CassandraClause] = ((ts match {
            case Some(t) => Some(Lt("ts", t))
            case None => None
          }) :: Some(Eq("user_id",ids._1)) :: Some(Eq("thread_id", ids._2)) :: Nil).filter(_.isDefined).map(_.get)

          EmailTopicIO().asyncFind(clauses, 40)
        }
        case None => Future(List())
      }

      emails pipeTo sender
    }

    case SendMail(email, emailAccountId) => {
      val result = EmailAccountIO().asyncFind(List(Eq("id",emailAccountId), Eq("user_id",email.userId)), 1).map(ea => ea.headOption match {
        case Some(acc) => SendEmail.send(email, acc.id, acc.accessToken) // TODO: return TRY
        case None => // TODO: return TRY - failure due to no email account
      })
      
      result pipeTo sender
    }

    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

