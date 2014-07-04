package com.textMailer.model

import java.util.UUID

case class Conversation (
  user_id: String,
  subject: String,
  recipients: String
)