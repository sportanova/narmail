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

      createTables()
  }
  
  def dropTable(table: String): Unit = {
    session.execute(s"DROP TABLE IF EXISTS $keyspace.$table")
  }
  
  def createTables(): Unit = {
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.users (" +
        "id text PRIMARY KEY," +
        "first_name text," +
        "last_name text," +
        "password text," +
      ");")
    
      // TODO: Add timestamp as value
      // TODO: add email id from email?
    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.emails_by_topic (" +
      "id text," +
      "user_id text," +
      "thread_id bigint," +
      "recipients_hash text," +
      "time text," +
      "subject text," +
      "sender text," +
      "cc text," + // TODO: move to conversation?
      "bcc text," + // move to conversation? And add "to"
      "text_body text," +
      "html_body text," +
      "PRIMARY KEY((user_id, thread_id), time, id)" + // be email timestamp, not server timestamp
    ");")
    
    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.new_emails_index (" +
      "user_id text," +
      "time text," +
      "recipients_hash text," +
      "subject text," +
      "PRIMARY KEY(user_id, time)" +
    ");")
    
    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.index_1 (" +
      "indexed_value_1 text PRIMARY KEY," +
      "data map<text,text>" +
    ");")
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.email_accounts (" +
      "user_id text," +
      "id text," +
      "provider text," +
      "username text," +
      "access_token text," +
      "refresh_token text," +
      "PRIMARY KEY(user_id, id));")

    // add timestamp to primary key?
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.conversations_by_user (" +
        "user_id text," +
        "recipients_hash text," +
        "recipients Set<text>," +
        "ts bigint," +
        "PRIMARY KEY(user_id, recipients_hash)" +
    ");")
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.ordered_conversations_by_user (" +
        "user_id text," +
        "recipients_hash text," +
        "recipients Set<text>," +
        "ts bigint," +
        "PRIMARY KEY((user_id), ts)" +
    ") with clustering order by (ts desc);")
    
    // index table listing all topics / conversations in the order they came???
    //    session.execute(
//      s"CREATE TABLE IF NOT EXISTS $keyspace.conversations_or_topics_by_time (" +
//        "user_id text," +
//        "conv_or_top text," + // <'conversation' || 'topic'>
//        "ts timestamp," +
//        "attrs Map<text,text>," + // attributes - queryable primary key && denormalized data
//        "PRIMARY KEY((user_id, index_category), ts)" +
//    ");")
    
    //TABLE COMBINING EVERYTHING?
//    session.execute(
//      s"CREATE TABLE IF NOT EXISTS $keyspace.conversations_by_user (" +
//        "user_id text," +
//        "recipients_hash text," +
//        "thread_id bigint," +
//        "email_id text," +
//        "ts timestamp," +
//        "text_body text," +
//        "html_body text," +
//        "email_info Map<text,text>," + // subject, recipients, sender, cc, bcc
//        "PRIMARY KEY(user_id, recipients_hash, thread_id, email_id, ts)" +
//    ");")
    
    // TODO: unique conversations / topics index table - list the uniques, no duplicates???
    // "PRIMARY KEY(user_id, index_category <conversation || topic>, Map<primary key values>)" - no timestamp in primary key
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.topics_by_conversation (" +
        "user_id text," +
        "recipients_hash text," +
        "thread_id bigint," +
        "subject text," +
        "ts bigint," +
        "PRIMARY KEY((user_id, recipients_hash), thread_id)" +
    ");")
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.ordered_topics_by_conversation (" +
        "user_id text," +
        "recipients_hash text," +
        "thread_id bigint," +
        "subject text," +
        "ts bigint," +
        "PRIMARY KEY((user_id), ts, thread_id)" +
    ") with clustering order by (ts desc);")
      
      // user => conversation => topic => email
    
//    insert into users (id, first_name, last_name, password) VALUES ('bbe1131d-3be5-4997-a1ee-295f6f2c9dbf', 'stephen', 'portanova', 'pw');

//    insert into email_accounts (user_id, id, provider, username, access_token, refresh_token) VALUES ('bbe1131d-3be5-4997-a1ee-295f6f2c9dbf', '90a5d5c6-9165-4080-a7aa-cc4b45268ef3', 'gmail', 'sportano@gmail.com', 'ya29.bwAp7qQU6MPSHSEAAABEnpRPrAiQk_M1e_2HOxc9sv6AjUEblpEHY7rE2EBeR4kvsPJi4NzZ7sfDyeetnoo', '1/v80mUQGjMDXYYJ56F7Tx1H62yLiWcMODON1xZett0EM');
  }
  
  def dropKeyspace(keyspace: String): Unit = {
    session.execute(s"DROP KEYSPACE IF EXISTS $keyspace;")
  }
  
  def close() = {
    cluster.close
  }
}