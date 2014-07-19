package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.EmailIO

object ImportGmailActor {
  case class GetEmailsForConversation(userId: String, recipients: String, subject: String)  
}

class ImportGmailActor extends Actor {
  import com.textMailer.IO.actors.EmailActor._

  def receive = {
    case GetEmailsForConversation(userId, recipients, subject) => {
      val emails = EmailIO().find(List(), 1)
      sender ! emails
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

