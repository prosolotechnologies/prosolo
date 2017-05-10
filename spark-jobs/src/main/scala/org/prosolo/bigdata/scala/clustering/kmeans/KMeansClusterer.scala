package org.prosolo.bigdata.scala.clustering.kmeans

//import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}

/**
  * Created by zoran on 19/07/16.
  */
/**
  * zoran 19/07/16
  */
object KMeansClusterer {

  /**
    * Analyses different KMeans models to find the one with lowest costs
    * @param dataFrame
    * @return
    */
  private def findBestKMeansModel(dataFrame: DataFrame,possibleNumClusters:Seq[Int],possibleMaxIterations:Seq[Int]):KMeansModel={
    //use cross validation to find best k-means cluster model based on WSSSE
    val kmeans=new KMeans()
      .setFeaturesCol("credentialsOneHotEncodedCombined")
      .setPredictionCol("clusterId")



    val results=
      for{
        numClusters <- possibleNumClusters
        maxIterations <- possibleMaxIterations
      }yield{
        kmeans.setK(numClusters)
        kmeans.setMaxIter(maxIterations)
        val kmeansModel=kmeans.fit(dataFrame)
        val WSSSE=kmeansModel.computeCost(dataFrame)
        (WSSSE, numClusters, maxIterations, kmeansModel)
      }
    val (bestWSSSE, bestNumClusters, bestMaxIterations, bestKMeansModel) = results.sortBy(_._1).toSeq(0)

    println("BEST WSSSE:"+bestWSSSE.toString)
    println("BEST NUM CLUSTERS:"+bestNumClusters)
    println("BEST MAX ITERATIONS:"+bestMaxIterations)
    bestKMeansModel
  }
  def performClustering(dataFrame: DataFrame, sqlContext:SQLContext, possibleNumClusters:Seq[Int],possibleMaxIterations:Seq[Int]): DataFrame ={
    val bestKMeansModel=findBestKMeansModel(dataFrame, possibleNumClusters, possibleMaxIterations)
    val kmeansClusters=bestKMeansModel.transform(dataFrame)
    kmeansClusters.select("*")
  }
}
