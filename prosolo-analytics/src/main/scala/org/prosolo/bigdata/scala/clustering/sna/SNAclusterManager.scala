package org.prosolo.bigdata.scala.clustering.sna


import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl
import org.prosolo.bigdata.dal.cassandra.impl.TableNames
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileClusteringManager._
import org.prosolo.bigdata.scala.messaging.BroadcastDistributer.getClass
import org.prosolo.bigdata.spark.scala.clustering.SNAClusteringSparkJob
import org.prosolo.common.config.CommonSettings
import org.slf4j.LoggerFactory



/**
  * Created by zoran on 21/12/15.
  */
/**
  * zoran 21/12/15
  */
object SNAclusterManager{
  val dbManager=SocialInteractionStatisticsDBManagerImpl.getInstance()
  val dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName+CommonSettings.getInstance().config.getNamespaceSufix();
  val logger = LoggerFactory.getLogger(getClass)
  logger.debug("INITIALIZED SNA CLUSTER MANAGER")

  def updateTimestamp(timestamp:Long)={
    logger.debug("UPDATE TIMESTAMP TO:"+timestamp)
    dbManager.updateCurrentTimestamp(TableNames.INSIDE_CLUSTER_INTERACTIONS,timestamp)
    dbManager.updateCurrentTimestamp(TableNames.OUTSIDE_CLUSTER_INTERACTIONS,timestamp)
    dbManager.updateCurrentTimestamp(TableNames.STUDENT_CLUSTER,timestamp)
  }


  def runClustering()={
    logger.debug("INITIALIZE USER PROFILE CLUSTERING ")
    val timestamp=System.currentTimeMillis()
    val clusteringDAO=new ClusteringDAOImpl();
    val deliveriesIds=clusteringDAO.getAllActiveDeliveriesIds
    val sNAClusteringSparkJob=new SNAClusteringSparkJob(dbName)
    sNAClusteringSparkJob.runSparkJob(deliveriesIds,dbName, timestamp)
    updateTimestamp(timestamp)
  }

}
