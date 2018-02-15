package org.prosolo.bigdata.scala.notifications

import org.prosolo.bigdata.email.EmailSender
import org.prosolo.bigdata.scala.emails.NotificationsEmailServiceImpl
import org.prosolo.bigdata.scala.spark.emails.{Notification, NotificationsSummary}
import org.prosolo.common.domainmodel.user.notifications.NotificationType
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.immutable.HashMap

class NotificationsTests extends FunSuite with BeforeAndAfter{
  var notificationsSummary:NotificationsSummary=null
  before{
    var notificationTypesCounts:HashMap[String,Int]=new HashMap[String,Int]
    val receiver=1
    val total=15
    notificationTypesCounts+=(NotificationType.Comment.toString->4)
    notificationTypesCounts+=(NotificationType.Comment_Like.toString->3)
    notificationTypesCounts+=(NotificationType.Follow_User.toString->2)
     notificationsSummary=new NotificationsSummary(receiver:Long, total:Int, notificationTypesCounts:HashMap[String,Int],new HashMap[String,Array[Notification]])
  }
  test("Sending email digest notification for one user"){
    val emailService=new NotificationsEmailServiceImpl

    val emailSender=new EmailSender
    emailSender.sendEmail(emailService.createEmailGenerator(notificationsSummary),"zoran.jeremic@gmail.com")

  }

}
