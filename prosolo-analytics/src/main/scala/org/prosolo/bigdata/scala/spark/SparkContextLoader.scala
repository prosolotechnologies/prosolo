package org.prosolo.bigdata.scala.spark

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
   * @return SparkContext .
   */
  /*def getSC: SparkContext = {
    if (sc == null) {
      println("INIT-SPARK CONTEXT SCALA")
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
  }*/
  /*def getSC:SparkContext={
    if (!hasSC) {
      println("INIT-SPARK CONTEXT SCALA:"+test)
      val workers=1
      val sparkConf = new SparkConf().setMaster("local[" + workers + "]").setAppName("SparkApp")
      sc = new SparkContext(sparkConf)
      hasSC = true
      test="some"
    }

    return sc.asInstanceOf[SparkContext]
  }*/
  val sparkConf = new SparkConf().setMaster("local[1]").setAppName("SparkApp")

  val sc = new SparkContext(sparkConf)
  def getSC:SparkContext={
    println("INIT-SPARK CONTEXT SCALA:")
    sc
  }

}