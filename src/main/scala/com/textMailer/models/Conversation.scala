package com.textMailer.models

import java.util.UUID
import org.joda.time.DateTime

case class Conversation (
  userId: String,
  recipientsHash: String,
  recipients: Set[String],
  ts: Long,
  emailAccountId: String
) extends Model