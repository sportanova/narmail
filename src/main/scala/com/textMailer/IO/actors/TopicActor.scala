package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq
import com.textMailer.IO.TopicIO
import com.textMailer.IO.CassandraClause
import com.textMailer.IO.Lt
import com.textMailer.IO.OrdTopicIO
import com.textMailer.IO.QueryIO.mostRecent

object TopicActor {
  case class GetTopicsByConversation(userId: String, recipientsHash: String)
  case class GetOrderedTopicsByConversation(userId: String, recipientsHash: String, time: Option[Long])
}

class TopicActor extends Actor {
  import com.textMailer.IO.actors.TopicActor._
  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.pattern.pipe

  def receive = {
    case GetTopicsByConversation(userId, recipientsHash) => {
      val topics = TopicIO().asyncFind(List(Eq("user_id", userId), Eq("recipients_hash", recipientsHash)), 100)
      topics pipeTo sender
    }
    case GetOrderedTopicsByConversation(userId, recipientsHash, time) => {
      val clauses: List[CassandraClause] = (time match {
        case Some(t) => Lt("ts", t) :: List(Eq("user_id", userId))
        case None => List(Eq("user_id",userId))
      }) ++ List(Eq("recipients_hash", recipientsHash))

      (for {
        topics <- OrdTopicIO().asyncFind(clauses, 30)
      } yield mostRecent(topics)) pipeTo sender  // TODO: write a test
    }
    case _ => sender ! "Error: Didn't match case in TopicsActor"
  }
}

