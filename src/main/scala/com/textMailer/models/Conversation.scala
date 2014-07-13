package com.textMailer.models

import java.util.UUID

case class Conversation (
  user_id: String,
  subject: String,
  recipients: String
)