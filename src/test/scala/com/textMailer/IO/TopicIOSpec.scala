package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.ConversationIO
import com.textMailer.models.Conversation
import com.textMailer.IO.Eq
import com.textMailer.models.Topic
import com.textMailer.IO.TopicIO

class TopicIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropTables
  prepare.CreateTables

  "TopicIO.write" should {
    "write to the db" in {
      val topic = Topic("someUserId", "sportano@gmail.com", "subject1")
      val writtenTopic = TopicIO().write(topic)
      val foundTopics = TopicIO().find(List(Eq("user_id","someUserId"), Eq("recipients_hash","sportano@gmail.com")), 10)
      foundTopics.headOption.get.userId === "someUserId"
      foundTopics.headOption.get.recipientsHash === "sportano@gmail.com"
    }
  }
}