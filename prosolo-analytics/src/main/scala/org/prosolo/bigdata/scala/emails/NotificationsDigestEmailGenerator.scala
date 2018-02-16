package org.prosolo.bigdata.scala.emails

import java.util

import org.prosolo.bigdata.scala.spark.emails.Notification
import org.prosolo.common.domainmodel.user.notifications.NotificationType
import org.prosolo.common.email.generators.EmailContentGenerator
import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

trait NotificationItemData{def actor:String}
case class CommentNotificationItemData(actor:String, predicate:String, objectTitle:String) extends NotificationItemData
case class FollowerNotificationItemData(actor:String) extends NotificationItemData


class NotificationsDigestEmailGenerator(val name:String,val total_number:Int, val notificationTypesCounts:HashMap[String,Int], val notificationsByType:HashMap[String,Array[Notification]]) extends EmailContentGenerator{

  override def getTemplateName: String = {"notifications/notification-digest"}

  override def getSubject: String = {"Your Daily Notifications Diggest"}

  def hasComments():Boolean={hasNotificationType(NotificationType.Comment) }
  def hasFollowers():Boolean={hasNotificationType(NotificationType.Follow_User) }
  def followersCount():Int={ getNotificationTypeCount(NotificationType.Follow_User)}
  def followers():java.util.List[Notification]={ getNotificationsByType(NotificationType.Follow_User)}

  def hasNotificationType(notificationType:NotificationType):Boolean={ notificationTypesCounts.getOrElse(notificationType.toString,0)>0 }
  def getNotificationTypeCount(notificationType:NotificationType):Int={  notificationTypesCounts.getOrElse(notificationType.toString,0) }
  def getNotificationsByType(notificationType:NotificationType):java.util.List[Notification]={notificationsByType.get(notificationType.name).get.toList}
}
