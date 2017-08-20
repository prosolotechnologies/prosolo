package org.prosolo.bigdata.scala.clustering.sna


import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl
import org.prosolo.bigdata.dal.cassandra.impl.TableNames
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileClusteringManager._
import org.prosolo.bigdata.spark.scala.clustering.SNAClusteringSparkJob
import org.prosolo.common.config.CommonSettings



/**
  * Created by zoran on 21/12/15.
  */
/**
  * zoran 21/12/15
  */
object SNAclusterManager{
  val dbManager=SocialInteractionStatisticsDBManagerImpl.getInstance()
  val dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName +
    CommonSettings.getInstance().config.getNamespaceSufix();
  println("INITIALIZED SNA CLUSTER MANAGER")

  def updateTimestamp(timestamp:Long)={
    println("UPDATE TIMESTAMP TO:"+timestamp)
    dbManager.updateCurrentTimestamp(TableNames.INSIDE_CLUSTER_INTERACTIONS,timestamp)
    dbManager.updateCurrentTimestamp(TableNames.OUTSIDE_CLUSTER_INTERACTIONS,timestamp)
    dbManager.updateCurrentTimestamp(TableNames.STUDENT_CLUSTER,timestamp)
  }


  def runClustering()={
    println("INITIALIZE USER PROFILE CLUSTERING ")
    val timestamp=System.currentTimeMillis()
    val credentialsIds=clusteringDAOManager.getAllCredentialsIds
    val sNAClusteringSparkJob=new SNAClusteringSparkJob(dbName)
    sNAClusteringSparkJob.runSparkJob(credentialsIds,dbName, timestamp)
    updateTimestamp(timestamp)
  }

}
