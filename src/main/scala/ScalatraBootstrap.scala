import com.textMailer._
import org.scalatra._
import javax.servlet.ServletContext
import com.textMailer.routes._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new TextMailerServlet, "/*")
    context.mount(new OAuthServlet, "/oauth")
    context.mount(new ConversationRoutes, "/conversations")
    context.mount(new EmailRoutes, "/emails")
  }
}
