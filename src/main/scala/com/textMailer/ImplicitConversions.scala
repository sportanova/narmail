package com.textMailer.Implicits

import org.joda.time.DateTime
import java.util.Date
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object ImplicitConversions {
  implicit def javaDateToJoda(date: Date): DateTime = new DateTime(date)

  implicit def OptionStringToOptionLong(str: Option[String]): Option[Long] = {
    str match {
      case Some(str) => {
        Try{str.toLong} match {
          case Success(l) => Some(l)
          case Failure(ex) => None
        }
      }
      case None => None
    }
  }
}