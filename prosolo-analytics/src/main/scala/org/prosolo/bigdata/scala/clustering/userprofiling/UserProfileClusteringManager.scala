package org.prosolo.bigdata.scala.clustering.userprofiling

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.jobs.GenerateUserProfileClusters
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import scala.collection.JavaConverters._

/**
  * Created by zoran on 15/12/15.
  */
/**
  * zoran 15/12/15
  */
object UserProfileClusteringManager {
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

  //val startDate: Date = dateFormat.parse("10/20/2014")
  val endDate: Date=new Date
  val DAY_IN_MS:Long=1000*60*60*24
  val jobProperties=Settings.getInstance().config.schedulerConfig.jobs.getJobConfig(classOf[GenerateUserProfileClusters].getName)
  println("JOB PROPERTIES:"+jobProperties.toString)
  val periodToCalculate=jobProperties.jobProperties.getProperty("periodToCalculate").toInt;
  val startDate:Date=new Date(endDate.getTime-(periodToCalculate*DAY_IN_MS))
  //val endDate: Date = dateFormat.parse("12/20/2014")
  //val moocCourses:Array[Long]=Array(1,32768,32769,32770,65536,98304,98305,98306,131072,131073,131074)
  // val moocCourses:Array[Long]=Array(1,32768);
  val clusteringDAOManager=new ClusteringDAOImpl

  def runClustering()={
    println("INITIALIZE USER PROFILE CLUSTERING ")
    val sc=SparkContextLoader.getSC
    val credentialsIds=clusteringDAOManager.getAllCredentialsIds
    val credentialsIdsScala:Seq[java.lang.Long]=credentialsIds.asScala.toSeq
    val credentialsRDD:RDD[Long]=sc.parallelize(credentialsIdsScala.map { Long2long})
    credentialsRDD.foreachPartition {
      credentials => {
        credentials.foreach { credentialid => {
          println("RUNNING USER PROFILE CLUSTERING FOR CREDENTIAL:" + credentialid)
         // println("TEMPORARY DISABLED")
          runPeriodicalKMeansClustering(startDate, endDate, credentialid)
          runPeriodicalHmmClustering(startDate, endDate, credentialid)
        }
        }
      }
    }
  }

  // runPeriodicalClustering(startDate,endDate2)
  def addDaysToDate(date:Date, days:Int): Date ={
    val cal:Calendar=Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DATE,days)
    val newDate:Date=cal.getTime
    newDate
  }
  /**
    * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
    *
    * @param startDate
    * @param endDate
    */
  def runPeriodicalKMeansClustering(startDate:Date, endDate:Date, credentialId:Long): Unit ={
    println("RUN PERIODICAL KMeans Clustering for course:"+credentialId+" between dates:"+startDate.toString+" and "+endDate.toString)
    var tempDate=startDate
     val usersClustering:UsersClustering=new UsersClustering()
    while(endDate.compareTo(tempDate)>0){
      usersClustering.performKMeansClusteringForPeriod(tempDate,addDaysToDate(tempDate,periodToCalculate), credentialId)
      tempDate=addDaysToDate(tempDate, periodToCalculate+1)
    }
  }
  /**
    * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
    *
    * @param startDate
    * @param endDate
    */
  def runPeriodicalHmmClustering(startDate:Date, endDate:Date, credentialId:Long): Unit ={
    var tempDate=startDate
    val hmmClustering:HmmClustering=new HmmClustering()
    while(endDate.compareTo(tempDate)>0){
      hmmClustering.performHmmClusteringForPeriod(tempDate,addDaysToDate(tempDate,periodToCalculate), credentialId)
      tempDate=addDaysToDate(tempDate, periodToCalculate+1)

    }
  }

}
