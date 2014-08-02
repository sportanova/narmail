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
import scala.util.Try
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.utils.UUIDs

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
  
  def createUserIfNotExists(userInfo: Map[String,String]) = {
    this.find(List(Eq("id", userInfo.get("userId").get)), 1).headOption match {
      case Some(u) => None
      case None => this.write(User(UUIDs.random().toString, "", "", ""))
    }
  }
  
  def break(user: User, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      user.id,
      user.firstName,
      user.lastName,
      user.password
    )
  }
}