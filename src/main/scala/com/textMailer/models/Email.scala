package com.textMailer.models

import java.util.UUID

case class Email (
  id: String,
  userId: String,
  threadId: Long,
  recipientsHash: String,
  time: String,
  subject: String,
  cc: String,
  bcc: String,
  body: String
) extends Model