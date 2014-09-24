package com.textMailer.TypeClass

import org.joda.time.DateTime
import java.util.Date
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.textMailer.models.Conversation
import com.textMailer.models.Topic

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
    implicit object MostRecentItemsTopic extends MostRecentItems[Topic] {
      def ts(topic: Topic): Long = topic.ts
      def member(topic: Topic): String = topic.threadId.toString
    }
  }
}