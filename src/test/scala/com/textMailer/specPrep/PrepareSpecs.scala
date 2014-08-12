package specs.prepare

import com.textMailer.IO.SimpleClient
import akka.actor.ActorSystem

object PrepareData {
  private lazy val prepareData = new PrepareData()
  def apply() = prepareData
}

class PrepareData() {
  val client = SimpleClient();
  client.connect("127.0.0.1");
  client.setKeyspace("app_test")
  
  val system = ActorSystem()
  def getSystem = system

  def CreateTables() {
    client.createSchema()
  }
  
  def DropTables {
    client.dropTable("users")
    client.dropTable("emails_by_topic")
    client.dropTable("conversations_by_user")
    client.dropTable("new_emails_index")
    client.dropTable("index_1")
    client.dropTable("email_accounts")
    client.dropTable("topics_by_conversation")
  }
}