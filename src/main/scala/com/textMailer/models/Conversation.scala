package com.textMailer.models

import java.util.UUID
import org.joda.time.DateTime

case class Conversation (
  userId: String,
  recipientsHash: String,
  recipients: Map[String,String],
  ts: Long,
  emailAccountId: String,
  topicCount: Long,
  emailCount: Long
) extends Model