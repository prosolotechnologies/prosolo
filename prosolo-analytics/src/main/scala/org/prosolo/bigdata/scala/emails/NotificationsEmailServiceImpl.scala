package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.email.EmailSender
import org.prosolo.bigdata.scala.spark.emails.{NotificationsSummary}
import org.prosolo.common.email.generators.EmailContentGenerator

import collection.JavaConverters._
class NotificationsEmailServiceImpl extends EmailService[NotificationsSummary] {
  override def sendEmail(emailSummary:NotificationsSummary): Unit = ???

  def createEmailGenerator(notificationSummary:NotificationsSummary):EmailContentGenerator={
    val notificationEmailGenerator=new NotificationsDigestEmailGenerator("Zoran",notificationSummary.total,notificationSummary.notificationTypesCounts)

    notificationEmailGenerator
  }

  override def sendEmailBatches(batchEmails:Array[NotificationsSummary]): Unit = {
   val emailsToSend:Map[EmailContentGenerator,String]= batchEmails.toStream.map{
      emailSummary=>{
        println("BATCH:"+emailSummary)
        (createEmailGenerator(emailSummary),"zoran.jeremic@gmail.com")
      }
    }.toMap//.asInstanceOf[java.util.Map[EmailContentGenerator,String]]
    emailsToSend.foreach{
      case(emailContentGenerator, email)=>
        println("EMAIL:"+email)
    }
    val emailSender=new EmailSender
    println("EMAIL SENDER SENDING")
    emailSender.sendBatchEmails(emailsToSend.asJava)
    println("FINISHED SENDING")
  }
}
