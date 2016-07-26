package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.mllib.linalg.{SparseVector, Vectors}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.prosolo.bigdata.scala.clustering.kmeans.KMeansClusterer
import org.prosolo.bigdata.scala.spark.SparkContextLoader

import scala.collection.JavaConversions._

/**
  * Created by zoran on 23/07/16.
  */
/**
  * zoran 23/07/16
  */
object SimilarUsersBasedOnPreferences extends App {
println("FIND SIMILAR USERS BASED ON PREFERENCES")
  val sc = SparkContextLoader.getSC
  sc.setLogLevel("WARN")
  val sqlContext = SQLContext.getOrCreate(sc)

  /**
    * Performs users clustering, in order to limit data model loading to one specific cluster only.
    * Users are clustered based on the credentials they are assigned to
    */
  def runKmeans(): Unit ={
    val possibleNumClusters=Seq(5,10)
    val possibleMaxIterations=Seq(15,20)
    val (usersWithCredentialsDF, usersWithExplodedCredentials)= UserFeaturesDataManager.prepareUsersCredentialDataFrame(sqlContext)
    val resultsDF= FeaturesBuilder.buildAndTransformPipelineModel(usersWithExplodedCredentials)
    val joinedResults=UserFeaturesDataManager.combineUserCredentialVectors(sqlContext, resultsDF, usersWithCredentialsDF)
    val clusteringResults= KMeansClusterer.performClustering(joinedResults,sqlContext, possibleNumClusters, possibleMaxIterations)
    UserFeaturesDataManager.interpretKMeansClusteringResults(sqlContext,clusteringResults)
  }

  /**
    * Performs recommendation of similar users based on their similarity using Spark ML ALS and cosine similarity
    */
  def runALSUserRecommender(): Unit ={
    val clustersUsers =UserFeaturesDataManager.loadUsersInClusters(sqlContext)
    clustersUsers.foreach {
      row: Row =>
        ALSUserRecommender.processClusterUsers(sc,row.getLong(0), row.getList[Long](1).toList)
    }
  }

  //runKmeans()
  runALSUserRecommender()
}
