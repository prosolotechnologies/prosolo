package org.prosolo.bigdata.scala.spark

import com.typesafe.config._
/**
  * Created by zoran on 05/02/17.
  */
/**
  * zoran 05/02/17
  */
object SparkApplicationConfig {
  val env = if (System.getenv("spark-standalone") == null) "local" else System.getenv("spark-standalone")

  val conf = ConfigFactory.load().getConfig(env)
  println("ENV:"+env)
  println("CONFIG:"+conf.toString)
 // println("CONF:"+conf.getConfig(env).toString)
 // def apply() = conf.getConfig(env)
}
