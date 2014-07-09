package com.textMailer.model

import java.util.UUID

case class Message (
  id: String,
  user_id: String,
  subject: String,
  recipients: String,
  time: String,
  cc: String,
  bcc: String,
  body: String
)