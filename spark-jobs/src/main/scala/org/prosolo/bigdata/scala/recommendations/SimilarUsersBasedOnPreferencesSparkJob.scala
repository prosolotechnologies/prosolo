package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.prosolo.bigdata.dal.cassandra.impl.{RecommendationsDAO, TablesNames}
//import org.prosolo.bigdata.es.impl.DataSearchImpl
import org.prosolo.bigdata.scala.clustering.kmeans.KMeansClusterer
//import org.prosolo.bigdata.scala.es.RecommendationsESIndexer
import org.prosolo.bigdata.scala.spark.SparkContextLoader

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by zoran on 29/04/17.
  */
object SimilarUsersBasedOnPreferencesSparkJob{
  val sc = SparkContextLoader.getSC
  sc.setLogLevel("WARN")
  val sqlContext = SQLContext.getOrCreate(sc)
  println("CREATED SQL CONTEXT")
  /**
    * Performs users clustering, in order to limit data model loading to one specific cluster only.
    * Users are clustered based on the credentials they are assigned to
    */
  def runKmeans(totalNumberOfUsers:Long, keyspaceName:String, possibleMaxIterations:Seq[Int], clusterAproxSize:Int): Unit ={
    val possibleNumClusters=getMinNumClusters(totalNumberOfUsers, clusterAproxSize)

    val (usersWithCredentialsDF, usersWithExplodedCredentials)= UserFeaturesDataManager.prepareUsersCredentialDataFrame(sqlContext,keyspaceName)
    val resultsDF= FeaturesBuilder.buildAndTransformPipelineModel(usersWithExplodedCredentials)
    val joinedResults=UserFeaturesDataManager.combineUserCredentialVectors(sqlContext, resultsDF, usersWithCredentialsDF)
    val clusteringResults= KMeansClusterer.performClustering(joinedResults,sqlContext, possibleNumClusters, possibleMaxIterations)
    UserFeaturesDataManager.interpretKMeansClusteringResults(sqlContext,clusteringResults,keyspaceName)
  }
  /**
    * Calculates what are the boundaries for the number of clusters
    * @return
    */
  private def getMinNumClusters(totalNumberOfUsers:Long,clusterAproxSize:Int)={
    val maxNumber:Int= (totalNumberOfUsers/clusterAproxSize).toInt
    val multiplicator:Int=if(maxNumber>20) maxNumber/10 else 1
    val minNumber:Int=if(maxNumber>5) maxNumber-5*multiplicator else 1
    println("MIN:"+minNumber+" MAX:"+maxNumber+" multiplicator:"+multiplicator)
    Seq(minNumber, maxNumber)
  }
  /**
    * Performs recommendation of similar users based on their similarity using Spark ML ALS and cosine similarity
    */
  def runALSUserRecommender(clusterAproxSize:Int,keyspaceName:String,indexRecommendationDataName:String, similarUsersIndexType:String): Unit ={
    val clustersUsers =UserFeaturesDataManager.loadUsersInClusters(sqlContext,keyspaceName).collect()
    clustersUsers.foreach {
      row: Row =>
        println("RUN ALS ON ROW:"+row.toString())
        ALSUserRecommender.processClusterUsers(sc,row.getLong(0), row.getList[Long](1).toList, clusterAproxSize, keyspaceName,indexRecommendationDataName,similarUsersIndexType)
    }
    println("runALSUserRecommender finished")
  }
  def createOneCluster(keyspaceName:String)= {
    println("CREATE ONE CLUSTER ONLY")
    println("KEYSPACE:"+keyspaceName+" ")
    if(sqlContext==null)println("SQL CONTEXT IS NULL")
    import sqlContext.implicits._
    val usersInTheSystemDF: DataFrame = sqlContext.read.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspaceName,
      "table" -> TablesNames.USER_COURSES)).load()
    usersInTheSystemDF.show
    val clusterUsers:java.util.List[java.lang.Long] = usersInTheSystemDF.select("userid").map(row => {
      println("MAPPING ROW:"+row.toString())
      val id = row.getLong(0).asInstanceOf[java.lang.Long]
      id
    }).collect().toList.asJava
    val clusterId:Long=0
    //println("TEMPORARY DISABLED")
    val recommendationsDAO=new RecommendationsDAO(keyspaceName)

    recommendationsDAO.insertClusterUsers(clusterId,clusterUsers)
    //UserRecommendationsDBManagerImpl.getInstance().insertClusterUsers(clusterId,clusterUsers)

  }

}
