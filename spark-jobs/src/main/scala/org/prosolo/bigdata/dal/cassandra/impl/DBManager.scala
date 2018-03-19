package org.prosolo.bigdata.dal.cassandra.impl

import com.datastax.spark.connector.cql.{CassandraConnector, Schema}
import org.apache.spark.sql.SparkSession
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkManager}

/**
  * Created by zoran on 04/03/17.
  */
/**
  * zoran 04/03/17
  */
object DBManager {

  //val sparkSession:SparkSession=SparkManager.sparkContextLoader.getSparkSession
 // val sc=sparkSession.sparkContext
  //val sc=SparkManager.sparkContextLoader.getSC
  val sparkConf=SparkManager.sparkContextLoader.sparkConf
  val connector=CassandraConnector(sparkConf)
  lazy val schema = Schema.fromCassandra(connector)

}
