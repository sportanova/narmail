package com.textMailer.IO

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.textMailer.models.Conversation

trait QueryIO {
//  def find(limit: Int): List[Conversation] = {
//    val query = QueryBuilder.select().all().from("app","conversations_by_user").limit(limit)
//    session.execute(query).all.asScala.toList.map(row => build(row))
//  }
}