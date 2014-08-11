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
  val conversationActor = system.actorOf(Props[ConversationActor])
  val accessTokenActor = system.actorOf(Props[AccessTokenActor])
  val userActor = system.actorOf(Props[UserActor])
  val importEmailActor = system.actorOf(Props[ImportEmailActor])
  val topicActor = system.actorOf(Props[TopicActor])

  override def init(context: ServletContext) {
    context.mount(new TextMailerServlet, "/*")
    context.mount(new OAuthRoutes(system, accessTokenActor), "/oauth")
    context.mount(new ConversationRoutes(system, conversationActor), "/conversations")
    context.mount(new TopicRoutes(system, topicActor), "/topics")
    context.mount(new EmailRoutes(system, emailActor), "/emails")
    context.mount(new UserRoutes(system, userActor), "/users")
    context.mount(new ImportEmailRoutes(system, importEmailActor), "/importEmail")
  }
  
  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}
