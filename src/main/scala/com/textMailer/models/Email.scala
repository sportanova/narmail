package com.textMailer.models

import java.util.UUID

case class Email (
  id: String,
  user_id: String,
  subject: String,
  recipients: String,
  time: String,
  cc: String,
  bcc: String,
  body: String
)