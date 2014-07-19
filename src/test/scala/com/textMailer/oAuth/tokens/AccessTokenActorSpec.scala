package specs.oAuth.tokens

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.textMailer.IO.Eq
import com.textMailer.routes.OAuthServlet

class AccessTokenActorSpec extends MutableScalatraSpec {
  addServlet(classOf[OAuthServlet], "/*")
  val prepare = PrepareData()
  prepare.DropKeyspace
  prepare.CreateKeyspace

  "Refreshinging an access token" should {
    "work for Gmail" in {
      put("/accessToken/gmail") {
        status must_== 200
      }
    }
  }
}