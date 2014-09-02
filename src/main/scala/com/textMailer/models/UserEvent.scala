package com.textMailer.models

import java.util.UUID

case class UserEvent (
  userId: UUID,
  eventType: String,
  ts: Long
) extends Model