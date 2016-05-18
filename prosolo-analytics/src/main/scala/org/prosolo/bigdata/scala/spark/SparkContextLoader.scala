package org.prosolo.bigdata.scala.spark

import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.{SparkConf, SparkContext}
import org.prosolo.bigdata.config.Settings
import org.prosolo.common.config.CommonSettings

/**
 * @author zoran Jul 28, 2015
 */
object SparkContextLoader {
  //var sc: SparkContext = _
  // var sparkConf: SparkConf = _
  /*var hasSC = false
  var sc:Any = 0
  var test:String="any"
*/

  /**
   * Return a SparkContext that has hard-coded parameters
   * for testing, ideally these should be supplied by config
   * of being reliant on parsing some SPARK_HOME/conf dir.
 *
   * @return SparkContext .
   */


  val numOfCores=Runtime.getRuntime.availableProcessors()
  //val numOfCores=2
  val dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig
  val sparkConfig = Settings.getInstance().config.sparkConfig

	val dbHost = dbConfig.dbHost
	val dbPort = dbConfig.dbPort
  val maxCores=if(numOfCores>sparkConfig.maxCores) sparkConfig.maxCores else numOfCores
  val master=if(sparkConfig.mode.equals("local")) "local["+numOfCores+"]" else sparkConfig.master
 //val numOfCores=1;
  val sparkConf = new SparkConf()
    .setMaster(master)

      .set("spark.cores_max",maxCores.toString)
      .set("spark.executor.memory",sparkConfig.executorMemory)
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .setAppName(sparkConfig.appName)
      //added for spark cassandra connection
      .set("spark.cassandra.connection.host", dbHost)
      .set("spark.cassandra.connection.port", dbPort.toString())

  val sc = new SparkContext(sparkConf)
  val jsc=new JavaSparkContext(sc)
  def getSC:SparkContext={
    sc
  }
  def getJSC:JavaSparkContext={
    jsc
  }

}