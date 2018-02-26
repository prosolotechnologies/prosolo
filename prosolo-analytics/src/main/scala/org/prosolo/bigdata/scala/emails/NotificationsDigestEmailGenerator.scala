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


  def hasFollowers():Boolean={hasNotificationType(NotificationType.Follow_User) }
  def followersCount():Int={ getNotificationTypeCount(NotificationType.Follow_User)}
  def followers():java.util.List[Notification]={ getNotificationsByType(NotificationType.Follow_User)}

  def hasAnnouncements():Boolean={hasNotificationType(NotificationType.AnnouncementPublished)}
  def announcementsCount():Int={ getNotificationTypeCount(NotificationType.AnnouncementPublished)}
  def announcements():java.util.List[Notification]={ getNotificationsByType(NotificationType.AnnouncementPublished)}

  def hasComments():Boolean={hasNotificationType(NotificationType.Comment) }
  def commentsCount():Int={ getNotificationTypeCount(NotificationType.Comment)}


  def hasAssessmentsApproved():Boolean={hasNotificationType(NotificationType.Assessment_Approved)}
  def assessmentApprovedCount():Int={ getNotificationTypeCount(NotificationType.Assessment_Approved)}

  def hasAssessmentsComments():Boolean={hasNotificationType(NotificationType.Assessment_Comment)}
  def assessmentCommentCount():Int={ getNotificationTypeCount(NotificationType.Assessment_Comment)}

  def hasAssessmentsRequest():Boolean={hasNotificationType(NotificationType.Assessment_Requested)}
  def assessmentRequestCount():Int={ getNotificationTypeCount(NotificationType.Assessment_Requested)}


  def hasNotificationType(notificationType:NotificationType):Boolean={ notificationTypesCounts.getOrElse(notificationType.toString,0)>0 }
  def getNotificationTypeCount(notificationType:NotificationType):Int={  notificationTypesCounts.getOrElse(notificationType.toString,0) }
  def getNotificationsByType(notificationType:NotificationType):java.util.List[Notification]={
    println("NOTIFICATION TYPE:"+notificationType.name+"----"+notificationsByType.get(notificationType.name).get.mkString(","));
    notificationsByType.get(notificationType.name).get.toList

  }
}
