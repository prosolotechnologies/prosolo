package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.mllib.linalg.{SparseVector, Vectors}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.prosolo.bigdata.dal.cassandra.impl.{CassandraDDLManagerImpl, TablesNames, UserObservationsDBManagerImpl, UserRecommendationsDBManagerImpl}
import org.prosolo.bigdata.scala.clustering.kmeans.KMeansClusterer
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import com.datastax.spark.connector._
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.jobs.{GenerateUserProfileClusters, SimilarUsersBasedOnPreferencesJob}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
/**
  * Created by zoran on 23/07/16.
  */
/**
  * zoran 23/07/16
  */
object SimilarUsersBasedOnPreferences extends App {
println("FIND SIMILAR USERS BASED ON PREFERENCES")
  val jobProperties=Settings.getInstance().config.schedulerConfig.jobs.getJobConfig(classOf[SimilarUsersBasedOnPreferencesJob].getName)
  val clusterAproxSize=jobProperties.jobProperties.getProperty("clusterAproximateSize").toInt;
  val maxIt1=jobProperties.jobProperties.getProperty("possibleKmeansMaxIteration1").toInt;
  val maxIt2=jobProperties.jobProperties.getProperty("possibleKmeansMaxIteration2").toInt;
  val possibleMaxIterations=Seq(maxIt1, maxIt2)
  val keyspaceName=CassandraDDLManagerImpl.getInstance().getSchemaName
  val sc = SparkContextLoader.getSC
  sc.setLogLevel("WARN")
  val sqlContext = SQLContext.getOrCreate(sc)

  def runJob(): Unit ={
    import sqlContext.implicits._
    val totalNumberOfUsers=UserObservationsDBManagerImpl.getInstance().findTotalNumberOfUsers()
    println("TOTAL NUMBER OF USERS:"+totalNumberOfUsers+" MIN-MAX:"+getMinNumClusters(totalNumberOfUsers).toString())

    if (totalNumberOfUsers>clusterAproxSize*1.5) runKmeans(totalNumberOfUsers) else createOneCluster()

    runALSUserRecommender()
  }
runJob()

  /**
    * Stores all users in one cluster without clustering if number of users is small
    */
  def createOneCluster()= {
    println("CREATE ONE CLUSTER ONLY")
    val usersInTheSystemDF: DataFrame = sqlContext.read.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspaceName,
      "table" -> TablesNames.USER_COURSES)).load()
    usersInTheSystemDF.show
    val clusterUsers:java.util.List[java.lang.Long] = usersInTheSystemDF.select("userid").map(row => {
      val id = row.getLong(0).asInstanceOf[java.lang.Long]
      id
    }).collect().toList.asJava
    val clusterId:Long=0
    UserRecommendationsDBManagerImpl.getInstance().insertClusterUsers(clusterId,clusterUsers)

  }

  /**
    * Calculates what are the boundaries for the number of clusters
    * @return
    */
  private def getMinNumClusters(totalNumberOfUsers:Long)={
    val maxNumber:Int= (totalNumberOfUsers/clusterAproxSize).toInt
    val multiplicator:Int=if(maxNumber>20) maxNumber/10 else 1
    val minNumber:Int=if(maxNumber>5) maxNumber-5*multiplicator else 1
    println("MIN:"+minNumber+" MAX:"+maxNumber+" multiplicator:"+multiplicator)
    Seq(minNumber, maxNumber)
  }

  /**
    * Performs users clustering, in order to limit data model loading to one specific cluster only.
    * Users are clustered based on the credentials they are assigned to
    */
  def runKmeans(totalNumberOfUsers:Long): Unit ={
    val possibleNumClusters=getMinNumClusters(totalNumberOfUsers)

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
        ALSUserRecommender.processClusterUsers(sc,row.getLong(0), row.getList[Long](1).toList, clusterAproxSize)
    }
  }

  // runKmeans()

}
