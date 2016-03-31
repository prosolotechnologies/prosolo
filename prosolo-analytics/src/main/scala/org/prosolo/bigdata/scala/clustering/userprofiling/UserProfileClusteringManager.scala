package org.prosolo.bigdata.scala.clustering.userprofiling

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
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
  val periodToCalculate=7
  val startDate:Date=new Date(endDate.getTime-(periodToCalculate*DAY_IN_MS))
  //val endDate: Date = dateFormat.parse("12/20/2014")
  //val moocCourses:Array[Long]=Array(1,32768,32769,32770,65536,98304,98305,98306,131072,131073,131074)
  // val moocCourses:Array[Long]=Array(1,32768);
  val clusteringDAOManager=new ClusteringDAOImpl

  def runClustering()={
    println("INITIALIZE USER PROFILE CLUSTERING ")
    val sc=SparkContextLoader.getSC
    val coursesIds=clusteringDAOManager.getAllCoursesIds
    val coursesIdsScala:Seq[java.lang.Long]=coursesIds.asScala.toSeq
    val coursesRDD:RDD[Long]=sc.parallelize(coursesIdsScala.map { Long2long})
    coursesRDD.foreachPartition {
      courses => {
        courses.foreach { courseid => {
          println("RUNNING USER PROFILE CLUSTERING FOR COURSE:" + courseid)
         // println("TEMPORARY DISABLED")
          runPeriodicalKMeansClustering(startDate, endDate, courseid)
          runPeriodicalHmmClustering(startDate, endDate, courseid)
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
    * @param startDate
    * @param endDate
    */
  def runPeriodicalKMeansClustering(startDate:Date, endDate:Date, courseId:Long): Unit ={
    println("RUN PERIODICAL KMeans Clustering between dates:"+startDate.toString+" and "+endDate.toString)
    var tempDate=startDate
     val usersClustering:UsersClustering=new UsersClustering()
    while(endDate.compareTo(tempDate)>0){
      usersClustering.performKMeansClusteringForPeriod(tempDate,addDaysToDate(tempDate,ClusteringUtils.periodDays), courseId)
      tempDate=addDaysToDate(tempDate, ClusteringUtils.periodDays+1)

    }
  }
  /**
    * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
    * @param startDate
    * @param endDate
    */
  def runPeriodicalHmmClustering(startDate:Date, endDate:Date, courseId:Long): Unit ={
    var tempDate=startDate
    val hmmClustering:HmmClustering=new HmmClustering()
    while(endDate.compareTo(tempDate)>0){
      hmmClustering.performHmmClusteringForPeriod(tempDate,addDaysToDate(tempDate,ClusteringUtils.periodDays), courseId)
      tempDate=addDaysToDate(tempDate, ClusteringUtils.periodDays+1)

    }
  }

}
