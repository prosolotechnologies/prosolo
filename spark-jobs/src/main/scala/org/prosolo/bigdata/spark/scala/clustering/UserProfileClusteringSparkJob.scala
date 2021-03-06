package org.prosolo.bigdata.spark.scala.clustering

import java.util.{Calendar, Date}

import org.apache.hadoop.fs.Path
import org.apache.spark.rdd.RDD
import org.joda.time.DateTime
//import org.prosolo.bigdata.scala.clustering.userprofiling.HmmClustering
import org.prosolo.bigdata.utils.DateUtil
import scala.collection.JavaConverters._
import org.prosolo.bigdata.scala.spark.{ProblemSeverity, SparkJob}
import org.prosolo.bigdata.dal.cassandra.impl.ProfilesDAO



/**
  * Created by zoran on 20/02/17.
  */
/**
  * zoran 20/02/17
  */
case class CourseClusterConfiguration(courseId: Long,
                                      clustersDir: String,
                                      vectorsDir: String,
                                      outputDir: String,
                                      output: Path,
                                      datapath: Path) extends Serializable

class UserProfileClusteringSparkJob(kName: String, numFeatures: Int, numClusters: Int) extends SparkJob with Serializable {
  val keyspaceName = kName
 // val profilesDAO = new ProfilesDAO(keyspaceName)

  def runSparkJob(credentialsIds: java.util.List[java.lang.Long], dbName: String, days: IndexedSeq[DateTime],
                  numClusters: Int, numFeatures: Int): Unit = {
    val daysSinceEpoch: IndexedSeq[Long] = days.map {
      day =>
        DateUtil.getDaysSinceEpoch(day)
    }
    if (credentialsIds.size() == 0) {
      submitTaskProblem("NO CREDENTIALS FOUND", 0, "runSparkJob", ProblemSeverity.MAJOR)
    }
    val credentialsIdsScala: Seq[java.lang.Long] = credentialsIds.asScala.toSeq
    logger.debug("ALL CREDENTIALS:" + credentialsIdsScala.mkString(","))

    val credentialsRDD: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })
    credentialsRDD.foreachPartition {
      val profilesDAO = new ProfilesDAO(dbName)
      credentialsIt => {
        val credentials = credentialsIt.duplicate

       val userCourseKMeansProfiles: Iterator[Iterable[(Long, String, Long, Long, String)]] =
        credentials._1.map { credentialId =>
       // logger.debug("RUNNING USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialId)
         val userCourseProfile: Iterable[(Long, String, Long, Long, String)] =
         KMeansClusteringUtility.performKMeansClusteringForPeriod(
           daysSinceEpoch,
           credentialId,
           dbName,
           numFeatures,
           numClusters
         )

        userCourseProfile
       }
        //This code is disabled because of Spark serialization issue and since it's not used at the moment
       /* userCourseKMeansProfiles.foreach(userProfile => {
          logger.debug("INSERTING FOR EACH USER PROFILE")
          profilesDAO.insertUserQuartileFeaturesByProfile(userProfile)
          profilesDAO.insertUserQuartileFeaturesByDate(userProfile)


        })*/
       //credentials._2.foreach {
      //    credentialid =>
          //  logger.debug("RUNNING HMM USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
          //  val hmmClustering: HmmClustering = new HmmClustering(dbName)
         //   hmmClustering.performHmmClusteringForPeriod(days, credentialid)

        //}
      }

    }

  }


  def addDaysToDate(date: Date, days: Int): Date = {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DATE, days)
    val newDate: Date = cal.getTime
    newDate
  }


}



