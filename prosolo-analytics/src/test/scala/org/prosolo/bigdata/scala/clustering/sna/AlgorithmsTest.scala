package org.prosolo.bigdata.scala.clustering.sna


import org.junit.Test
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileInteractionsManager
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.common.config.CommonSettings
import com.datastax.spark.connector._

/**
  * Created by zoran on 15/06/16.
  */
class AlgorithmsTest {
  @Test def testUserProfileGenerating(){
    println("RUN ALGORITHM")
    UserProfileInteractionsManager.runAnalyser()
    println("FINISHED PROFILER")
  }
  @Test def testCassandraConnector(): Unit ={
    val sc = SparkContextLoader.getSC
    val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix
    val rdd=sc.cassandraTable(dbName, "sna_socialinteractionscount")
      .select("course","source","target","count").where("course=?",1)
    println("COUNT:"+rdd.count())
    rdd.toArray.foreach(println)




  }
}
