package org.prosolo.bigdata.scala.clustering

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

/**
  * Created by Zoran on 06/12/15.
  */
/**
  * Zoran 06/12/15
  */
object ClusteringUtils {
  val numFeatures = 12

  val clustersDir = "clustersdir"
  val vectorsDir = clustersDir + "/users"
  val outputDir: String = clustersDir + "/output"
  val output = new Path(outputDir)
  val conf = new Configuration()
  val fs = FileSystem.get(conf)
  val datapath = new Path(vectorsDir + "/part-00000")
  val algorithmType: AlgorithmType.AlgorithmType = AlgorithmType.KMeans
  val numClusters = 4
  val periodDays=7
}
