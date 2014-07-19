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

  def CreateKeyspace() {
    client.createSchema();
  }
  
  def DropKeyspace {
    client.dropKeyspace("app_test")
  }
}