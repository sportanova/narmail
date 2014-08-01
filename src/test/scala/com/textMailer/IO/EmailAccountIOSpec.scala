package specs.prepare.IO

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.EmailAccountIO
import com.textMailer.models.EmailAccount
import com.textMailer.IO.Eq

class EmailAccountIOSpec extends MutableScalatraSpec {
  val prepare = PrepareData()

  "Index1IO.write" should {
    "write to the db" in {
      val gmailAccount = EmailAccount("1", "13242342", "gmail", "23424sdjfsf", "afdasfasdfsadfsf")
      val writtenGmailAccount = EmailAccountIO().write(gmailAccount)
      val iCloudAccount = EmailAccount("1", "2343252525", "iCloud", "fdafadsfajjasf", "8844juuow")
      val writteniCloudAccount = EmailAccountIO().write(iCloudAccount)

      val foundEmailAccounts = EmailAccountIO().find(List(Eq("user_id","1")), 10)
      foundEmailAccounts.size === 2

      val accountIds = foundEmailAccounts.map(account => account.id)
      accountIds.contains("13242342") === true
      accountIds.contains("2343252525") === true
    }
  }
}