package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq

object ConversationActor {
  case class GetConversationsByUser(userId: String)  
}

class ConversationActor extends Actor {
  import com.textMailer.IO.actors.ConversationActor._

  def receive = {
    case GetConversationsByUser(userId) => {
      val conversations = ConversationIO().find(List(Eq("user_id",userId)), 100)
      sender ! conversations
    }
    case _ => sender ! "Error: Didn't match case in ConversationActor"
  }
}

