package com.textMailer.models

import java.util.UUID

case class User (
  id: String,
  emails: String,
  firstName: String,
  lastName: String,
  accessToken: String,
  refreshToken: String,
  password: String
) extends Model