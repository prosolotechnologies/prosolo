package org.prosolo.bigdata.spark.scala.clustering

import java.util.{Calendar, Date}

import org.apache.spark.rdd.RDD
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import org.prosolo.bigdata.scala.clustering.userprofiling.{HmmClustering, UsersClustering}
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkJob}
import org.prosolo.bigdata.dal.cassandra.impl.ProfilesDAO

import scala.collection.mutable.Iterable

/**
  * Created by zoran on 20/02/17.
  */
/**
  * zoran 20/02/17
  */
class UserProfileClusteringSparkJob(kName:String) extends SparkJob{
  val keyspaceName=kName
 // val sc = SparkContextLoader.getSC

  def runSparkJob(credentialsIds: java.util.List[java.lang.Long], dbName: String, days: IndexedSeq[DateTime],
                  numClusters: Int, numFeatures: Int): Unit = {
    val credentialsIdsScala: Seq[java.lang.Long] = credentialsIds.asScala.toSeq
    println("ALL CREDENTIALS:" + credentialsIdsScala.mkString(","))


    val credentialsRDD: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })
    // val connector = CassandraConnector(sc.getConf)
    credentialsRDD.foreachPartition {
      val profilesDAO = new ProfilesDAO(dbName)
      credentialsIt => {
        val credentials = credentialsIt.duplicate
        val userCourseKMeansProfiles: Iterator[Iterable[Tuple5[Long, String, Long, Long, String]]] = credentials._1.map { credentialid =>
          println("RUNNING USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
         val userCourseProfile: Iterable[Tuple5[Long, String, Long, Long, String]] = runPeriodicalKMeansClustering(dbName, days, numClusters, numFeatures, credentialid)
          userCourseProfile
        }
        userCourseKMeansProfiles.foreach(userProfile => {
          println("INSERTING FOR EACH USER PROFILE")
          profilesDAO.insertUserQuartileFeaturesByProfile(userProfile)
          profilesDAO.insertUserQuartileFeaturesByDate(userProfile)


        })
        println("FOREACH PARTITION KT-2")
        credentials._2.foreach {
          credentialid =>
            println("RUNNING HMM USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
            val hmmClustering: HmmClustering = new HmmClustering(dbName)
            hmmClustering.performHmmClusteringForPeriod(days, credentialid)

        }
      }

    }

  }

  /**
    * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
    *
    * @param startDate
    * @param endDate
    */
  def runPeriodicalKMeansClustering(dbName: String, days: IndexedSeq[DateTime], numClusters: Int, numFeatures: Int, credentialId: Long): Iterable[Tuple5[Long, String, Long, Long, String]] = {
    val usersClustering: UsersClustering = new UsersClustering(dbName, numClusters, numFeatures)
    val userscourseprofiles = usersClustering.performKMeansClusteringForPeriod(days, credentialId)
    userscourseprofiles
  }

  def addDaysToDate(date: Date, days: Int): Date = {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DATE, days)
    val newDate: Date = cal.getTime
    newDate
  }

}



