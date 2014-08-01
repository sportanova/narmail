package com.textMailer.models

import java.util.UUID

case class EmailAccount (
  userId: String,
  id: String,
  provider: String,
  username: String,
  accessToken: String,
  refreshToken: String
) extends Model