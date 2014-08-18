import com.textMailer._
import org.scalatra._
import javax.servlet.ServletContext
import com.textMailer.routes._
import _root_.akka.actor.{ActorSystem, Props}
import com.textMailer.IO.actors._
import com.textMailer.oAuth.tokens.AccessTokenActor
import com.textMailer.IO.SimpleClient

class ScalatraBootstrap extends LifeCycle {
  val system = ActorSystem()
  val emailActor = system.actorOf(Props[EmailActor])
  val conversationActor = system.actorOf(Props[ConversationActor])
  val accessTokenActor = system.actorOf(Props[AccessTokenActor])
  val userActor = system.actorOf(Props[UserActor])
  val importEmailActor = system.actorOf(Props[ImportEmailActor])
  val topicActor = system.actorOf(Props[TopicActor])
  
  val client = SimpleClient();

  override def init(context: ServletContext) {
    val cass_ip = sys.env.get("general_cassandra_cluster_ip") match {
      case Some(ip) => ip
      case None => "127.0.0.1"
    }

    client.connect(cass_ip); // 54.183.164.178       127.0.0.1    // eip 54.183.66.201
    client.setKeyspace("app")
    client.createSchema();

    context.mount(new TextMailerServlet, "/*")
    context.mount(new OAuthRoutes(system, accessTokenActor), "/oauth")
    context.mount(new ConversationRoutes(system, conversationActor), "/conversations")
    context.mount(new TopicRoutes(system, topicActor), "/topics")
    context.mount(new EmailRoutes(system, emailActor), "/emails")
    context.mount(new UserRoutes(system, userActor), "/users")
    context.mount(new ImportEmailRoutes(system, importEmailActor), "/importEmail")
  }
  
  override def destroy(context:ServletContext) {
    client.close();
    system.shutdown()
  }
}
