package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.scala.spark.emails.{NotificationReceiverSummary, NotificationSections, UserNotificationEmailsSparkJob}
import org.prosolo.bigdata.scala.spark.emails.{NotificationReceiverSummary, UserNotificationEmailsSparkJob}
import org.prosolo.common.config.CommonSettings

import org.slf4j.LoggerFactory

object NotificationsEmailManager {
  val logger = LoggerFactory.getLogger(getClass)
  val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix
  val clusteringDAOManager = new ClusteringDAOImpl

  def runAnalyser(date: Long) = {
    println("RUN ANALYZER")

    val sparkJob = new UserNotificationEmailsSparkJob(dbName)
    val emailService = new NotificationsEmailServiceImpl

    val roles = Array(NotificationSections.STUDENT, NotificationSections.MANAGE)
    for (role <- roles) {
      val emailBatches: Array[Array[NotificationReceiverSummary]] = sparkJob.runSparkJob(date, role)
      emailBatches.foreach {
        emailBatch =>
          val emailResults = emailService.sendEmailBatches(emailBatch)
          val success = emailResults._1
          val failure = emailResults._2
          sparkJob.addSuccessEmails(emailResults._1)
          sparkJob.addFailedEmails(emailResults._2)

      }

  def runAnalyser(date: Long) = {
    logger.debug("RUN ANALYZER")
    val sparkJob = new UserNotificationEmailsSparkJob(dbName)
    val emailService = new NotificationsEmailServiceImpl
    val emailBatches: Array[Array[NotificationReceiverSummary]] = sparkJob.runSparkJob(date)
    emailBatches.foreach {
      emailBatch =>
        val emailResults = emailService.sendEmailBatches(emailBatch)
        sparkJob.addSuccessEmails(emailResults._1)
        sparkJob.addFailedEmails(emailResults._2)
    }


    sparkJob.finishJob()
    logger.debug("FINISHED ANALYZER FOR USER NOTIFICATIONS MANAGER JOB")
  }
}
