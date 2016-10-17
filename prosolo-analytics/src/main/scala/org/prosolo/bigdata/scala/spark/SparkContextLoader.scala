package org.prosolo.bigdata.scala.spark

import java.util

import org.apache.commons.lang.StringUtils
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.{SparkConf, SparkContext}
import org.prosolo.bigdata.config.Settings
import org.prosolo.common.config.{CommonSettings, ElasticSearchHost}

import scala.collection.mutable.ListBuffer

/**
 * @author zoran Jul 28, 2015
 */
object SparkContextLoader {

  /**
   * Return a SparkContext that has hard-coded parameters
   * for testing, ideally these should be supplied by config
   * of being reliant on parsing some SPARK_HOME/conf dir.
 *
   * @return SparkContext .
   */
println("Initializing SparkContextLoader")

  val numOfCores=Runtime.getRuntime.availableProcessors()
  //val numOfCores=1
  val dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig
  val sparkConfig = Settings.getInstance().config.sparkConfig
  val esConfig=CommonSettings.getInstance().config.elasticSearch

	val dbHost = dbConfig.dbHost
	val dbPort = dbConfig.dbPort
  val maxCores=if(numOfCores>sparkConfig.maxCores) sparkConfig.maxCores else numOfCores
  val master=if(sparkConfig.mode.equals("local")) "local["+numOfCores+"]" else sparkConfig.master
 //val numOfCores=1;
  val sparkConf = new SparkConf()
  sparkConf.setMaster(master)

  sparkConf.set("spark.cores_max",maxCores.toString)
  sparkConf.set("spark.executor.memory",sparkConfig.executorMemory)
  sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  sparkConf.setAppName(sparkConfig.appName)
      //added for spark cassandra connection
  sparkConf.set("spark.cassandra.connection.host", dbHost)
  sparkConf.set("spark.cassandra.connection.port", dbPort.toString())
  addESConfig(sparkConf)
      println("SPARK CONFIG:"+sparkConf.toDebugString)



  val sc = new SparkContext(sparkConf)
  val jsc=new JavaSparkContext(sc)
  def getSC:SparkContext={
    sc
  }
  def getJSC:JavaSparkContext={
    jsc
  }
  def addESConfig(sparkConf:SparkConf): Unit ={
    sparkConf.set("es.index.auto.create","true")
    sparkConf.set("es.http.timeout","5m")
    sparkConf.set("es.scroll.size","50")

    if(esConfig.`type`.equals("local")){

    }else if(esConfig.`type`.equals("server")){
      val esHosts: util.ArrayList[ElasticSearchHost]=esConfig.esHostsConfig.esHosts
      val hosts=new util.LinkedList[String]
      val hostit=esHosts.iterator()
      while(hostit.hasNext()){
        val host=hostit.next()
        hosts.add(host.host+":"+"9200")
      }

      sparkConf.set("es.nodes", StringUtils.join(hosts, ","))
        sparkConf.set("es.nodes.wan.only","true")
     // sparkConf.set("es.nodes", "dev.prosolo.ca")
       //  sparkConf.set("es.port","9200")

    }else if(esConfig.`type`.equals("cloud-aws")){

    }

  }

}