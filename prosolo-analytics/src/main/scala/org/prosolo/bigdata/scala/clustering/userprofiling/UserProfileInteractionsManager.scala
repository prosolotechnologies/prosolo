package org.prosolo.bigdata.scala.clustering.userprofiling


import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{SocialInteractionStatisticsDBManagerImpl, SocialInteractionsStatements, TablesNames}
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.common.config.CommonSettings
import org.prosolo.bigdata.spark.scala.clustering.UserProfileInteractionsSparkJob
import org.slf4j.LoggerFactory

/**
  * Created by zoran on 29/03/16.
  */
object UserProfileInteractionsManager{
  val logger = LoggerFactory.getLogger(getClass)
  val dbManager = SocialInteractionStatisticsDBManagerImpl.getInstance()
    val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix

  def runAnalyser() = {
    logger.debug("RUN ANALYZER")
    val clusteringDAOManager = new ClusteringDAOImpl
    val credentialsIds = clusteringDAOManager.getAllActiveDeliveriesIds
    val userProfileInteractionsSparkJob=new UserProfileInteractionsSparkJob(dbName)
    userProfileInteractionsSparkJob.runSparkJob(credentialsIds,dbName)
    logger.debug("FINISHED ANALYZER FOR USER PROFILE INTERACTIONS MANAGER JOB")
  }



}
