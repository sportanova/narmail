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
  
  def build(row: Row): User = {
    val id = row.getString("id")
    val emails = row.getString("emails")
    val firstName = row.getString("first_name")
    val lastName = row.getString("last_name")
    val accessToken = row.getString("access_token")
    val refreshToken = row.getString("refresh_token")
    val password = row.getString("password")
    
    User(id, emails, firstName, lastName, accessToken, refreshToken, password)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.users " +
    "(id, emails, first_name, last_name, access_token, refresh_token, password) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(user: User): ResultSet = {
    curriedWrite(user)
  }
  
  def break(user: User, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      user.id,
      user.emails,
      user.firstName,
      user.lastName,
      user.accessToken,
      user.refreshToken,
      user.password
    )
  }
}