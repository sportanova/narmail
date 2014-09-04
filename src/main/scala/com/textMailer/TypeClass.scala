package com.textMailer.TypeClass

import org.joda.time.DateTime
import java.util.Date
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.textMailer.models.Conversation

object TypeClass {
  trait MostRecentItems[T] {
    def ts(item: T): Long
    def member(item: T): String
  }
  object MostRecentItems {
    implicit object MostRecentItemsConversation extends MostRecentItems[Conversation] {
      def ts(conversation: Conversation): Long = conversation.ts
      def member(conversation: Conversation): String = conversation.recipientsHash
    }
  }
}