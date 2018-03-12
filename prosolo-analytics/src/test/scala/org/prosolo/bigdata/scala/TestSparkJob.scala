package org.prosolo.bigdata.scala

import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark.rdd.EsSpark
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileClusteringManager
import org.prosolo.bigdata.scala.spark._
import org.elasticsearch.spark._
import org.elasticsearch.spark.rdd.EsSpark
import org.prosolo.bigdata.scala.emails.NotificationsEmailManager
import org.prosolo.bigdata.scala.instructorEmails.{InstructorEmailManager, InstructorEmailManager$}
import org.prosolo.common.config.CommonSettings
import org.prosolo.common.util.date.DateEpochUtil
/**
  *
  * @author Zoran Jeremic
  * @date 2017-09-08
  * @since 1.0.0
  */

object TestSparkJob extends App {

 // UserProfileClusteringManager.runClustering()

 // val emailManager = NotificationsEmailManager$.MODULE$
 val date: Long = DateEpochUtil.getDaysSinceEpoch
  println("CURRENT DATE:"+date)
 val testDate=17589
  //NotificationsEmailManager.runAnalyser(testDate)

  InstructorEmailManager.runJob()

 //testESInsert()
  def testESInsert(): Unit ={
    val sparkSession:SparkSession=SparkManager.sparkContextLoader.getSparkSession
    val sc=sparkSession.sparkContext
    val task=new TaskSummary("A","C", 0,System.currentTimeMillis())
    val rddSummary= sparkSession.sparkContext.makeRDD(Seq(task))
     val resource=CommonSettings.getInstance().config.elasticSearch.jobsLogsIndex;//SparkApplicationConfig.conf.getString("elasticsearch.jobsIndex")
    EsSpark.saveToEs(rddSummary,resource+"/summary")

    val mapping=Map("es.mapping.id"->"jobId")
    val failedTask=new FailedTask("A","C","Failed task in some job...",123, "Task type here")
    val failedTask2=new FailedTask("A","C","Failed task in some job...",1233, "Task type here")
    val rddFailed= sparkSession.sparkContext.makeRDD(Seq(failedTask,failedTask2))
    EsSpark.saveToEs(rddFailed,resource+"/failed", mapping)

   // val upcomingTrip = org.prosolo.bigdata.scala.spark.Trip("OTP2", "SFO2")
    //val lastWeekTrip = org.prosolo.bigdata.scala.spark.Trip("MUC2", "OTP2")

  //  val rdd = sc.makeRDD(Seq(upcomingTrip, lastWeekTrip))
    //EsSpark.saveToEs(rdd, resource+"/docs")





  }

}
