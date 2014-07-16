package specs.prepare

import com.textMailer.IO.SimpleClient

object PrepareData {
  private lazy val prepareData = new PrepareData()
  def apply() = prepareData
}

class PrepareData() {
  val client = SimpleClient();
  client.connect("127.0.0.1");
  client.setKeyspace("app_test")

  def CreateKeyspace() {
    client.createSchema();
  }
  
  def DropKeyspace {
    client.dropKeyspace("app_test")
  }
}