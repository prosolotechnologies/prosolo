package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.email.EmailSender
import org.prosolo.bigdata.scala.spark.emails.{NotificationReceiverSummary}
import org.prosolo.common.email.generators.EmailContentGenerator

import collection.JavaConverters._
class NotificationsEmailServiceImpl extends EmailService[NotificationReceiverSummary] {
  override def sendEmail(emailSummary:NotificationReceiverSummary): Unit = ???

  def createEmailGenerator(notificationReceiverSummary:NotificationReceiverSummary):EmailContentGenerator={
    val nSummary=notificationReceiverSummary.summary
    val nReceiver=notificationReceiverSummary.receiver
    val notificationEmailGenerator=
      new NotificationsDigestEmailGenerator(nReceiver.fullname,nSummary.total,nSummary.notificationTypesCounts,nSummary.notificationsByType)

    notificationEmailGenerator
  }

  override def sendEmailBatches(batchEmails:Array[NotificationReceiverSummary]): Unit = {
   val emailsToSend:Map[EmailContentGenerator,String]= batchEmails.toStream.map{
      emailSummary=>{
        println("BATCH:"+emailSummary)
        (createEmailGenerator(emailSummary),"zoran.jeremic@gmail.com")
      }
    }.toMap//.asInstanceOf[java.util.Map[EmailContentGenerator,String]]
    val emailSender=new EmailSender
        emailSender.sendBatchEmails(emailsToSend.asJava)
    println("FINISHED SENDING EMAILS")
  }
}
