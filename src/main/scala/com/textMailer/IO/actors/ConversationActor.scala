package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq
import com.textMailer.IO.Gt
import com.textMailer.IO.CassandraClause
import com.textMailer.IO.OrdConversationIO
import com.textMailer.IO.Lt

object ConversationActor {
  case class GetConversationsByUser(userId: String)
  case class GetOrderedConversationsByUser(userId: String, time: Option[Long])
}

class ConversationActor extends Actor {
  import com.textMailer.IO.actors.ConversationActor._
  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.pattern.pipe

  def receive = {
    case GetConversationsByUser(userId) => {
      val conversations = ConversationIO().asyncFind(List(Eq("user_id",userId)), 100)
      conversations pipeTo sender
    }
    case GetOrderedConversationsByUser(userId, time) => {
      val clauses: List[CassandraClause] = time match {
        case Some(t) => Lt("ts", t) :: List(Eq("user_id",userId))
        case None => List(Eq("user_id",userId))
      }

      (for {
        convsersations <- OrdConversationIO().asyncFind(clauses, 100)
      } yield {
        convsersations.map(c => (c.recipientsHash, c)).toMap.map(_._2) // TODO: write a test
      }) pipeTo sender
    }
    case _ => sender ! "Error: Didn't match case in ConversationActor"
  }
}

