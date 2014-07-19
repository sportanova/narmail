import com.textMailer._
import org.scalatra._
import javax.servlet.ServletContext
import com.textMailer.routes._
import _root_.akka.actor.{ActorSystem, Props}
import com.textMailer.IO.actors._
import com.textMailer.oAuth.tokens.AccessTokenActor

class ScalatraBootstrap extends LifeCycle {
  val system = ActorSystem()
  val emailActor = system.actorOf(Props[EmailActor])
  val accessTokenActor = system.actorOf(Props[AccessTokenActor])

  override def init(context: ServletContext) {
    context.mount(new TextMailerServlet, "/*")
    context.mount(new OAuthRoutes(system, accessTokenActor), "/oauth")
    context.mount(new ConversationRoutes, "/conversations")
    context.mount(new EmailRoutes(system, emailActor), "/emails")
  }
  
  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}
