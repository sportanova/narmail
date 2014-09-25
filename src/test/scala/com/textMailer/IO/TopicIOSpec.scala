package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.ConversationIO
import com.textMailer.models.Conversation
import com.textMailer.IO.Eq
import com.textMailer.models.Topic
import com.textMailer.IO.TopicIO
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TopicIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropTables
  prepare.CreateTables

  "TopicIO.write" should {
    "write to the db" in {
      val topic = Topic("someUserId", "sportano@gmail.com", 4534535l, "subject1", 1l, 2l)
      val writtenTopic = TopicIO().write(topic)
      val foundTopics = TopicIO().find(List(Eq("user_id","someUserId"), Eq("recipients_hash","sportano@gmail.com")), 10)
      foundTopics.headOption.get.userId === "someUserId"
      foundTopics.headOption.get.recipientsHash === "sportano@gmail.com"
    }
    
    "count number of topics for a conversation" in {
      val topic = Topic("someUserId", "sportano@gmail.com", 535l, "subject1", 1l, 2l)
      val writtenTopic = TopicIO().write(topic)
      val topic3 = Topic("someUserId", "sportano@gmail.com", 123l, "subject1", 1l, 2l)
      val writtenTopic3 = TopicIO().write(topic3)
      
      val countFuture = (for {
        count <- TopicIO().asyncCount(List(Eq("user_id","someUserId"), Eq("recipients_hash","sportano@gmail.com")), 100)  
      } yield(count))
      
      val count = Await.result(countFuture, Duration(1000, "millis"))
      count  === 3
    }
  }
}