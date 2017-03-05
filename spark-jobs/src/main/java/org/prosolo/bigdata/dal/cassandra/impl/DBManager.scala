package org.prosolo.bigdata.dal.cassandra.impl

import com.datastax.spark.connector.cql.{CassandraConnector, Schema}
import org.apache.spark.SparkConf
import org.prosolo.bigdata.scala.spark.SparkContextLoader

/**
  * Created by zoran on 04/03/17.
  */
/**
  * zoran 04/03/17
  */
object DBManager {

  val sc=SparkContextLoader.getSC
  val sparkConf=SparkContextLoader.sparkConf
  val connector=CassandraConnector(sparkConf)
  lazy val schema = Schema.fromCassandra(connector)

}
