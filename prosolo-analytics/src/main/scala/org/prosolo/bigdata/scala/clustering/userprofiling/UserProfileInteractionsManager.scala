package org.prosolo.bigdata.scala.clustering.userprofiling


import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{SocialInteractionStatisticsDBManagerImpl, SocialInteractionsStatements, TablesNames}
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.common.config.CommonSettings
import org.prosolo.bigdata.spark.scala.clustering.UserProfileInteractionsSparkJob

/**
  * Created by zoran on 29/03/16.
  */
object UserProfileInteractionsManager{
  val clusteringDAOManager = new ClusteringDAOImpl
  val dbManager = SocialInteractionStatisticsDBManagerImpl.getInstance()
  //val sc = SparkContextLoader.getSC
  val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix

  def runAnalyser() = {
    println("RUN ANALYZER")
    val credentialsIds = clusteringDAOManager.getAllCredentialsIds
    val userProfileInteractionsSparkJob=new UserProfileInteractionsSparkJob(dbName)
    userProfileInteractionsSparkJob.runSparkJob(credentialsIds,dbName)
    println("FINISHED ANALYZER FOR USER PROFILE INTERACTIONS MANAGER JOB")
  }



}
