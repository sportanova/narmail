package com.textMailer.models

import java.util.UUID

case class Topic (
  userId: String,
  recipientsHash: String,
  threadId: Long,
  subject: String
) extends Model