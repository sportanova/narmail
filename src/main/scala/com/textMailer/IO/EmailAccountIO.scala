package com.textMailer.IO

import com.datastax.driver.core.BoundStatement
import java.util.UUID
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import scala.collection.JavaConverters._
import com.datastax.driver.core.ResultSet
import com.textMailer.models._
import collection.JavaConversions._
import scala.util.Try
import scala.concurrent.Future

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
  
  val asyncCurriedFind = asyncCurryFind(keyspace)(table)(build)(session) _

  def asyncFind(clauses: List[CassandraClause], limit: Int): Future[List[EmailAccount]] = {
    asyncCurriedFind(clauses)(limit)
  }
  
  val asyncCurriedWrite = asyncCurryWrite(session)(preparedStatement)(break) _
  
  def asyncWrite(emailAccount: EmailAccount): Future[EmailAccount] = {
    asyncCurriedWrite(emailAccount)
  }
  
  def build(row: Row): EmailAccount = {
    val userId = row.getString("user_id")
    val id = row.getString("id")
    val provider = row.getString("provider")
    val username = row.getString("username")
    val accessToken = row.getString("access_token")
    val refreshToken = row.getString("refresh_token")

    EmailAccount(userId, id, provider, username, accessToken, refreshToken)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(user_id, id, provider, username, access_token, refresh_token) " +
    "VALUES (?, ?, ?, ?, ?, ?);");
  
  def prepareWrite(emailAccount: EmailAccount): Unit = {
  }
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(emailAccount: EmailAccount): Try[EmailAccount] = {
    curriedWrite(emailAccount)
  }

  def break(emailAccount: EmailAccount, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      emailAccount.userId,
      emailAccount.id,
      emailAccount.provider,
      emailAccount.username,
      emailAccount.accessToken,
      emailAccount.refreshToken
    )
  }
}