package com.textMailer.models

import java.util.UUID

case class Conversation (
  userId: String,
  subject: String,
  recipientsHash: String,
  recipients: Set[String]
) extends Model