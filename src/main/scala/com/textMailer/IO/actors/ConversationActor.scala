package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO

object ConversationActor {
  case class GetConversationsForUsers(userId: String)  
}

class ConversationActor extends Actor {
  import com.textMailer.IO.actors.ConversationActor._

  def receive = {
    case GetConversationsForUsers(userId) => {
      val conversations = ConversationIO().find(List(), 100)
      sender ! conversations
    }
    case _ => sender ! "Error: Didn't match case in EmailActor"
  }
}

