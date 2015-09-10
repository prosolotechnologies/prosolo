package org.prosolo.bigdata.scala.spark

import org.apache.spark.{SparkConf, SparkContext}

/**
 * @author zoran Jul 28, 2015
 */
object SparkContextLoader {
  var sc: SparkContext = _
   var sparkConf: SparkConf = _
   
   

  /**
   * Return a SparkContext that has hard-coded parameters
   * for testing, ideally these should be supplied by config
   * of being reliant on parsing some SPARK_HOME/conf dir.
   * @return SparkContext .
   */
  def getSC: SparkContext = {
    if (sc == null) {
      sparkConf = new SparkConf()
      sparkConf.set("spark.scheduler.mode", "FAIR")
      sparkConf.set("spark.speculation", "true")
      sparkConf.setMaster("local[1]")
      sparkConf.setAppName("ProsoloBigDataScala")
      sparkConf.set("spark.executor.memory", "512M")
      sparkConf.set("spark.driver.memory", "1G")
      sc = new SparkContext(sparkConf)
    }
    sc
  }

}