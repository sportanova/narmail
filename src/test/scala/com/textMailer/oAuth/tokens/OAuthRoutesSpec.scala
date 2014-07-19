package specs.oAuth.tokens

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.textMailer.IO.Eq
import com.textMailer.routes.OAuthRoutes
import akka.actor.Props
import com.textMailer.oAuth.tokens.AccessTokenActor

class OAuthRoutesSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  val system = prepare.getSystem
  val accessTokenActor = system.actorOf(Props[AccessTokenActor])
  addServlet(new OAuthRoutes(system, accessTokenActor), "/*")

  prepare.DropKeyspace
  prepare.CreateKeyspace

  "The refresh token" should {
    "be used to get a new access token for Gmail" in {
      val user = User("someId", "sportano@gmail.com", "Stephen", "Portanova","accessafasdfasdfasdf","1/roJI5cuO89mcZgj1e3N67kAxmSA1IBf5KEYZM7voWOo","PASSWORD")
      val writtenUser = UserIO().write(user)
      put(s"/accessToken/gmail/${user.id}") {
        status must_== 200
        println(s"@@@@@@@@@@@@@ response ${response.body}")
      }
    }
  }
}