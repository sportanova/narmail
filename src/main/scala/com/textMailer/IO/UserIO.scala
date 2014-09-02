package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.textMailer.models.Conversation
import scala.collection.JavaConverters._
import com.textMailer.models.Conversation
import com.datastax.driver.core.ResultSet
import com.textMailer.models.User
import com.textMailer.models.UserEvent
import scala.util.Try
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.utils.UUIDs
import scala.concurrent.Future
import org.joda.time.DateTime

object UserIO {
  val client = SimpleClient()
  private lazy val UserIO = new UserIO(client)
  def apply() = UserIO 
}

class UserIO(client: SimpleClient) extends QueryIO {
  val table = "users"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[User] = {
    curriedFind(clauses)(limit)
  }
  
  val asyncCurriedFind = asyncCurryFind(keyspace)(table)(build)(session) _

  def asyncFind(clauses: List[CassandraClause], limit: Int): Future[List[User]] = {
    asyncCurriedFind(clauses)(limit)
  }
  
  val asyncCurriedWrite = asyncCurryWrite(session)(preparedStatement)(break) _
  
  def asyncWrite(user: User): Future[User] = {
    asyncCurriedWrite(user)
  }
  
  def build(row: Row): User = {
    val id = row.getString("id")
    val firstName = row.getString("first_name")
    val lastName = row.getString("last_name")
    val password = row.getString("password")
    
    User(id, firstName, lastName, password)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.users " +
    "(id, first_name, last_name, password) " +
    "VALUES (?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(user: User): Try[User] = {
    curriedWrite(user)
  }
  
  def preWrite(user: User): Unit = {
    val fake_uuid = java.util.UUID.fromString("f5183e19-d45e-4871-9bab-076c0cd2e422") // used as signup for all users - need better way to do this
    val userEvent = UserEvent(fake_uuid, "userSignup", new DateTime().getMillis, Map("userId" -> user.id))
    UserEventIO().write(userEvent)
  }
  
  def break(user: User, boundStatement: BoundStatement): BoundStatement = {
    preWrite(user) // TODO: add unit test

    boundStatement.bind(
      user.id,
      user.firstName,
      user.lastName,
      user.password
    )
  }
}