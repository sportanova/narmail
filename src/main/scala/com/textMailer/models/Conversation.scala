package com.textMailer.models

import java.util.UUID

case class Conversation (
  userId: String,
  recipientsHash: String,
  recipients: Set[String]
) extends Model