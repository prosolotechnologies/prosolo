package org.prosolo.bigdata.scala.emails


import java.util
import java.util.Date

import org.prosolo.bigdata.scala.spark.emails.{Notification, NotificationSections}
import org.prosolo.common.domainmodel.user.notifications.NotificationType
import org.prosolo.common.email.generators.EmailContentGenerator
import org.prosolo.common.util.date.DateUtil

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

trait NotificationItemData{def actor:String}
case class CommentNotificationItemData(actor:String, predicate:String, objectTitle:String) extends NotificationItemData
case class FollowerNotificationItemData(actor:String) extends NotificationItemData



class NotificationsDigestEmailGenerator(
                                         val name:String,
                                         val total_number:Int,
                                         val notificationTypesCounts:HashMap[String,Int],
                                         val notificationsByType:HashMap[String,Array[Notification]],
                                         val role: NotificationSections.NotificationSection) extends EmailContentGenerator{
  val DISPLAY_NUMBER=3
  override def getTemplateName: String = {"notifications/notification-digest"}
  val prettyDate: String = DateUtil.getPrettyDateEn(DateUtil.getPreviousDay(new Date()))

  override def getSubject: String = {s"Prosolo Notifications for $prettyDate"}
  val domain: String =System.getProperty("app.domain")
  val roleLink: String = if(role.equals(NotificationSections.MANAGE)) "manage/notifications" else "notifications"
  val notificationsLink: String =if(domain.endsWith("/")) domain+roleLink else domain+"/"+roleLink

  def hasMore(f:()=>Int):Boolean=f() > DISPLAY_NUMBER

  def hasFollowers = hasNotificationType(NotificationType.Follow_User)
  def followersCount() = getNotificationTypeCount(NotificationType.Follow_User)
  def followers(): util.List[Notification] = getNotificationsByType(NotificationType.Follow_User)
  def hasMoreFollowers():Boolean = hasMore(followersCount)

  def hasAnnouncements():Boolean= hasNotificationType(NotificationType.AnnouncementPublished)
  def announcementsCount():Int= getNotificationTypeCount(NotificationType.AnnouncementPublished)
  def announcements():java.util.List[Notification]= getNotificationsByType(NotificationType.AnnouncementPublished)
  def hasMoreAnnouncements():Boolean=hasMore(announcementsCount)

  def hasComments():Boolean= hasNotificationType(NotificationType.Comment)
  def commentsCount():Int= getNotificationTypeCount(NotificationType.Comment)
  def comments():java.util.List[Notification]= getNotificationsByType(NotificationType.Comment)
  def hasMoreComments():Boolean=hasMore(commentsCount)

  def hasAssessmentsApproved():Boolean= hasNotificationType(NotificationType.Assessment_Approved)
  def assessmentsApprovedCount():Int= getNotificationTypeCount(NotificationType.Assessment_Approved)
  def assessmentsApproved():java.util.List[Notification]={ getNotificationsByType(NotificationType.Assessment_Approved)}
  def hasMoreAssessmentsApproved:Boolean=hasMore(assessmentsApprovedCount)

  def hasAssessmentsComments():Boolean= hasNotificationType(NotificationType.Assessment_Comment)
  def assessmentsCommentsCount():Int= getNotificationTypeCount(NotificationType.Assessment_Comment)
  def assessmentsComments():java.util.List[Notification]= getNotificationsByType(NotificationType.Assessment_Comment)
  def hasMoreAssessmentsComments:Boolean=hasMore(assessmentsCommentsCount)

  def hasAssessmentsRequest():Boolean= hasNotificationType(NotificationType.Assessment_Requested)
  def assessmentsRequestsCount():Int= getNotificationTypeCount(NotificationType.Assessment_Requested)
  def assessmentsRequests():java.util.List[Notification]= getNotificationsByType(NotificationType.Assessment_Requested)
  def hasMoreAssessmentsRequests:Boolean=hasMore(assessmentsRequestsCount)

  def hasGradeAdded():Boolean= hasNotificationType(NotificationType.GradeAdded)
  def gradeAddedCount():Int={ getNotificationTypeCount(NotificationType.GradeAdded)}
  def gradesAdded():java.util.List[Notification]= getNotificationsByType(NotificationType.GradeAdded)
  def hasMoreGradesAdded:Boolean=hasMore(gradeAddedCount)

  def hasCommentLiked():Boolean= hasNotificationType(NotificationType.Comment_Like)
  def commentLikeCount():Int= getNotificationTypeCount(NotificationType.Comment_Like)
  def commentLikes():java.util.List[Notification]= getNotificationsByType(NotificationType.Comment_Like)
  def hasMoreCommentLikes:Boolean=hasMore(commentLikeCount)

  def hasSocialActivityLiked():Boolean = hasNotificationType(NotificationType.Social_Activity_Like)
  def socialActivityLikeCount():Int = getNotificationTypeCount(NotificationType.Social_Activity_Like)
  def socialActivityLike:java.util.List[Notification]=getNotificationsByType(NotificationType.Social_Activity_Like)
  def hasMoreSocialActivityLikes:Boolean=hasMore(socialActivityLikeCount)

  def hasNotificationType(notificationType:NotificationType):Boolean= notificationTypesCounts.getOrElse(notificationType.toString,0)>0
  def getNotificationTypeCount(notificationType:NotificationType):Int= notificationTypesCounts.getOrElse(notificationType.toString,0)
  def getNotificationsByType(notificationType:NotificationType):java.util.List[Notification] = notificationsByType.get(notificationType.name).get.toList
}
