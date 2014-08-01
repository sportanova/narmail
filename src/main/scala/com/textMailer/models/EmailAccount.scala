package com.textMailer.models

import java.util.UUID

case class EmailAccount (
  id: String,
  userId: String,
  provider: String,
  accessToken: String,
  refreshToken: String
) extends Model