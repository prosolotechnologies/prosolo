package org.prosolo.bigdata.scala.spark

import com.typesafe.config._
/**
  * Created by zoran on 05/02/17.
  */
/**
  * zoran 05/02/17
  */
object SparkApplicationConfig {
  println("SPARK SYSTEM ENV:"+System.getenv("spark-mode"))
   val env = if (System.getenv("spark-mode") == null) "local" else System.getenv("spark-mode")
//val env="spark-mode"
  println("ENV:"+env)
  val conf = ConfigFactory.load().getConfig(env)

  println("CONFIG:"+conf.toString)
 // println("CONF:"+conf.getConfig(env).toString)
 // def apply() = conf.getConfig(env)
}
