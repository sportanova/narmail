package com.textMailer.models

import java.util.UUID

case class Email (
  id: String,
  userId: String,
  threadId: String,
  recipientsHash: String,
  recipients: Option[Set[String]],
  ts: Long,
  subject: String,
  sender: String,
  cc: String,
  bcc: String,
  textBody: String,
  htmlBody: String
) extends Model