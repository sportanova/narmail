package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.textMailer.IO.Eq

class UserIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()

  "UserIO.write" should {
    "write to the db" in {
      val user = User("someId", "Stephen", "Portanova","PASSWORD")
      val writtenUser = UserIO().write(user)
      val foundUsers = UserIO().find(List(Eq("id","someId")), 10)
      foundUsers.headOption.get.id === "someId"
    }
  }
}