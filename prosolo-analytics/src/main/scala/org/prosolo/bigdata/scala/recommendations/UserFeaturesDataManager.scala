package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.mllib.linalg.{SparseVector, Vectors}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.jblas.DoubleMatrix
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{CassandraDDLManagerImpl, TablesNames}
import org.prosolo.bigdata.dal.cassandra.impl.SimpleCassandraClientImpl.TableNames
import org.prosolo.common.config.CommonSettings

/**
  * Created by zoran on 19/07/16.
  */
/**
  * zoran 19/07/16
  */
case class UserCredentials(id:Long, credentials: Seq[String]) {
  override def toString: String=id+", "+credentials
}
object UserFeaturesDataManager {

  val keyspaceName=CassandraDDLManagerImpl.getInstance().getSchemaName
 // val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix



  /**
    * Loads User credentials from cassandra database and explodes credentials
    * @param sqlContext
    * @return
    */
  def prepareUsersCredentialDataFrame(sqlContext: SQLContext): (DataFrame, DataFrame) = {
    import sqlContext.implicits._
    val usersCredentialsDF: DataFrame = sqlContext.read.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspaceName,
      "table" -> TablesNames.USER_COURSES)).load()
    usersCredentialsDF.show

    //convert users to UsersCredential and explode credentials
    val usersWithCredentialsDF: DataFrame = usersCredentialsDF.select("userid", "courses").map(row => {
      val id = row.getLong(0)
      val credentials: Seq[String] = row.getSeq(1).map { x: Long => x.toString } //.trim.split("\\|")
      UserCredentials(id, credentials)
    }).toDF("userid", "credentials").as("usersWithCredentials")
    usersWithCredentialsDF.registerTempTable("usersWithCredentials")
    val usersWithExplodedCredentials = sqlContext.sql("SELECT userid, explode(credentials) as credential FROM usersWithCredentials")
    usersWithExplodedCredentials.show
    (usersWithCredentialsDF, usersWithExplodedCredentials)
  }

  /**
    * Combines users with credentials data frame with results in a single dataframe
    * @param sqlContext
    * @param resultsDF
    * @param usersWithCredentialsDF
    * @return
    */
  def combineUserCredentialVectors(sqlContext: SQLContext, resultsDF:DataFrame, usersWithCredentialsDF:DataFrame):DataFrame={
    import sqlContext.implicits._
    //Combine One-Hot Encoded credential vectors for each item
    val usersWithcredentialsOneHotEncodedCombinedDF=resultsDF.map{
      case Row(userid: Long, credential:String, credentialIndex: Double, credentialsOneHotEncoded: SparseVector)=>
        (userid, (new DoubleMatrix(credentialsOneHotEncoded.toDense.values)))
    }.reduceByKey((credentialsOneHotEncodedDoubleMatrix1, credentialsOneHotEncodedDoubleMatrix2)=>
      credentialsOneHotEncodedDoubleMatrix1.ori(credentialsOneHotEncodedDoubleMatrix2))
      .mapValues(tagsOneHotEncodedDoubleMatrixCompined => Vectors.dense(tagsOneHotEncodedDoubleMatrixCompined.toArray))
      .toDF("userid","credentialsOneHotEncodedCombined").as("oneHotEncodedCombined")

    val joinedResultsWithcredentialsOneHotEncodedCombinedDF=usersWithCredentialsDF
      .join(usersWithcredentialsOneHotEncodedCombinedDF,$"usersWithCredentials.userid" ===$"oneHotEncodedCombined.userid")

    joinedResultsWithcredentialsOneHotEncodedCombinedDF.select($"usersWithCredentials.userid",$"credentials",$"credentialsOneHotEncodedCombined").show(35)
   // joinedResultsWithcredentialsOneHotEncodedCombinedDF
    joinedResultsWithcredentialsOneHotEncodedCombinedDF.select($"usersWithCredentials.userid",$"credentials",$"credentialsOneHotEncodedCombined")
  }

  def interpretKMeansClusteringResults(sqlContext: SQLContext,clusteringResults:DataFrame): Unit ={
    import sqlContext.implicits._
    clusteringResults.sort($"clusterId" asc).show(1000)
    val count=clusteringResults.groupBy("clusterId").count

   // println("COUNT:"+count)
    count.show

   val groupedByClusters= clusteringResults.select("clusterId","userid").rdd.groupBy(s=>s.getInt(0)).cache()
   val clustersUsers= groupedByClusters.map(s=>{
      (s._1,s._2.map(row=>row.get(1)))})
   // clustersUsers.saveToCassandra("prosolo_zj","")
    clustersUsers.saveToCassandra(keyspaceName,TablesNames.USERRECOM_CLUSTERUSERS,SomeColumns("cluster","users"))
      //.collect().foreach(s=>println("CLUSTER:"+s._1+" values:"+s._2.mkString(",")))

  }
  def loadUsersInClusters(sqlContext: SQLContext):DataFrame={
    val clustersUsers = sqlContext.read.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspaceName,
      "table" -> TablesNames.USERRECOM_CLUSTERUSERS)).load()
    clustersUsers.show
    clustersUsers
  }
}

