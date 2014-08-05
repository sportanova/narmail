package specs.routes

import specs.prepare._
import org.scalatra.test.specs2._
import com.textMailer.IO.UserIO
import com.textMailer.models.User
import com.textMailer.IO.Eq
import com.textMailer.routes.UserRoutes
import akka.actor.Props
import com.textMailer.IO.actors.UserActor
import org.json4s.JsonDSL.WithDouble._
import org.json4s._
import org.json4s.jackson.JsonMethods._

class UserRoutesSpec extends MutableScalatraSpec {
  val prepare = PrepareData()
  val system = prepare.getSystem
  val userActor = system.actorOf(Props[UserActor])
  addServlet(new UserRoutes(system, userActor), "/*")

  prepare.DropTables
  prepare.CreateTables

  "POST /user" should {
    "be used create a new user when user info is provided" in {
      post(s"/", compact(render(Map("lastName" -> "portanova"))), Map("Content-Type" -> "application/json")) {
        status must_== 200
        val res = parse(response.body).values.asInstanceOf[Map[String,String]]
        res.get("lastName").get === "portanova"
      }
    }
    "be used create a new user when no user info is provided" in {
      post(s"/") {
        status must_== 200
        val res = response.body
        res !== "None"
      }
    }
  }
}