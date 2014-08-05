package com.textMailer.models

import java.util.UUID

case class Email (
  id: String,
  userId: String,
  subject: String,
  recipientsHash: String,
  time: String,
  cc: String,
  bcc: String,
  body: String
) extends Model