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
import com.textMailer.IO.actors.EmailActor
import com.textMailer.IO.EmailIO
import com.textMailer.IO.TopicIO
import com.textMailer.models.Topic
import com.textMailer.models.Email
import com.textMailer.routes.EmailRoutes

class EmailRoutesSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  val system = prepare.getSystem
  val emailActor = system.actorOf(Props[EmailActor])
  addServlet(new EmailRoutes(system, emailActor), "/*")

  prepare.DropTables
  prepare.CreateTables

  "get /emails" should {
    "get emails for a topic" in {
      val user = User("someId", "Stephen", "Portanova", "PASSWORD")
      val writtenUser = UserIO().write(user)
      val recipientsHash = "dasfasfasfasd"
      val threadId = 4534535l
      TopicIO().write(Topic(user.id, recipientsHash, threadId, "subject1", 1l))
      val email1 = Email(123l, "someId", threadId, recipientsHash, 11l, "subject1", "sender1", "cc", "bcc", "emailBodyText", "emailBodyHtml")
      val email2 = Email(321l, "someId", threadId, recipientsHash, 12l, "subject1", "sender2", "cc", "bcc", "emailBodyText", "emailBodyHtml")
      EmailIO().write(email1)
      EmailIO().write(email2)

      get(s"/${user.id}/${threadId.toString}") {
        status must_== 200
        val res = response.body
        res === """[{"id":321,"userId":"someId","threadId":4534535,"recipientsHash":"dasfasfasfasd","ts":12,"subject":"subject1","sender":"sender2","cc":"cc","bcc":"bcc","textBody":"emailBodyText","htmlBody":"emailBodyHtml"},{"id":123,"userId":"someId","threadId":4534535,"recipientsHash":"dasfasfasfasd","ts":11,"subject":"subject1","sender":"sender1","cc":"cc","bcc":"bcc","textBody":"emailBodyText","htmlBody":"emailBodyHtml"}]"""
      }
    }
  }
}