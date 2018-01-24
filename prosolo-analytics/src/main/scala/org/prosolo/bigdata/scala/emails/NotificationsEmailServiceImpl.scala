package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.email.EmailSender
import org.prosolo.bigdata.scala.spark.emails.{EmailSummary, NotificationsSummary}
import org.prosolo.common.email.generators.EmailContentGenerator
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions._
import collection.JavaConverters._
class NotificationsEmailServiceImpl extends EmailService[NotificationsSummary] {
  override def sendEmail(emailSummary:NotificationsSummary): Unit = ???

  def createEmailGenerator(notificationSummary:NotificationsSummary):EmailContentGenerator={
    val notificationEmailGenerator=new NotificationsDigestEmailGenerator("Zoran",notificationSummary.total,notificationSummary.notificationTypesCounts)

    notificationEmailGenerator
  }

  override def sendEmailBatches(batchEmails:Array[NotificationsSummary]): Unit = {
   val emailsToSend= batchEmails.toStream.map{
      emailSummary=>{
        (createEmailGenerator(emailSummary),"zoran.jeremic@gmail.com")
      }
    }.toMap//.asInstanceOf[java.util.Map[EmailContentGenerator,String]]
    val emailSender=new EmailSender
    emailSender.sendBatchEmails(emailsToSend.asJava)
  }
}
