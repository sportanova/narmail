package specs.prepare

import com.textMailer.IO.SimpleClient

object PrepareData {
  private lazy val prepareData = new PrepareData()
  def apply() = prepareData
}

class PrepareData() {
  val client = SimpleClient();
  client.connect("127.0.0.1");

  def CreateKeyspace() {
    client.createSchema("app_test");
  }
  
  def DropKeyspace {
    client.dropKeyspace("app_test")
  }
}