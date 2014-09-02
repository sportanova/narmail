package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq
import com.textMailer.IO.Gt
import com.textMailer.IO.CassandraClause
import com.textMailer.IO.OrdConversationIO
import com.textMailer.IO.Lt

object ScheduledEmailActor {
}

class ScheduledEmailActor extends Actor {
  import com.textMailer.IO.actors.ConversationActor._
  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.pattern.pipe

  def receive = {
    case "foo" => {
      println(s"!!!!!!!!!!!!!!!!!!!!")
      
    }
    case _ => sender ! "Error: Didn't match case in ConversationActor"
  }
}

