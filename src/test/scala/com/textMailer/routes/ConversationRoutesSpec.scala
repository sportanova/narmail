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
import org.joda.time.DateTime

class ConversationRoutesSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  val system = prepare.getSystem
  val conversationActor = system.actorOf(Props[ConversationActor])
  addServlet(new ConversationRoutes(system, conversationActor), "/*")

  prepare.DropTables
  prepare.CreateTables

  " get /conversation" should {
    "get conversations for a user" in {
      val user = User("someId", "Stephen", "Portanova", "PASSWORD")
      val writtenUser = UserIO().write(user)
      val ts = {new DateTime}.getMillis
      ConversationIO().write(Conversation(user.id, "peter@gmail.com", Map("peter@gmail.com" -> "peter@gmail.com"), ts, "123", 2l, 4l))
      ConversationIO().write(Conversation(user.id, "stephen@gmail.com", Map("stephen@gmail.com" -> "stephen@gmail.com"), ts, "123", 3l, 9l))

      get(s"/${user.id}") {
        status must_== 200
        val res = response.body
        res === s"""[{"userId":"someId","recipientsHash":"peter@gmail.com","recipients":{"peter@gmail.com":"peter@gmail.com"},"ts":$ts,"emailAccountId":"123","topicCount":2,"emailCount":4},{"userId":"someId","recipientsHash":"stephen@gmail.com","recipients":{"stephen@gmail.com":"stephen@gmail.com"},"ts":$ts,"emailAccountId":"123","topicCount":3,"emailCount":9}]"""
      }
    }
  }
}