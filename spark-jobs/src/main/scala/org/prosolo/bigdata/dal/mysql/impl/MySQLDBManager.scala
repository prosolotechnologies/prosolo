package org.prosolo.bigdata.dal.mysql.impl

import com.typesafe.config.{Config, ConfigFactory}
import org.prosolo.bigdata.scala.spark.SparkManager
import org.apache.spark.sql._
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext, sql}

object MySQLDBManager {
  val conf:Config=ConfigFactory.load();
  val dbHost =conf.getString("mysql.dbHost")
  val dbName = conf.getString("mysql.dbName")
  val dbPort = conf.getString("mysql.dbPort")
  val dbUser = conf.getString("mysql.dbUser")
  val dbPass = conf.getString("mysql.dbPass")
  val url="jdbc:mysql://"+dbHost+":3306/"+dbName;
  val sparkSession:SparkSession=SparkManager.sparkContextLoader.getSparkSession
  def createDataFrame(tableName:String,sql:String): DataFrame ={
    var jdbcDF = sparkSession.read
      .format("jdbc")
      .option("url", url)
      .option("dbtable", tableName)
            .option("user", dbUser)
             .option("password", dbPass)
      .load()

    jdbcDF.createOrReplaceTempView(tableName)
    sparkSession.sql(sql)
  }
}
