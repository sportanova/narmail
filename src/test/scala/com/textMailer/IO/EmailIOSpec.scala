package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.EmailIO
import com.textMailer.models.Email
import com.textMailer.IO.Eq

class EmailIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  prepare.DropKeyspace
  prepare.CreateKeyspace

  "EmailIO.write" should {
    "write to the db" in {
      val email = Email("someId", "someUserId", "subject","recipients","time","cc","bcc","body")
      val writtenEmail = EmailIO().write(email)
      val foundEmails = EmailIO().find(List(Eq("user_id","someUserId"), Eq("recipients_string","recipients"), Eq("subject","subject")), 10)
      println(s"######## foundEmails $foundEmails")
      foundEmails.headOption.get.id === "someUserId"
    }
  }
}