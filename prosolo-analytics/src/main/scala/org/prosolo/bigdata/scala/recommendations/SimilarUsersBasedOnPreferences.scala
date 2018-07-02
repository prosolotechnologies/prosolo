package org.prosolo.bigdata.scala.recommendations

/*import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.mllib.linalg.{SparseVector, Vectors}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.prosolo.bigdata.dal.cassandra.impl.{CassandraDDLManagerImpl, TablesNames, UserObservationsDBManagerImpl, UserRecommendationsDBManagerImpl}
import org.prosolo.bigdata.scala.clustering.kmeans.KMeansClusterer
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import com.datastax.spark.connector._*/

import org.prosolo.bigdata.common.enums.ESIndexTypes
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.slf4j.LoggerFactory
//import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.es.impl.DataSearchImpl
//import org.prosolo.bigdata.jobs.{GenerateUserProfileClusters, SimilarUsersBasedOnPreferencesJob}
import org.prosolo.bigdata.scala.es.RecommendationsESIndexer
import org.prosolo.common.ESIndexNames

//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by zoran on 23/07/16.
  */
/**
  * zoran 23/07/16
  */

object SimilarUsersBasedOnPreferences {
  val logger = LoggerFactory.getLogger(getClass)
  logger.debug("FIND SIMILAR USERS BASED ON PREFERENCES")

  def recommendBasedOnCredentialsForColdStart(userid: Long, credentials: java.util.Set[java.lang.Long]): Unit = {
    val dataSearch = new DataSearchImpl
    val creds = credentials.asScala
    val recommendations = creds.flatMap { credId =>
      val members = dataSearch.findCredentialMembers(credId, 0, 50).asScala
      members
    }.map { member =>
      (member.toInt, 0.0)
    }.toArray
    logger.debug("STORE RECOMMENDATIONS FOR:" + userid + " REC:" + recommendations.mkString(","))
    RecommendationsESIndexer.storeRecommendedUsersForUser(userid, recommendations, ESIndexNames.INDEX_RECOMMENDATION_DATA, ESIndexTypes.SIMILAR_USERS)


  }
}
