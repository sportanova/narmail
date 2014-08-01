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
import com.textMailer.models.EmailAccount
import collection.JavaConversions._

object EmailAccountIO {
  val client = SimpleClient()
  private lazy val emailAccountIO = new EmailAccountIO(client)
  def apply() = emailAccountIO
}

class EmailAccountIO(client: SimpleClient) extends QueryIO {
  val table = "email_accounts"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[EmailAccount] = {
    curriedFind(clauses)(limit)
  }
  
  def build(row: Row): EmailAccount = {
    val userId = row.getString("user_id")
    val id = row.getString("id")
    val provider = row.getString("provider")
    val accessToken = row.getString("access_token")
    val refreshToken = row.getString("refresh_token")

    EmailAccount(id, userId, provider, accessToken, refreshToken)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(user_id, id, provider, access_token, refresh_token) " +
    "VALUES (?, ?, ?, ?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(index1: EmailAccount): ResultSet = {
    curriedWrite(index1)
  }

  def break(emailAccount: EmailAccount, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      emailAccount.id,
      emailAccount.userId,
      emailAccount.provider,
      emailAccount.accessToken,
      emailAccount.refreshToken
    )
  }
}