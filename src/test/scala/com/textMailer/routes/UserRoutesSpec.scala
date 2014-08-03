package specs.routes

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.textMailer.IO.Eq
import com.textMailer.routes.UserRoutes
import akka.actor.Props
import com.textMailer.IO.actors.UserActor

class UserRoutesSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  val system = prepare.getSystem
  val userActor = system.actorOf(Props[UserActor])
  addServlet(new UserRoutes(system, userActor), "/*")

  prepare.DropTables
  prepare.CreateTables

  "The refresh token" should {
    "be used to get a new access token for Gmail" in {
      post(s"/") {
        status must_== 200
        val res = response.body
        res !== "None"
      }
    }
  }
}