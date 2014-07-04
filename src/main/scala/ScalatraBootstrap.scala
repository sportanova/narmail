import com.textMailer._
import org.scalatra._
import javax.servlet.ServletContext
import com.textMailer.routes.OAuthServlet
import com.textMailer.services.ConversationsService

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new TextMailerServlet, "/*")
    context.mount(new OAuthServlet, "/oauth")
    context.mount(new ConversationsService, "/conversations")
  }
}
