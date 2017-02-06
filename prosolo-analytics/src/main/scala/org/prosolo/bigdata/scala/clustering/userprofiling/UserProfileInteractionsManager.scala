package org.prosolo.bigdata.scala.clustering.userprofiling

import com.datastax.driver.core.Row
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{SocialInteractionStatisticsDBManagerImpl, SocialInteractionsStatements, TablesNames}
import SocialInteractionsStatements._
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.common.config.CommonSettings

import scala.collection.JavaConverters._
import play.api.libs.json.Json
import com.datastax.spark.connector._
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

     UserProfileInteractionsSparkJob.runSparkJob(credentialsIds,dbName)

   // UserProfileInteractionsSparkJob.runSparkJob(credentialsRDD, dbName, sc)
    /*credentialsRDD.foreach { credentialid =>
      println("RUNNING USER PROFILE ANALYZER FOR credential:" + credentialid)
      runStudentInteractionsGeneralOverviewAnalysis(credentialid)
      runStudentInteractionsByTypeOverviewAnalysis(credentialid)
      println("FINISHED ANALYZER FOR credential:"+credentialid)
    }*/

    println("FINISHED ANALYZER FOR USER PROFILE INTERACTIONS MANAGER JOB")
  }



}
