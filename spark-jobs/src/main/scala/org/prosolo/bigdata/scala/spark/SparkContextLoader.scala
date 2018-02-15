package org.prosolo.bigdata.scala.spark

import java.util

import com.typesafe.config.{Config, ConfigList, ConfigObject}
import org.apache.commons.lang.StringUtils
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import com.typesafe.config._

import scala.collection.JavaConverters._
//import org.prosolo.bigdata.config.Settings
//import org.prosolo.common.config.{CommonSettings, ElasticSearchHost}

import scala.collection.mutable.ListBuffer

/**
 * @author zoran Jul 28, 2015
 */
class SparkContextLoader {

  /**
   * Return a SparkContext that has hard-coded parameters
   * for testing, ideally these should be supplied by config
   * of being reliant on parsing some SPARK_HOME/conf dir.
 *
   * @return SparkContext .
   */
println("Initializing SparkContextLoader")

  val numOfCores=Runtime.getRuntime.availableProcessors()
  val conf:Config=ConfigFactory.load();
 //val conf =//SparkApplicationConfig.getConf("local")
	val dbHost =conf.getString("cassandra.dbHost")
	val dbPort = conf.getString("cassandra.dbPort")
  val maxCores=conf.getString("spark.maxCores")
  val mode=conf.getString("spark.mode")
  val maxNumberCores=if(numOfCores>maxCores.toInt) maxCores else numOfCores
  val master=if(mode.equals("local")) "local["+numOfCores+"]" else conf.getString("spark.master")
  val executorMemory=conf.getString("spark.executorMemory")
  val sparkConf = new SparkConf()
  sparkConf.setMaster(master)

  sparkConf.set("spark.cores_max",maxNumberCores.toString)
  sparkConf.set("spark.executor.memory",executorMemory)
  sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  sparkConf.setAppName(conf.getString("spark.applicationName"))
      //added for spark cassandra connection
  sparkConf.set("spark.cassandra.connection.host", dbHost)
  sparkConf.set("spark.cassandra.connection.port", dbPort)
  sparkConf.set("spark.ui.port","4041")
  if(mode=="standalone"){
    sparkConf.setJars(List(conf.getString("spark.oneJar")))
  }
  addESConfig(sparkConf)
     println("SPARK CONFIG:"+sparkConf.toDebugString)
  //val sparkSession=SparkSession.builder().appName(conf.getString("spark.applicationName")).master(master).getOrCreate()
  val sparkSession:SparkSession=SparkSession.builder().config(sparkConf).getOrCreate()



 // @transient  val sc = new SparkContext(sparkConf)
val sc=sparkSession.sparkContext
  val jsc=new JavaSparkContext(sc)

  def getSC:SparkContext={
    sc
  }
  def getJSC:JavaSparkContext={
    jsc
  }
  def getSparkSession:SparkSession={
    sparkSession
  }


  def addESConfig(sparkConf:SparkConf): Unit ={
    sparkConf.set("es.index.auto.create","true")
    sparkConf.set("es.http.timeout","5m")
    sparkConf.set("es.scroll.size","50")
      val hosts=new util.LinkedList[String]
        hosts.add(conf.getString("elasticsearch.host")+":"+conf.getString("elasticsearch.port"))
      sparkConf.set("es.nodes", StringUtils.join(hosts, ","))
        sparkConf.set("es.nodes.wan.only","true")
  }

}