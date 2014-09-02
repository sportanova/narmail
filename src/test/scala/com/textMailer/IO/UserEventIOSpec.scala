package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.ConversationIO
import com.textMailer.models.Conversation
import com.textMailer.IO.Eq
import com.textMailer.models.Topic
import com.textMailer.IO.TopicIO
import com.textMailer.models.UserEvent
import com.datastax.driver.core.utils.UUIDs
import com.textMailer.IO.UserEventIO

class UserEventIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropTables
  prepare.CreateTables

  "UserEventIO.write" should {
    "write to the db" in {
      val userId = UUIDs.random
      val userEvent = UserEvent(userId, "importEmails", 4534535l, Map())
      val writtenUserEvent = UserEventIO().write(userEvent)
      val foundUserEvents = UserEventIO().find(List(Eq("user_id",userId), Eq("event_type","importEmails")), 10)
      foundUserEvents.headOption.get.userId === userId
      foundUserEvents.headOption.get.eventType === "importEmails"
    }
  }
}