package org.prosolo.bigdata.scala.emails


import java.util
import java.util.Date

import org.prosolo.bigdata.scala.spark.emails.{Notification, NotificationSections}
import org.prosolo.bigdata.scala.spark.emails.Notification
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.prosolo.common.domainmodel.user.notifications.NotificationType
import org.prosolo.common.email.generators.EmailContentGenerator
import org.prosolo.common.util.date.DateUtil

import org.slf4j.LoggerFactory

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
  val logger = LoggerFactory.getLogger(getClass)
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

  def hasAcceptedAssessmentRequests():Boolean= hasNotificationType(NotificationType.ASSESSMENT_REQUEST_ACCEPTED)
  def acceptedAssessmentRequestsCount():Int= getNotificationTypeCount(NotificationType.ASSESSMENT_REQUEST_ACCEPTED)
  def acceptedAssessmentRequests():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSESSMENT_REQUEST_ACCEPTED)
  def hasMoreAcceptedAssessmentRequests:Boolean=hasMore(acceptedAssessmentRequestsCount)

  def hasDeclinedAssessmentRequests():Boolean= hasNotificationType(NotificationType.ASSESSMENT_REQUEST_DECLINED)
  def declinedAssessmentRequestsCount():Int= getNotificationTypeCount(NotificationType.ASSESSMENT_REQUEST_DECLINED)
  def declinedAssessmentRequests():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSESSMENT_REQUEST_DECLINED)
  def hasMoreDeclinedAssessmentRequests:Boolean=hasMore(declinedAssessmentRequestsCount)

  def hasWithdrawnAssessments():Boolean= hasNotificationType(NotificationType.ASSESSOR_WITHDREW_FROM_ASSESSMENT)
  def withdrawnAssessmentsCount():Int= getNotificationTypeCount(NotificationType.ASSESSOR_WITHDREW_FROM_ASSESSMENT)
  def withdrawnAssessments():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSESSOR_WITHDREW_FROM_ASSESSMENT)
  def hasMoreWithdrawnAssessments:Boolean=hasMore(withdrawnAssessmentsCount)

  def hasAssignedAssessors():Boolean= hasNotificationType(NotificationType.ASSESSOR_ASSIGNED_TO_ASSESSMENT)
  def assignedAssessorsCount():Int= getNotificationTypeCount(NotificationType.ASSESSOR_ASSIGNED_TO_ASSESSMENT)
  def assignedAssessors():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSESSOR_ASSIGNED_TO_ASSESSMENT)
  def hasMoreAssignedAssessors:Boolean=hasMore(assignedAssessorsCount)

  def hasAssignedAssessments():Boolean= hasNotificationType(NotificationType.ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR)
  def assignedAssessmentsCount():Int= getNotificationTypeCount(NotificationType.ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR)
  def assignedAssessments():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR)
  def hasMoreAssignedAssessments:Boolean=hasMore(assignedAssessmentsCount)

  def hasExpiredAssessments():Boolean= hasNotificationType(NotificationType.ASSESSMENT_REQUEST_EXPIRED)
  def expiredAssessmentsCount():Int= getNotificationTypeCount(NotificationType.ASSESSMENT_REQUEST_EXPIRED)
  def expiredAssessments():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSESSMENT_REQUEST_EXPIRED)
  def hasMoreExpiredAssessments:Boolean=hasMore(expiredAssessmentsCount)

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

  def hasAssessmentTokensUpdated():Boolean= hasNotificationType(NotificationType.ASSESSMENT_TOKENS_NUMBER_UPDATED)
  def assessmentTokensUpdatedCount():Int= getNotificationTypeCount(NotificationType.ASSESSMENT_TOKENS_NUMBER_UPDATED)
  def assessmentTokensUpdated():java.util.List[Notification]= getNotificationsByType(NotificationType.ASSESSMENT_TOKENS_NUMBER_UPDATED)
  def hasMoreAssessmentTokensUpdated:Boolean=hasMore(assessmentTokensUpdatedCount)

  def hasNotificationType(notificationType:NotificationType):Boolean= notificationTypesCounts.getOrElse(notificationType.toString,0)>0
  def getNotificationTypeCount(notificationType:NotificationType):Int= notificationTypesCounts.getOrElse(notificationType.toString,0)
  def getNotificationsByType(notificationType:NotificationType):java.util.List[Notification] = notificationsByType.get(notificationType.name).get.toList
}
