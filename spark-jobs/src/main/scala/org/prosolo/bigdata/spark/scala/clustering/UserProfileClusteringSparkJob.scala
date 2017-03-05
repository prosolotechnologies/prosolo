package org.prosolo.bigdata.spark.scala.clustering

import java.util.{Calendar, Date}

import org.apache.spark.rdd.RDD
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import org.prosolo.bigdata.scala.clustering.userprofiling.{HmmClustering, UsersClustering}
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.prosolo.bigdata.dal.cassandra.impl.TablesNames

import scala.collection.mutable.Iterable

/**
  * Created by zoran on 20/02/17.
  */
/**
  * zoran 20/02/17
  */
object UserProfileClusteringSparkJob {
  val sc = SparkContextLoader.getSC

  def runSparkJob(credentialsIds: java.util.List[java.lang.Long], dbName: String, days: IndexedSeq[DateTime],
                  numClusters: Int, numFeatures: Int): Unit = {
    val credentialsIdsScala: Seq[java.lang.Long] = credentialsIds.asScala.toSeq
    println("ALL CREDENTIALS:" + credentialsIdsScala.mkString(","))


    val credentialsRDD: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })
    val connector = CassandraConnector(sc.getConf)
    credentialsRDD.foreachPartition {

      credentialsIt => {
        val credentials=credentialsIt.duplicate
        val userCourseKMeansProfiles: Iterator[Iterable[Tuple5[Long, String, Long, Long, String]]] = credentials._1.map { credentialid =>
          println("RUNNING USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
         // println("TEMPORARY DISABLED")

          val userCourseProfile: Iterable[Tuple5[Long, String, Long, Long, String]] = runPeriodicalKMeansClustering(dbName, days, numClusters, numFeatures, credentialid)

          // runPeriodicalHmmClustering(dbName, startDate, endDate,  periodToCalculate, credentialid)
          userCourseProfile
        }
        userCourseKMeansProfiles.foreach(userProfile => {
          println("INSERTING FOR EACH USER PROFILE")
          val query = "INSERT INTO " + dbName + "." + TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE + "(course,  profile,date, userid, sequence) VALUES (?, ?, ?,?,?) ";
          connector.withSessionDo {
            session => {
              userProfile.foreach(record => {
                println("USER PROFILE RECORD:" + record._1 + " " + record._2 + " " + record._3 + " " + record._4 + " " + record._5)
                session.execute(query, record._1.asInstanceOf[java.lang.Long], record._2.asInstanceOf[String], record._3.asInstanceOf[java.lang.Long], record._4.asInstanceOf[java.lang.Long], record._5.asInstanceOf[String])
              })

            }
          }
          //UserObservationsDBManagerImpl.getInstance().insertUserQuartileFeaturesByProfile(courseId, clusterProfile, endDateSinceEpoch, userid, sequence);
         // INSERT INTO "+TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE+"(course,  profile,date, userid, sequence) VALUES (?, ?, ?,?,?);";

        //  UserObservationsDBManagerImpl.getInstance().insertUserQuartileFeaturesByDate(courseId, endDateSinceEpoch, userid, clusterProfile, sequence);
       //   "INSERT INTO " + TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE + "(course,  date, userid,profile, sequence) VALUES (?, ?, ?,?,?);";
       val bydatequery = "INSERT INTO " + dbName + "." + TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE + "(course,  date, userid,profile, sequence) VALUES (?, ?, ?,?,?) ";
          connector.withSessionDo {
            session => {
              userProfile.foreach(record => {
                println("USER PROFILE RECORD BY DATE:" + record._1 + " " + record._3 + " " + record._4 + " " + record._2 + " " + record._5)
                session.execute(bydatequery, record._1.asInstanceOf[java.lang.Long], record._3.asInstanceOf[java.lang.Long], record._4.asInstanceOf[java.lang.Long], record._2.asInstanceOf[String], record._5.asInstanceOf[String])
              })

            }
          }
        })
        println("KT-1")
 credentials._2.foreach {
          credentialid =>
            println("RUNNING HMM USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
     val hmmClustering: HmmClustering = new HmmClustering(dbName)
     hmmClustering.performHmmClusteringForPeriod(days, credentialid)

        }
      }

        println("KT-2. ending partition")
    }
    println("STAGE 2")
   /* val credentialsRDD2: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })*/
   /* credentialsRDD.foreachPartition {

      credentials => {
        val testcId = credentials.map {
          cid =>
            println("CID2:" + cid)
            cid
        }
        println("TEST CID2:" + testcId.length)
      }
    }*/
/*println("HMM PARTITIONS")
    println("ALL CREDENTIALS 2:" + credentialsIdsScala.mkString(","))
    val credentialsRDD2: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })
    println("CREATED RDD")
    credentialsRDD2.foreachPartition{
      credentials=>{
        println("STARTING HMM CLUSTERING NOW")
        credentials.foreach { credentialid =>
          println("RUNNING HMM USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
          //  println("TEMPORARY DISABLED")
          // val userCourseProfile: Iterable[Tuple5[Long, String, Long, Long, String]] = runPeriodicalKMeansClustering(dbName, days, numClusters, numFeatures, credentialid)
          // val hmmClustering: HmmClustering = new HmmClustering(dbName)
          // hmmClustering.performHmmClusteringForPeriod(days, credentialid)

        }
      }
    }*/

    }

    /**
      * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
      *
      * @param startDate
      * @param endDate
      */
    def runPeriodicalKMeansClustering(dbName: String, days: IndexedSeq[DateTime], numClusters: Int, numFeatures: Int, credentialId: Long): Iterable[Tuple5[Long, String, Long, Long, String]] = {
      //  println("RUN PERIODICAL KMeans Clustering for course:"+credentialId+" between dates:"+startDate.toString+" and "+endDate.toString)
      //  println("START:"+startDate+" END:"+endDate)
      //  val start=new DateTime(startDate)
      //  val end=new DateTime(endDate)
      // val days=(0 until periodToCalculate).map(start.plusDays(_)).foreach(println)
      val usersClustering: UsersClustering = new UsersClustering(dbName, numClusters, numFeatures)

      val userscourseprofiles = usersClustering.performKMeansClusteringForPeriod(days, credentialId)
      userscourseprofiles
    }

    /**
      * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
      *
      * @param startDate
      * @param endDate
      */
    /* def runPeriodicalHmmClustering(dbName:String,days:IndexedSeq[DateTime], credentialId:Long): Unit ={
   // var tempDate=startDate
    val hmmClustering:HmmClustering=new HmmClustering(dbName)
   // while(endDate.compareTo(tempDate)>0){
      hmmClustering.performHmmClusteringForPeriod(days, credentialId)
     // tempDate=addDaysToDate(tempDate, periodToCalculate+1)

    }
  }*/


    // runPeriodicalClustering(startDate,endDate2)
    def addDaysToDate(date: Date, days: Int): Date = {
      val cal: Calendar = Calendar.getInstance()
      cal.setTime(date)
      cal.add(Calendar.DATE, days)
      val newDate: Date = cal.getTime
      newDate
    }

}



