package specs.routes

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.textMailer.IO.Eq
import akka.actor.Props
import com.textMailer.routes.ConversationRoutes
import com.textMailer.IO.actors.ConversationActor
import com.textMailer.models.Conversation
import com.textMailer.IO.ConversationIO
import com.textMailer.IO.actors.TopicActor
import com.textMailer.IO.TopicIO
import com.textMailer.models.Topic
import com.textMailer.routes.TopicRoutes

class TopicRoutesSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  val system = prepare.getSystem
  val topicActor = system.actorOf(Props[TopicActor])
  addServlet(new TopicRoutes(system, topicActor), "/*")

  prepare.DropTables
  prepare.CreateTables

  " get /topics" should {
    "get topics for a conversation" in {
      val user = User("someId", "Stephen", "Portanova", "PASSWORD")
      val writtenUser = UserIO().write(user)
      val recipientsHash = "dasfasfasfasd"
      TopicIO().write(Topic(user.id, recipientsHash, 4534535l, "subject1", 1l, 2l))
      TopicIO().write(Topic(user.id, recipientsHash, 54534535l, "subject2", 2l, 2l))

      get(s"/${user.id}/$recipientsHash") {
        status must_== 200
        val res = response.body
        res === """[{"userId":"someId","recipientsHash":"dasfasfasfasd","threadId":4534535,"subject":"subject1","ts":1,"emailCount":2},{"userId":"someId","recipientsHash":"dasfasfasfasd","threadId":54534535,"subject":"subject2","ts":2,"emailCount":2}]"""
      }
    }
  }
}