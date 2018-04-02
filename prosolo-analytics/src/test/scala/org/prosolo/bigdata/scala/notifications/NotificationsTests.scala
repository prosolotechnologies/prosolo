package org.prosolo.bigdata.scala.notifications

import org.prosolo.bigdata.email.EmailSender
import org.prosolo.bigdata.scala.emails.NotificationsEmailServiceImpl
import org.prosolo.bigdata.scala.spark.emails.{ NotificationReceiverSummary, NotificationsSummary, Receiver}
import org.prosolo.common.domainmodel.user.notifications.NotificationType
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.immutable.HashMap

class NotificationsTests extends FunSuite with BeforeAndAfter{
  var notificationsSummary:NotificationsSummary=_
  var notificationReceiverSummary:NotificationReceiverSummary=_
  before{
    var notificationTypesCounts:HashMap[String,Int]=new HashMap[String,Int]
    val receiverId=1
    val total=15
    notificationTypesCounts+=(NotificationType.Comment.toString->4)
    notificationTypesCounts+=(NotificationType.Comment_Like.toString->3)
    notificationTypesCounts+=(NotificationType.Follow_User.toString->2)
   // val notification1=Notification(1234,NotificationType.Follow_User.name(),123,"Follower 1",123,"zoran.jeremic@gmail.com","xxxxx","yyyy",
   // "","Erika Ames",111)
    //val notification2=Notification(1234,NotificationType.Follow_User.name(),123,"Follower 1",123,"zoran.jeremic@gmail.com","xxxxx","yyyy",
     // "","Erika Ames",111)
   // val notifications=Array(notification1,notification2)
  //  var notificationsMap=new HashMap[String,Array[Notification]]
  //  notificationsMap+=(NotificationType.Follow_User.name()->notifications)

    // notificationsSummary=NotificationsSummary(receiverId:Long, total:Int, notificationTypesCounts:HashMap[String,Int],notificationsMap)

    val receiver=Receiver(2,"Zoran Jeremic","zoran.jeremic@gmail.com")
    notificationReceiverSummary=NotificationReceiverSummary(receiver,notificationsSummary)

  }
  test("Sending email digest notification for one user"){
    val emailService=new NotificationsEmailServiceImpl

    val emailSender=new EmailSender
    emailSender.sendEmail(emailService.createEmailGenerator(notificationReceiverSummary),"zoran.jeremic@gmail.com")

  }

}
