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
import com.textMailer.models.EmailAccount
import com.textMailer.IO.EmailAccountIO
import org.scalatra.json._
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import com.textMailer.IO.EmailTopicIO

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
      val threadId = "4534535"
      TopicIO().write(Topic(user.id, recipientsHash, threadId, "subject1", 1l, 2l))
      val email1 = Email("123", "someId", Some(threadId), recipientsHash, None, 11l, "subject1", Map("sportano@gmail.com" -> "sportano@gmail.com"), "cc", "bcc", "emailBodyText", "emailBodyHtml", "msgId", None, None)
      val email2 = Email("321", "someId", Some(threadId), recipientsHash, None, 12l, "subject1", Map("sportano@gmail.com" -> "sportano@gmail.com"), "cc", "bcc", "emailBodyText", "emailBodyHtml", "msgId", Some("thing"), Some("else"))
      EmailTopicIO().write(email1)
      EmailTopicIO().write(email2)

      get(s"/${user.id}/${threadId}") {
        status must_== 200
        val res = response.body
        res === """[{"id":"321","userId":"someId","threadId":"4534535","recipientsHash":"dasfasfasfasd","recipients":{},"ts":12,"subject":"subject1","sender":{"sportano@gmail.com":"sportano@gmail.com"},"cc":"cc","bcc":"bcc","textBody":"emailBodyText","htmlBody":"emailBodyHtml","messageId":"msgId","inReplyTo":"thing","references":"else"},{"id":"123","userId":"someId","threadId":"4534535","recipientsHash":"dasfasfasfasd","recipients":{},"ts":11,"subject":"subject1","sender":{"sportano@gmail.com":"sportano@gmail.com"},"cc":"cc","bcc":"bcc","textBody":"emailBodyText","htmlBody":"emailBodyHtml","messageId":"msgId"}]"""
      }
    }
  }
  
  "post /emails/:emailAccountId"   should {
    val gmailAccount = EmailAccount("1", "13242342", "gmail", "sportano@gmail.com", "ya29.ggBQJxk5l6b11TpeLeNrr3ah6AOEtddjA0qRjp869-9KTSSLUnSjPhMI", "ya29.ggBQJxk5l6b11TpeLeNrr3ah6AOEtddjA0qRjp869-9KTSSLUnSjPhMI")
      EmailAccountIO().write(gmailAccount)
    "send an email" in {

      implicit val formats = Serialization.formats(NoTypeHints)
      val email1 = org.json4s.jackson.Serialization.write(Email("123l", "1", Some("2342342l"), "234242", Some(Map("sportano@gmail.com" -> "sportano@gmail.com")), 11l, "subject1", Map("dude@gmail.com" -> "dude@gmail.com"), "cc", "bcc", "emailBodyText", "emailBodyHtml", "msgId"))

      post(s"/${gmailAccount.id}", email1, Map("Content-Type" -> "application/json")) {
        
      }
    }
  }
}