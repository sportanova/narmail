package com.textMailer.Implicits

import org.joda.time.DateTime
import java.util.Date

object ImplicitConversions {
  implicit def javaDateToJoda(date: Date): DateTime = new DateTime(date);
}