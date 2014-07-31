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
import com.textMailer.models.Index1
import collection.JavaConversions._

object Index1IO {
  val client = SimpleClient()
  private lazy val Index1IO = new Index1IO(client)
  def apply() = Index1IO 
}

class Index1IO(client: SimpleClient) extends QueryIO {
  val table = "index_1"
  val session = client.getSession
  val keyspace = client.getKeyspace

  val curriedFind = curryFind(keyspace)(table)(build)(session) _

  def find(clauses: List[CassandraClause], limit: Int): List[Index1] = {
    curriedFind(clauses)(limit)
  }
  
  def build(row: Row): Index1 = {
    val indexedValue = row.getString("indexed_value_1")
    val str: java.lang.String = ""
    val data: Map[String,String] = row.getMap("data", str.getClass, str.getClass).toMap

    Index1(indexedValue, data)
  }
  
  val preparedStatement = session.prepare(
    s"INSERT INTO $keyspace.$table " +
    "(indexed_value_1, data) " +
    "VALUES (?, ?);");
  
  val curriedWrite = curryWrite(session)(preparedStatement)(break) _

  def write(index1: Index1): ResultSet = {
    curriedWrite(index1)
  }
  
  def break(index1: Index1, boundStatement: BoundStatement): BoundStatement = {
    boundStatement.bind(
      index1.indexedValue1,
      mapAsJavaMap(index1.data)
    )
  }
}