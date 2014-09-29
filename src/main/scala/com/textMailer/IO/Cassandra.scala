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
    
      // TODO: shorten cf names to converations, topics, emails, etc.. 

    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.emails_by_topic (" +
      "id bigint," +
      "user_id text," +
      "thread_id bigint," + // TODO: add account id?
      "recipients_hash text," +
      "recipients set<text>," +
      "ts bigint," +
      "subject text," +
      "sender text," +
      "cc text," +
      "bcc text," +
      "text_body text," +
      "html_body text," +
      "PRIMARY KEY((user_id, thread_id), ts, id)" +
    ") with clustering order by (ts desc);")
    
    session.execute(
    s"CREATE TABLE IF NOT EXISTS $keyspace.emails_by_conversation (" +
      "id bigint," +
      "user_id text," +
      "thread_id bigint," + // TODO: add account id?
      "recipients_hash text," +
      "recipients set<text>," +
      "ts bigint," +
      "subject text," +
      "sender text," +
      "cc text," +
      "bcc text," +
      "text_body text," +
      "html_body text," +
      "PRIMARY KEY((user_id, recipients_hash), ts, id)" +
    ") with clustering order by (ts desc);")
    
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
      s"CREATE TABLE IF NOT EXISTS $keyspace.email_accounts (" + // TODO: add gmail_user_id 100030981325891290860 ya29.jQAqCp5kI3gS9GU2NuDfXhKGMGMRp_WqPQjDls126xmXQ5qxFxxYNy6U
      "user_id text," +
      "id text," +
      "provider text," +
      "username text," +
      "access_token text," +
      "refresh_token text," +
      "PRIMARY KEY(user_id, id));") // TODO: start checking for access tokens from user_events cf? that way wouldn't keep updating / compactions

    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.conversations_by_user (" +
        "user_id text," +
        "recipients_hash text," +
        "recipients set<text>," +
        "ts bigint," +
        "email_account_id text," +
        "em_cnt bigint," +
        "tp_cnt bigint," +
        "PRIMARY KEY(user_id, recipients_hash)" +
    ");")
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.ordered_conversations_by_user (" +
        "user_id text," +
        "recipients_hash text," +
        "recipients set<text>," +
        "ts bigint," +
        "email_account_id text," +
        "em_cnt bigint," +
        "tp_cnt bigint," +
        "PRIMARY KEY((user_id), ts)" +
    ") with clustering order by (ts desc);") // TODO: add ttl
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.topics_by_conversation (" +
        "user_id text," +
        "recipients_hash text," +
        "thread_id bigint," +
        "subject text," +
        "ts bigint," +
        "em_cnt bigint," +
        "PRIMARY KEY((user_id, recipients_hash), thread_id)" +
    ");")
    
    session.execute(
      s"CREATE TABLE IF NOT EXISTS $keyspace.ordered_topics_by_conversation (" +
        "user_id text," +
        "recipients_hash text," +
        "thread_id bigint," +
        "subject text," +
        "ts bigint," +
        "em_cnt bigint," +
        "PRIMARY KEY((user_id, recipients_hash), ts, thread_id)" +
    ") with clustering order by (ts desc);") // TODO: add ttl
    
    session.execute(     // for users joining, importing user's emails, updating user's refresh tokens
      s"CREATE TABLE IF NOT EXISTS $keyspace.user_events (" +
        "user_id uuid," +
        "event_type text," +
        "ts bigint," +
        "data map<text,text>," +
        "PRIMARY KEY((user_id, event_type), ts)" +
    ") with clustering order by (ts desc);") // TODO: add ttl
      
      // user => conversation => topic => email
    
//    insert into users (id, first_name, last_name, password) VALUES ('bbe1131d-3be5-4997-a1ee-295f6f2c9dbf', 'stephen', 'portanova', 'pw');
    // insert into ordered_conversations_by_user (user_id, recipients_hash, recipients, ts) VALUES ('bbe1131d-3be5-4997-a1ee-295f6f2c9dbf', 'supercalifrag', {'john', 'jacob'}, 1409965333000);
    // delete from ordered_conversations_by_user where user_id = 'bbe1131d-3be5-4997-a1ee-295f6f2c9dbf' AND ts = 1409965333000;

//    insert into email_accounts (user_id, id, provider, username, access_token, refresh_token) VALUES ('bbe1131d-3be5-4997-a1ee-295f6f2c9dbf', '90a5d5c6-9165-4080-a7aa-cc4b45268ef3', 'gmail', 'sportano@gmail.com', 'ya29.bwAp7qQU6MPSHSEAAABEnpRPrAiQk_M1e_2HOxc9sv6AjUEblpEHY7rE2EBeR4kvsPJi4NzZ7sfDyeetnoo', '1/v80mUQGjMDXYYJ56F7Tx1H62yLiWcMODON1xZett0EM');
//    Insert into user_events (user_id, event_type, ts, data) VALUES(f5183e19-d45e-4871-9bab-076c0cd2e422, 'userSignup', 1407961587000, {'userId':'bbe1131d-3be5-4997-a1ee-295f6f2c9dbf'});
//    update email_accounts set access_token = 'ya29.jgA2eVbmFNfqdgRh6_YL6W4yFZPl8YuHVWjCSrdGjcVc_PNS3b9Y8L7r' where user_id = 'bbe1131d-3be5-4997-a1ee-295f6f2c9dbf' AND id = 'c0cf3490-25a2-4071-a486-5e0e62247f8d';
  }
  
  def dropKeyspace(keyspace: String): Unit = {
    session.execute(s"DROP KEYSPACE IF EXISTS $keyspace;")
  }
  
  def close() = {
    cluster.close
  }
}