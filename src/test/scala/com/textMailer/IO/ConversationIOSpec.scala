package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.ConversationIO
import com.textMailer.models.Conversation
import com.textMailer.IO.Eq

class ConversationIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropTables
  prepare.CreateTables

  "EmailIO.write" should {
    "write to the db" in {
      val conversation = Conversation("someUserId", "sportano@gmail.com", Set("sportano@gmail.com"))
      val writtenConversation = ConversationIO().write(conversation)
      val foundConversations = ConversationIO().find(List(Eq("user_id","someUserId"), Eq("recipients_hash","sportano@gmail.com")), 10)
      foundConversations.headOption.get.userId === "someUserId"
      foundConversations.headOption.get.recipients === Set("sportano@gmail.com")
    }
  }
}