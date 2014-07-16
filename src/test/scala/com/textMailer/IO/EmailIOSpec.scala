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
    "do stuff" in {
      val email = Email("someId", "someUserId", "subject","recipients","time","cc","bcc","body")
      val writtenEmail = EmailIO().write(email)
      println(s"######## writtenEmail $writtenEmail")
      val findEmail = EmailIO().find(List(Eq("", "")), 10)
      val x = 1
    }
  }
  
  "find" should {
    "do stuff" in {
      val a = 1
      a === 1
    }
  }
  
}