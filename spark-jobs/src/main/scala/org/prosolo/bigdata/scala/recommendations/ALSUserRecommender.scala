package org.prosolo.bigdata.scala.recommendations

import com.datastax.spark.connector.{CassandraRow, _}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.dal.cassandra.impl.{RecommendationsDAO, TablesNames}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.jblas.DoubleMatrix
import org.prosolo.bigdata.scala.es.RecommendationsESIndexer

/**
  * Created by zoran on 23/07/16.
  */
/**
  * zoran 23/07/16
  */
object ALSUserRecommender {
  //val keyspaceName=CassandraDDLManagerImpl.getInstance().getSchemaName

  def processClusterUsers(sc: SparkContext, cId: Long, users: List[Long], clusterAproxSize:Int, keyspaceName:String,indexRecommendationDataName:String, similarUsersIndexType:String) {
    println("PROCESS CLUSTER:" + cId + " users:" + users.mkString(","))
    val usersIds = sc.parallelize(users)
    val recommendationsDAO=new RecommendationsDAO(keyspaceName)
    val rawRatings = usersIds.map(Tuple1(_)).joinWithCassandraTable(keyspaceName, TablesNames.USERRECOM_USERRESOURCEPREFERENCES)
    println("FOUND:" + rawRatings.collect().length);
  if(!rawRatings.isEmpty()) {
    val maximumPreference = rawRatings.fold(rawRatings.first())((res1: (Tuple1[Long], CassandraRow), res2: (Tuple1[Long], CassandraRow)) => {
      if (res1._2.getDouble("preference") < res2._2.getDouble("preference")) res2 else res1
    })._2.getDouble("preference")
    println("HIGHEST PREFERENCE:" + maximumPreference)

    val ratings: RDD[Rating] = rawRatings
      .map {
        case (id, row) =>
          val rating = new Rating(row.getLong(0).toInt, resourceIdHash(row.getString(1), row.getLong(2)), (row.getDouble(4) / maximumPreference).toFloat)
          rating
      }
    val model = ALS.train(ratings, 50, 10, 0.01)
    println("FINISHED MODEL FOR CLUSTER:" + cId + " users number:" + usersIds.collect().length)

    val recNumber = clusterAproxSize

    users.foreach(userId => {
      // val recommendations= usersIds.map(userId => {
      println("PROCESSING USER:" + userId)
      val sortedSims: Array[(Int, Double)] = findSimilarUsers(model, userId, recNumber);
      val nonRelevantUsers: List[Long] = users.filter { uid => !sortedSims.exists(_._1 == uid) }
      println("USER RECOMMENDATIONS:FOR USER:" + sortedSims.size + ":" + userId + " :" + sortedSims.slice(1, recNumber + 1).mkString(","))
      println("NON RELEVANT" + nonRelevantUsers)
      val nonRel: Array[(Int, Double)] = nonRelevantUsers.map { uid => (uid.toInt, 0.0) }.toArray
      val recommendations: Array[(Int, Double)] = sortedSims ++ nonRel
      println("WHOLE LIST:" + recommendations.mkString(","))
      // println("TEMPORARY DISABLED NEXT TWO LINES")
      RecommendationsESIndexer.storeRecommendedUsersForUser(userId, recommendations, indexRecommendationDataName, similarUsersIndexType)
      recommendationsDAO.deleteStudentNew(userId)
      // UserRecommendationsDBManagerImpl.getInstance.deleteStudentNew(userId)
      //(userId, sortedSims)
      // }
    }
    )
  }
  //  println("RECOMMENDATIONS:"+recommendations.collect().length);
    println("USER RECOMMENDATIONS:FINISHED:")


  }
  def resourceIdHash(resourcetype: String, resourceid: Long): Int = (resourcetype + "_" + resourceid).hashCode & 0x7FFFFF
  def findSimilarUsers(model: MatrixFactorizationModel, userid: Long, recNumber: Integer): Array[(Int, Double)] = {

    val userFactorOpt = model.userFeatures.lookup(userid.toInt).headOption
    userFactorOpt match {
      case Some(s) => {
        val userVector = new DoubleMatrix(userFactorOpt.get)
        // val s1=cosineSimilarity(userVector,userVector)
        val sims = model.userFeatures.map { case (id, factor) =>
          val factorVector = new DoubleMatrix(factor)
          val sim = cosineSimilarity(factorVector, userVector)
          (id, sim)
        }
        val sortedSims = sims.top(recNumber + 1)(Ordering.by[(Int, Double), Double] { case (id, similarity) => similarity })
        sortedSims.slice(1, recNumber + 1)
      }
      case None => {
        println("USER FACTOR NOT FOUND:" + userid)
        new Array[(Int, Double)](0)
      }
    }
  }
  def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }
}
