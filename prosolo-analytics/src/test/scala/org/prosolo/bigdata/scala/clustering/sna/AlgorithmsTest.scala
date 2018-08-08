package org.prosolo.bigdata.scala.clustering.sna


import org.junit.Test
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileInteractionsManager
import org.prosolo.bigdata.scala.spark.SparkManager
import org.prosolo.common.config.CommonSettings
import com.datastax.spark.connector._
import org.prosolo.bigdata.dal.cassandra.impl.TablesNames
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.slf4j.LoggerFactory

/**
  * Created by zoran on 15/06/16.
  */
class AlgorithmsTest {
  val logger = LoggerFactory.getLogger(getClass)
  @Test def testUserProfileGenerating(){
    logger.debug("RUN ALGORITHM")
    UserProfileInteractionsManager.runAnalyser()
    logger.debug("FINISHED PROFILER")
  }
  @Test def testCassandraConnector(): Unit ={
    val sc = SparkManager.sparkContextLoader.getSC
    val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix
    val rdd=sc.cassandraTable(dbName, TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT )
      .select("course","source","target","count").where("course=?",1)
    logger.debug("COUNT:"+rdd.count())
    //rdd.toArray.foreach(logger.debug)




  }
}
