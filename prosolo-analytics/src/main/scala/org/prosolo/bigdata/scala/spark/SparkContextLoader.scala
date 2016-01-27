package org.prosolo.bigdata.scala.spark

import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.{SparkConf, SparkContext}

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
  val sparkConf = new SparkConf()
    .setMaster("local["+numOfCores+"]")
      .set("spark.cores_max","4")
      .set("spark.executor.memory","4g")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .setAppName("prosolo.bigdata")

  val sc = new SparkContext(sparkConf)
  val jsc=new JavaSparkContext(sc)
  def getSC:SparkContext={
    println("GET-SPARK CONTEXT SCALA")
    sc
  }
  def getJSC:JavaSparkContext={
    println("GET-SPARK CONTEXT JAVA")
    jsc
  }

}