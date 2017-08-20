package org.prosolo.bigdata.scala.spark

import java.util

import com.typesafe.config.{ConfigList, ConfigObject}
import org.apache.commons.lang.StringUtils
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._
//import org.prosolo.bigdata.config.Settings
//import org.prosolo.common.config.{CommonSettings, ElasticSearchHost}

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
 // val dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig
  //val sparkConfig = Settings.getInstance().config.sparkConfig
  //val esConfig=CommonSettings.getInstance().config.elasticSearch

	val dbHost =SparkApplicationConfig.conf.getString("cassandra.dbHost")
  //dbConfig.dbHost
	val dbPort = SparkApplicationConfig.conf.getString("cassandra.dbPort")
  val maxCores=SparkApplicationConfig.conf.getString("spark.maxCores")
  val mode=SparkApplicationConfig.conf.getString("spark.mode")
  val maxNumberCores=if(numOfCores>maxCores.toInt) maxCores else numOfCores
  println("SPARK MODE:"+mode)
  val master=if(mode.equals("local")) "local["+numOfCores+"]" else SparkApplicationConfig.conf.getString("spark.master")
  val executorMemory=SparkApplicationConfig.conf.getString("spark.executorMemory")
 //val numOfCores=1;
  val sparkConf = new SparkConf()
  sparkConf.setMaster(master)

  sparkConf.set("spark.cores_max",maxNumberCores.toString)
  sparkConf.set("spark.executor.memory",executorMemory)
  sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  sparkConf.setAppName(SparkApplicationConfig.conf.getString("spark.applicationName"))
      //added for spark cassandra connection
  sparkConf.set("spark.cassandra.connection.host", dbHost)
  sparkConf.set("spark.cassandra.connection.port", dbPort)
  sparkConf.set("spark.ui.port","4041")
  println("MODE:"+mode)
  if(mode=="standalone"){

    sparkConf.setJars(List(SparkApplicationConfig.conf.getString("spark.oneJar")))
    println("ADDED JARS")
  }
  addESConfig(sparkConf)
      println("SPARK CONFIG:"+sparkConf.toDebugString)
  //val sparkSession=SparkSession.builder().appName(SparkApplicationConfig.conf.getString("spark.applicationName")).master(master).getOrCreate()
  val sparkSession:SparkSession=SparkSession.builder().config(sparkConf).getOrCreate()


/*

  */


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
        hosts.add("52.202.81.232:"+"9200")


      sparkConf.set("es.nodes", StringUtils.join(hosts, ","))
        sparkConf.set("es.nodes.wan.only","true")
     // sparkConf.set("es.nodes", "dev.prosolo.ca")
       //  sparkConf.set("es.port","9200")



  }

}