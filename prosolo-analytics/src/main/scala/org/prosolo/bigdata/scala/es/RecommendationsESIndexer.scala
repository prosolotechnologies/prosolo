package org.prosolo.bigdata.scala.es

import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.elasticsearch.spark.rdd.EsSpark
import org.prosolo.bigdata.common.enums.ESIndexTypes
import org.prosolo.bigdata.es.ESIndexNames
import org.prosolo.common.config.CommonSettings

/**
  * Created by zoran on 24/07/16.
  */
/**
  * zoran 24/07/16
  */
case class UserRecommendations(id:Long, recommendedUsers:Array[Recommendations])
case class Recommendations(id:Long, similarity:Double)
object RecommendationsESIndexer {
  val mapping=Map("es.mapping.id"->"id")
  def storeRecommendedUsersForUser(userId:Long, sortedSims: Array[(Int, Double)]): Unit ={
    val recommendations=sortedSims.map{
      case (user,similarity)=>
        Recommendations(user,similarity)
    }

    val sc=SparkContextLoader.getSC
    val rdd=sc.makeRDD(Seq(UserRecommendations(userId,recommendations)))
     val resource=ESIndexNames.INDEX_RECOMMENDATIONDATA+"/"+ESIndexTypes.SIMILAR_USERS
    //val resource="spark/docs"
    EsSpark.saveToEs(rdd, resource,mapping)
  }
}
