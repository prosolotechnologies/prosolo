package org.prosolo.bigdata.scala.clustering

import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl
import com.datastax.driver.core.Row

import org.apache.mahout.math.{ DenseVector, NamedVector, VectorWritable }
import org.apache.hadoop.io.{ SequenceFile, Text }
import org.apache.mahout.common.HadoopUtil
import org.apache.mahout.common.distance.CosineDistanceMeasure
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.apache.mahout.clustering.kmeans.{KMeansDriver, RandomSeedGenerator, Kluster}
import org.apache.mahout.clustering.Cluster

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ Path, FileSystem }
import org.apache.hadoop.io.SequenceFile


import scala.collection.mutable.{ Buffer, ListBuffer }
import scala.collection.JavaConverters._

/**
 * @author zoran October 24, 2015
 */
object UsersKMeansClustering extends App {
  val dbManager = new UserObservationsDBManagerImpl()
   val clustersDir="clustersdir"
  val vectorsDir = clustersDir+"/users";
  val outputDir:String=clustersDir+"/output";
 
     val conf = new Configuration()
    val fs = FileSystem.get(conf)
    val datapath = new Path(vectorsDir + "/part-00000")
     val numClusters=4

  def initData() = {
    val vectors = new ListBuffer[NamedVector]
    val rows: java.util.List[Row] = dbManager.findAllUsersObservationsForDate(16366)

    rows.asScala.foreach { row =>
      //creating dense vector for each user observation
      val dv = new DenseVector(Array[Double](
        row.getLong(2).toDouble,
        row.getLong(3).toDouble,
        row.getLong(4).toDouble,
        row.getLong(5).toDouble))
      //creating named vector by user id
      vectors += (new NamedVector(dv, row.getLong(1).toString()))
    }
    //storing input vectors to hdfs
    
    
    val writer = new SequenceFile.Writer(fs, conf, datapath, classOf[Text], classOf[VectorWritable])
    val vec = new VectorWritable()
    vectors.foreach { vector =>
      vec.set(vector)
      writer.append(new Text(vector.getName), vec)
    }
    writer.close()
    //reading for test only. should remove this
    val reader = new SequenceFile.Reader(fs, datapath, conf)

    val key = new Text()
    val value = new VectorWritable()
    while (reader.next(key, value)) {
      println("INPUT:" + s"$key--- ${value.get().asFormatString()}")
    }
    reader.close()
    println("Finished data initialization");
  }
  def runClustering()={
    val output=new Path(outputDir)
    HadoopUtil.delete(conf, output)
     val measure = new CosineDistanceMeasure()
     val clustersIn = new Path(output, "random-seeds")
    RandomSeedGenerator.buildRandom(conf, datapath, clustersIn, numClusters, measure)
    val convergenceDelta=0.01
    val maxIterations=10
    KMeansDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, 0.0, true)

    val clusters = ClusterHelper.readClusters(conf, output)

    clusters.get(clusters.size - 1).asScala.foreach { cluster =>
      println(s"Cluster id:${cluster.getId} center:${cluster.getCenter.asFormatString()}")
    }
    val reader = new SequenceFile.Reader(fs,  new Path(outputDir+"/"+ Cluster.CLUSTERED_POINTS_DIR + "/part-m-0"), conf)
    val key = new org.apache.hadoop.io.IntWritable()
    val value = new WeightedPropertyVectorWritable()
    
    //if(reader==null){println("reader is null")}
      val finalClusters = new ListBuffer[String]
    while (reader.next(key,value)) {
     // println("KEY:"+value)
      println(s"$value belongs to cluster $key")
      if(!finalClusters.contains(key.toString))
      finalClusters+=key.toString()
    }
    
    reader.close()
    println("CLUSTERS:"+finalClusters) 
    
  }

  initData();
  runClustering();
}