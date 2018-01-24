package org.prosolo.bigdata.scala.emails

import org.prosolo.common.domainmodel.user.notifications.NotificationType
import org.prosolo.common.email.generators.EmailContentGenerator

import scala.collection.immutable.HashMap

trait NotificationItemData{def actor:String}
case class CommentNotificationItemData(actor:String, predicate:String, objectTitle:String) extends NotificationItemData
case class FollowerNotificationItemData(actor:String) extends NotificationItemData


class NotificationsDigestEmailGenerator(val name:String,val total_number:Int, val notificationTypesCounts:HashMap[String,Int]) extends EmailContentGenerator{

  override def getTemplateName: String = {"notifications/notification-digest"}

  override def getSubject: String = {"Your Daily Notifications Diggest"}

  def hasComments():Boolean={ return hasNotificationType(NotificationType.Comment) }
  def hasFollowers():Boolean={ return hasNotificationType(NotificationType.Follow_User) }

  def hasNotificationType(notificationType:NotificationType):Boolean={ return notificationTypesCounts.getOrElse(notificationType.toString,0)>0 }
}
