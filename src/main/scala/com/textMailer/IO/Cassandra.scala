package com.textMailer.IO

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session

object SimpleClient {
  private lazy val client = new SimpleClient()
  def apply() = client
}


class SimpleClient {
  private var cluster: Cluster = null
  var session: Session = null
  
  def getSession = session

  def connect(node: String): Unit = {
    cluster = Cluster.builder().addContactPoint(node).build();
      
    val metaData = cluster.getMetadata();
    session = cluster.connect()
  }
  
  def createSchema(keyspace: String): Unit = {
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication " + 
      "= {'class':'SimpleStrategy', 'replication_factor':3};")
    
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