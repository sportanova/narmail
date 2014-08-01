package com.textMailer.models

import java.util.UUID

case class User (
  id: String,
  emails: String, // remove
  firstName: String,
  lastName: String,
  accessToken: String, // remove
  refreshToken: String, // remove
  password: String
) extends Model