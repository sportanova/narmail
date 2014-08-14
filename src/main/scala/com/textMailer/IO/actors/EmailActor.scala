package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq

object EmailActor {
  case class GetEmailsForTopic(userId: Option[String], threadId: Option[String])  
}

class EmailActor extends Actor {
  import com.textMailer.IO.actors.EmailActor._

  def receive = {
    case GetEmailsForTopic(userId, threadId) => {
      val emails =  (for {
        uid <- userId
        tid <- threadId
      } yield(uid, tid)) match {
        case Some(ids) => EmailIO().find(List(Eq("user_id", ids._1), Eq("thread_id", ids._2.toLong)), 40)
        case None => List()
      }

      println(s"@@@@@@@@@@ emails $emails")
      sender ! emails
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

