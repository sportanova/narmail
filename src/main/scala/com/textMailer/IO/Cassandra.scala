package com.textMailer.IO

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session

object SimpleClient {
  private lazy val client = new SimpleClient()
  def apply() = client
}


class SimpleClient() {
  private var cluster: Cluster = null

  var session: Session = null  
  def getSession = session

  def connect(node: String): Unit = {
    cluster = Cluster.builder().addContactPoint(node).build();
      
    val metaData = cluster.getMetadata();
    session = cluster.connect()
  }
  
  var keyspace: String = null
  def getKeyspace = keyspace
  def setKeyspace(newKeyspace: String) = {
    keyspace = newKeyspace
  }
  
  def createSchema(): Unit = {
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication " + 
      "= {'class':'SimpleStrategy', 'replication_factor':3};")
      
      // need email accounts table
      createTables()
  }
  
  def dropTable(table: String): Unit = {
    session.execute(s"DROP TABLE IF EXISTS $keyspace.$table")
  }
  
  def createTables(): Unit = {
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.users (" +
        "id text PRIMARY KEY," +
        "emails text," +
        "first_name text," +
        "last_name text," +
        "access_token text," +
        "refresh_token text," +
        "password text," +
      ");")
    
      // TODO: Add timestamp as value
    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.emails_by_conversation (" +
      "id text," +
      "user_id text," +
      "subject text," +
      "recipients_string text," +
      "time text," +
      "recipients text," +
      "cc text," +
      "bcc text," +
      "body text," +
      "PRIMARY KEY((user_id, recipients_string, subject), time, id)" +
    ");")
    
    // emails by user index table?
    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.new_emails_index (" +
      "user_id text," +
      "time text," +
      "recipients_hash text," +
      "subject text," +
      "PRIMARY KEY(user_id, time)" +
    ");")



    // add timestamp to primary key
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.conversations_by_user (" +
        "user_id text," +
        "subject text," +
        "recipients_string_hash text," +
        "recipients_string text," +
        "PRIMARY KEY((user_id), recipients_string_hash, subject)" +
      ");")
  }
  
  def dropKeyspace(keyspace: String): Unit = {
    session.execute(s"DROP KEYSPACE IF EXISTS $keyspace;")
  }
  
  def close() = {
    cluster.close
  }
}