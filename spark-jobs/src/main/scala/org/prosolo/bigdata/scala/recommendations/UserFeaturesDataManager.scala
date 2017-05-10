package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.sql._
import com.datastax.spark.connector._
import org.apache.spark.ml.linalg.Vectors
import org.jblas.DoubleMatrix
import org.prosolo.bigdata.dal.cassandra.impl.TablesNames
import org.apache.spark.mllib.linalg.Vector



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

  implicit val doubleMatrixEncoder=org.apache.spark.sql.Encoders.kryo[DoubleMatrix]
  implicit def tuple2[A1, A2](
                               implicit e1: Encoder[A1],
                               e2: Encoder[A2]
                             ): Encoder[(A1,A2)] = Encoders.tuple[A1,A2](e1, e2)

  //val keyspaceName=CassandraDDLManagerImpl.getInstance().getSchemaName
 // val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix



  /**
    * Loads User credentials from cassandra database and explodes credentials
    * @param sqlContext
    * @return
    */
  def prepareUsersCredentialDataFrame(sqlContext: SQLContext, keyspaceName: String): (DataFrame, DataFrame) = {
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
    implicit val myObjEncoder = org.apache.spark.sql.Encoders.kryo[Tuple2[Long,DoubleMatrix]]
    usersWithCredentialsDF.show(100)
    resultsDF.show(100)
     //Combine One-Hot Encoded credential vectors for each item
    val usersWithcredentialsOneHotEncodedCombinedDF=resultsDF.map{
  case Row(userid: Long, credential:String, credentialIndex: Double, credentialsOneHotEncoded: org.apache.spark.ml.linalg.SparseVector)=>
     (userid, new DoubleMatrix(credentialsOneHotEncoded.toDense.values))
 }.rdd.reduceByKey((credentialsOneHotEncodedDoubleMatrix1, credentialsOneHotEncodedDoubleMatrix2)=>
   credentialsOneHotEncodedDoubleMatrix1.ori(credentialsOneHotEncodedDoubleMatrix2))
   .mapValues(tagsOneHotEncodedDoubleMatrixCompined => Vectors.dense(tagsOneHotEncodedDoubleMatrixCompined.toArray))
   .toDF("userid","credentialsOneHotEncodedCombined").as("oneHotEncodedCombined")

    val joinedResultsWithcredentialsOneHotEncodedCombinedDF=usersWithCredentialsDF
      .join(usersWithcredentialsOneHotEncodedCombinedDF,$"usersWithCredentials.userid" ===$"oneHotEncodedCombined.userid")

    joinedResultsWithcredentialsOneHotEncodedCombinedDF.select($"usersWithCredentials.userid",$"credentials",$"credentialsOneHotEncodedCombined").show(35)

    joinedResultsWithcredentialsOneHotEncodedCombinedDF.select($"usersWithCredentials.userid",$"credentials",$"credentialsOneHotEncodedCombined")

  }

  def ori(other:Array[Double],result:Array[Double]): Array[Double] ={
      for(i<-0 to result.length-1)
      {
        result(i)= if (result(i) != 0.0D | other(i) != 0.0D) 1.0D
        else 0.0D
      }
    result
  }

  def interpretKMeansClusteringResults(sqlContext: SQLContext,clusteringResults:DataFrame,keyspaceName:String): Unit ={
    import sqlContext.implicits._
    clusteringResults.sort($"clusterId" asc).show(1000)
  //  val count=clusteringResults.groupBy("clusterId").count
    //count.show

   val groupedByClusters= clusteringResults.select("clusterId","userid").rdd.groupBy(s=>s.getInt(0)).cache()
   val clustersUsers= groupedByClusters.map(s=>{
      (s._1,s._2.map(row=>row.get(1)))})
    clustersUsers.saveToCassandra(keyspaceName,TablesNames.USERRECOM_CLUSTERUSERS,SomeColumns("cluster","users"))
  }
  def loadUsersInClusters(sqlContext: SQLContext,keyspaceName:String):DataFrame={
    println("LOAD USERS IN CLUSTERS:"+keyspaceName)
    val clustersUsers = sqlContext.read.format("org.apache.spark.sql.cassandra").options(Map("keyspace" -> keyspaceName,
      "table" -> TablesNames.USERRECOM_CLUSTERUSERS)).load()
    clustersUsers.show
    clustersUsers
  }
}

