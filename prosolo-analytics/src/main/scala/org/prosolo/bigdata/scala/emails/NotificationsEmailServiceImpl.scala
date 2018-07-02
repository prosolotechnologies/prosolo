package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.email.EmailSender
import org.prosolo.bigdata.scala.spark.emails.{EmailSuccess, NotificationReceiverSummary}
import org.prosolo.common.email.generators.EmailContentGenerator

import collection.JavaConverters._
import scala.collection.mutable
class NotificationsEmailServiceImpl extends EmailService[NotificationReceiverSummary] {
  override def sendEmail(emailSummary:NotificationReceiverSummary): Unit = ???

  def createEmailGenerator(notificationReceiverSummary:NotificationReceiverSummary):EmailContentGenerator={
    val nSummary=notificationReceiverSummary.summary
    val nReceiver=notificationReceiverSummary.receiver
    val notificationEmailGenerator=
      new NotificationsDigestEmailGenerator(nReceiver.fullname,nSummary.total,nSummary.notificationTypesCounts,nSummary.notificationsByType)

    notificationEmailGenerator
  }

  override def sendEmailBatches(batchEmails:Array[NotificationReceiverSummary]): Tuple2[mutable.Map[String,EmailSuccess],mutable.Map[String,EmailSuccess]] = {
   val emailsToSend:Map[EmailContentGenerator,String]= batchEmails.toStream.map{
      emailSummary=>{
        logger.debug("BATCH:"+emailSummary)
        (createEmailGenerator(emailSummary),emailSummary.receiver.email)
      }
    }.toMap
    val emailSender=new EmailSender
       val success= emailSender.sendBatchEmails(emailsToSend.asJava)


    (success.getFirst.asScala,success.getSecond.asScala)
  }
}
