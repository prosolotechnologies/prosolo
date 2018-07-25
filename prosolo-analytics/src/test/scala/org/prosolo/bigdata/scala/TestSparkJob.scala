package org.prosolo.bigdata.scala

import org.apache.spark.sql.SparkSession
import org.prosolo.bigdata.scala.spark._
import org.elasticsearch.spark.rdd.EsSpark
import org.prosolo.bigdata.scala.clustering.sna.SNAclusterManager
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileClusteringManager.{days, dbName, numClusters, numFeatures}
import org.prosolo.bigdata.spark.scala.clustering.UserProfileClusteringSparkJob
import org.prosolo.common.config.CommonSettings
import org.prosolo.common.util.date.DateEpochUtil

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
/**
  *
  * @author Zoran Jeremic
  * @date 2017-09-08
  * @since 1.0.0
  */

object TestSparkJob extends App {

  // UserProfileClusteringManager.runClustering()


 // UserProfileClusteringManager.runClustering()
 // SNAclusterManager.runClustering()
  //val emailManager = NotificationsEmailManager$.MODULE$
  val date: Long = DateEpochUtil.getDaysSinceEpoch
  println("CURRENT DATE:" + date)
  val testDate = 17714
  NotificationsEmailManager.runAnalyser(testDate)
// val date: Long = DateEpochUtil.getDaysSinceEpoch
//  logger.debug("CURRENT DATE:"+date)
 //val testDate=17589
 //  NotificationsEmailManager.runAnalyser(testDate)
 val userProfileClusteringSparkJob=new UserProfileClusteringSparkJob(dbName, numFeatures,numClusters)
  val credentialsIds=List[java.lang.Long](1).asJava

  def testESInsert(): Unit = {
    val sparkSession: SparkSession = SparkManager.sparkContextLoader.getSparkSession
    val sc = sparkSession.sparkContext
    val task = new TaskSummary("A", "C", 0, System.currentTimeMillis())
    val rddSummary = sparkSession.sparkContext.makeRDD(Seq(task))
    val resource = CommonSettings.getInstance().config.elasticSearch.jobsLogsIndex; //SparkApplicationConfig.conf.getString("elasticsearch.jobsIndex")
    EsSpark.saveToEs(rddSummary, resource + "/summary")
  userProfileClusteringSparkJob.runSparkJob(credentialsIds,dbName, days,numClusters,numFeatures)
 // InstructorEmailManager.runJob()

 //testESInsert()
  def testESInsert(): Unit ={
    val sparkSession:SparkSession=SparkManager.sparkContextLoader.getSparkSession
    val sc=sparkSession.sparkContext
    val task=new TaskSummary("A","C", 0,System.currentTimeMillis())
    val rddSummary= sparkSession.sparkContext.makeRDD(Seq(task))
     val resource=CommonSettings.getInstance().config.elasticSearch.jobsLogsIndex;//SparkApplicationConfig.conf.getString("elasticsearch.jobsIndex")
    EsSpark.saveToEs(rddSummary,resource+"/summary")

    val mapping = Map("es.mapping.id" -> "jobId")
    val failedTask = new FailedTask("A", "C", "Failed task in some job...", 123, "Task type here")
    val failedTask2 = new FailedTask("A", "C", "Failed task in some job...", 1233, "Task type here")
    val rddFailed = sparkSession.sparkContext.makeRDD(Seq(failedTask, failedTask2))
    EsSpark.saveToEs(rddFailed, resource + "/failed", mapping)


  }

}
