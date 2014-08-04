package com.textMailer.IO.actors

import akka.actor.{ActorRef, Actor, ActorSystem}
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.Eq
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.datastax.driver.core.utils.UUIDs
import scala.util.Success
import scala.util.Failure

object UserActor {
  case class CreateUser(firstName: String, lastName: String, password: String)  
}

class UserActor extends Actor {
  import com.textMailer.IO.actors.UserActor._

  def receive = {
    case CreateUser(firstName, lastName, password) => {
      val user = UserIO().write(User(UUIDs.random.toString, firstName, lastName, password)) match {
        case Success(u) => Some(u)
        case Failure(ex) => None
      }
      sender ! user
    }
    case _ => sender ! "Error: Didn't match case in ConversationActor"
  }
}

