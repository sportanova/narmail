package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq
import scala.concurrent.Future

object EmailActor {
  case class GetEmailsForTopic(userId: Option[String], threadId: Option[String])  
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
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

