package com.textMailer.models

import java.util.UUID

case class UserEvent (
  userId: UUID,
  eventType: String,
  ts: Long,
  data: Map[String,String]
) extends Model