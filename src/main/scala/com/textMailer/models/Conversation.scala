package com.textMailer.models

import java.util.UUID

case class Conversation (
  userId: String,
  subject: String,
  recipients: String
) extends Model