package com.textMailer.models

import java.util.UUID

case class Email (
  id: String,
  userId: String,
  threadId: String,
  recipientsHash: String,
  recipients: Option[Map[String,String]],
  ts: Long,
  subject: String,
  sender: Map[String,String], // cassandra hates 'from'
  cc: String,
  bcc: String,
  textBody: String,
  htmlBody: String,
  messageId: String,
  inReplyTo: Option[String] = None,
  references: Option[String] = None
) extends Model