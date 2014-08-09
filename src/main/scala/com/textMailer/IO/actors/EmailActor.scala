package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO
import com.textMailer.IO.Eq

object EmailActor {
  case class GetEmailsForConversation(userId: String, recipients: String, subject: String)  
}

class EmailActor extends Actor {
  import com.textMailer.IO.actors.EmailActor._

  def receive = {
    case GetEmailsForConversation(userId, recipients, subject) => {
      val emails = EmailIO().find(List(Eq("user_id", userId), Eq("recipients_hash", recipients), Eq("subject", subject)), 40)
      println(s"@@@@@@@@@@ emails $emails")
      sender ! emails
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

