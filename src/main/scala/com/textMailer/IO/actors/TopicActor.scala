package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq
import com.textMailer.IO.TopicIO

object TopicActor {
  case class GetTopicsByConversation(userId: String, recipientsHash: String)  
}

class TopicActor extends Actor {
  import com.textMailer.IO.actors.TopicActor._

  def receive = {
    case GetTopicsByConversation(userId, recipientsHash) => {
      val topics = TopicIO().find(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100)
      sender ! topics
    }
    case _ => sender ! "Error: Didn't match case in TopicsActor"
  }
}

