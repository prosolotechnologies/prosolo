package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.scala.spark.emails.NotificationsSummary
import org.prosolo.common.util.date.DateEpochUtil

import org.prosolo.bigdata.scala.spark.emails.UserNotificationEmailsSparkJob
import org.prosolo.common.config.CommonSettings

object NotificationsEmailManager {
  val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix
  val clusteringDAOManager = new ClusteringDAOImpl
  def runAnalyser() = {
    println("RUN ANALYZER")
    //val credentialsIds = clusteringDAOManager.getAllActiveDeliveriesIds
    val sparkJob=new UserNotificationEmailsSparkJob(dbName)
    val emailService=new NotificationsEmailServiceImpl
    val date=DateEpochUtil.getDaysSinceEpoch
    val emailBatches:Array[Array[NotificationsSummary]]=sparkJob.runSparkJob(date)
    println("BATCHES:"+emailBatches.mkString(","))
    println("EMAIL BATCHES:"+emailBatches.size)
    emailBatches.foreach {
      emailBatch =>
        emailService.sendEmailBatches(emailBatch)
    }
    sparkJob.finishJob()
    println("FINISHED ANALYZER FOR USER NOTIFICATIONS MANAGER JOB")
  }
}
