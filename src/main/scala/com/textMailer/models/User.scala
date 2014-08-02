package com.textMailer.models

import java.util.UUID

case class User (
  id: String,
  firstName: String,
  lastName: String,
  password: String
) extends Model