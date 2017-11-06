package org.prosolo.bigdata.scala.es

import org.elasticsearch.spark.rdd.EsSpark
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkManager}

//import org.prosolo.bigdata.es.ESIndexNames

/**
  * Created by zoran on 24/07/16.
  */
/**
  * zoran 24/07/16
  */
case class UserRecommendations(id:Long, recommendedUsers:Array[Recommendations])
case class Recommendations(id:Long, score:Double)
object RecommendationsESIndexer {
  val mapping=Map("es.mapping.id"->"id")
  def storeRecommendedUsersForUser(userId:Long, sortedSims: Array[(Int, Double) ],indexRecommendationDataName:String, similarUsersIndexType:String): Unit ={
    val recommendations=sortedSims.map{
      case (user,similarity)=>
         Recommendations(user,similarity)

    }

    val sc=SparkManager.sparkContextLoader.getSC
    val rdd=sc.makeRDD(Seq(UserRecommendations(userId,recommendations)))
     val resource=indexRecommendationDataName+"/"+similarUsersIndexType
    //val resource="spark/docs"
    EsSpark.saveToEs(rdd, resource,mapping)
  }
}
