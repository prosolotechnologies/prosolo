package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.sql.{DataFrame, Row}
import org.prosolo.bigdata.dal.cassandra.impl.{RecommendationsDAO, TablesNames}
import org.prosolo.bigdata.scala.spark.SparkJob
import org.prosolo.bigdata.scala.clustering.kmeans.KMeansClusterer
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by zoran on 29/04/17.
  */
class SimilarUsersBasedOnPreferencesSparkJob(kName:String) extends SparkJob{
  val keyspaceName=kName



  logger.debug("CREATED SQL CONTEXT")
  /**
    * Performs users clustering, in order to limit data model loading to one specific cluster only.
    * Users are clustered based on the credentials they are assigned to
    */
  def runKmeans(totalNumberOfUsers:Long, keyspaceName:String, possibleMaxIterations:Seq[Int], clusterAproxSize:Int): Unit ={
    val possibleNumClusters=getMinNumClusters(totalNumberOfUsers, clusterAproxSize)

    val (usersWithCredentialsDF, usersWithExplodedCredentials)= UserFeaturesDataManager.prepareUsersCredentialDataFrame(sparkSession,keyspaceName)
    val resultsDF= FeaturesBuilder.buildAndTransformPipelineModel(usersWithExplodedCredentials)
    val joinedResults=UserFeaturesDataManager.combineUserCredentialVectors(sparkSession, resultsDF, usersWithCredentialsDF)
    val clusteringResults= KMeansClusterer.performClustering(joinedResults,sparkSession, possibleNumClusters, possibleMaxIterations)
    UserFeaturesDataManager.interpretKMeansClusteringResults(sparkSession,clusteringResults,keyspaceName)
  }
  /**
    * Calculates what are the boundaries for the number of clusters
    * @return
    */
  private def getMinNumClusters(totalNumberOfUsers:Long,clusterAproxSize:Int)={
    val maxNumber:Int= (totalNumberOfUsers/clusterAproxSize).toInt
    val multiplicator:Int=if(maxNumber>20) maxNumber/10 else 1
    val minNumber:Int=if(maxNumber>5) maxNumber-5*multiplicator else 1
    logger.debug("MIN:"+minNumber+" MAX:"+maxNumber+" multiplicator:"+multiplicator)
    Seq(minNumber, maxNumber)
  }
  /**
    * Performs recommendation of similar users based on their similarity using Spark ML ALS and cosine similarity
    */
  def runALSUserRecommender(clusterAproxSize:Int,keyspaceName:String,indexRecommendationDataName:String, similarUsersIndexType:String): Unit ={
    val clustersUsers =UserFeaturesDataManager.loadUsersInClusters(sparkSession,keyspaceName).collect()
    clustersUsers.foreach {
      row: Row =>
        logger.debug("RUN ALS ON ROW:"+row.toString())
        ALSUserRecommender.processClusterUsers(sc,row.getLong(0), row.getList[Long](1).toList, clusterAproxSize, keyspaceName,indexRecommendationDataName,similarUsersIndexType)
    }
    logger.debug("runALSUserRecommender finished")
  }
  def createOneCluster(keyspaceName:String)= {
    logger.debug("CREATE ONE CLUSTER ONLY")
    logger.debug("KEYSPACE:"+keyspaceName+" ")
    if(sparkSession==null)logger.debug("SQL CONTEXT IS NULL")
    import sparkSession.implicits._
    val usersInTheSystemDF: DataFrame = sparkSession.read.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspaceName,
      "table" -> TablesNames.USER_COURSES)).load()
    usersInTheSystemDF.show
    val clusterUsers:java.util.List[java.lang.Long] = usersInTheSystemDF.select("userid").map(row => {
      logger.debug("MAPPING ROW:"+row.toString())
      val id = row.getLong(0).asInstanceOf[java.lang.Long]
      id
    }).collect().toList.asJava
    val clusterId:Long=0
    //logger.debug("TEMPORARY DISABLED")
    val recommendationsDAO=new RecommendationsDAO(keyspaceName)

    recommendationsDAO.insertClusterUsers(clusterId,clusterUsers)


    //UserRecommendationsDBManagerImpl.getInstance().insertClusterUsers(clusterId,clusterUsers)

  }

}
