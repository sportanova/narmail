package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.EmailIO
import com.textMailer.models.Email
import com.textMailer.IO.Eq
import com.textMailer.IO.EmailTopicIO

class EmailIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropTables
  prepare.CreateTables

  "EmailIO.write" should {
    "write to the db" in {
      val email = Email("123", "someUserId", Some("4535335"), "recipients", None, 234243l, "subject", Map("sportano@gmail.com" -> "sportano@gmail.com"), "cc","bcc","body", "emailBodyHtml", "msg_id")
      val writtenEmail = EmailTopicIO().write(email)
      val foundEmails = EmailTopicIO().find(List(Eq("user_id","someUserId"), Eq("thread_id", "4535335")), 10)
      foundEmails.headOption.get.id === "123"
    }
  }
}