package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq
import com.textMailer.IO.Gt
import com.textMailer.IO.CassandraClause
import com.textMailer.IO.OrdConversationIO
import com.textMailer.IO.Lt
import com.textMailer.models.Model
import com.textMailer.models.Conversation

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
      val conversations = ConversationIO().asyncFind(List(Eq("user_id",userId)), 20)
      conversations pipeTo sender
    }
    case GetOrderedConversationsByUser(userId, time) => {
      val clauses: List[CassandraClause] = time match {
        case Some(t) => Lt("ts", t) :: List(Eq("user_id",userId))
        case None => List(Eq("user_id",userId))
      }

      (for {
        conversations <- OrdConversationIO().asyncFind(clauses, 20)
      } yield mostRecent("ts", conversations)) pipeTo sender  // TODO: write a test
    }
    case _ => sender ! "Error: Didn't match case in ConversationActor"
  }
  
  def mostRecent(prop: String, models: List[Conversation]) = { // if there are more than one models with the same property, take the one with the most recent timestamp
    models.groupBy(_.recipientsHash).map(tuple => tuple match {
      case t if t._2.size > 1 => t._2.reduceLeft((conv1, conv2) => if(conv1.ts > conv2.ts) conv1 else conv2) 
      case t => t._2.head
    }).toSeq.sortBy(item => item.ts).reverse
  }
}

