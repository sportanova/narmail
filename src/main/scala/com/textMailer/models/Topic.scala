package com.textMailer.models

import java.util.UUID

case class Topic (
  userId: String,
  recipientsHash: String,
  threadId: String,
  subject: String,
  ts: Long,
  emailCount: Long
) extends Model