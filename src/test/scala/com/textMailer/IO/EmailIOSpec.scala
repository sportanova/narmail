package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.EmailIO
import com.textMailer.models.Email
import com.textMailer.IO.Eq

class EmailIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropTables
  prepare.CreateTables

  "EmailIO.write" should {
    "write to the db" in {
      val email = Email("someId", "someUserId", 4535335l, "recipients","time", "subject", "sender", "cc","bcc","body", "emailBodyHtml")
      val writtenEmail = EmailIO().write(email)
      val foundEmails = EmailIO().find(List(Eq("user_id","someUserId"), Eq("thread_id", 4535335l)), 10)
      foundEmails.headOption.get.id === "someUserId"
    }
  }
}