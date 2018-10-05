package org.prosolo.bigdata.scala.clustering.userprofiling

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.rdd.RDD
import org.joda.time.DateTime
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.jobs.GenerateUserProfileClusters
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.prosolo.bigdata.spark.scala.clustering.UserProfileClusteringSparkJob
import org.prosolo.common.config.CommonSettings
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Created by zoran on 15/12/15.
  */
/**
  * zoran 15/12/15
  */
object UserProfileClusteringManager {
  val logger = LoggerFactory.getLogger(getClass)
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

  val endDate: Date=new Date
  val DAY_IN_MS:Long=1000*60*60*24
  val jobProperties=Settings.getInstance().config.schedulerConfig.jobs.getJobConfig(classOf[GenerateUserProfileClusters].getName).jobProperties
  logger.debug("JOB PROPERTIES:"+jobProperties.toString)
  val periodToCalculate=jobProperties.getProperty("periodToCalculate").toInt;
  logger.debug("PERIOD TO CALCULATE:"+periodToCalculate)


  val numClusters=jobProperties.getProperty("numberOfClusters").toInt;
  val numFeatures=jobProperties.getProperty("numberOfFeatures").toInt;
  val startDate:Date=new Date(endDate.getTime-(periodToCalculate*DAY_IN_MS))
  val days:IndexedSeq[DateTime]=(0 until periodToCalculate).map(new DateTime(startDate).plusDays(_))
  logger.debug("NUMBER OF DAYS:"+days.length+" DAYS:"+days.mkString(","))

  val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix

  def runClustering()={
    logger.debug("INITIALIZE USER PROFILE CLUSTERING ")
    val clusteringDAOManager=new ClusteringDAOImpl
     val credentialsIds=clusteringDAOManager.getAllActiveDeliveriesIds
    //val credentialsIds=new java.util.List[Long]()
val userProfileClusteringSparkJob=new UserProfileClusteringSparkJob(dbName, numFeatures,numClusters)
    userProfileClusteringSparkJob.runSparkJob(credentialsIds,dbName, days,numClusters,numFeatures)
    userProfileClusteringSparkJob.finishJob()
  }



}
