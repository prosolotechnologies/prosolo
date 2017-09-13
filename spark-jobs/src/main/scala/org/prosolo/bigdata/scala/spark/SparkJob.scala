package org.prosolo.bigdata.scala.spark

import org.apache.spark.sql.SparkSession
import org.prosolo.bigdata.dal.cassandra.impl.LogSeverity.Value
import org.prosolo.bigdata.dal.cassandra.impl.{JobLoggerDAO, LogSeverity, LogType}
import org.elasticsearch.spark._
import org.elasticsearch.spark.rdd.EsSpark


/**
  * Created by zoran on 26/07/17.
  */
private[spark] sealed trait TaskResult{
 def message:String
  def objectId:Long
  def taskType:String
}
private[spark] case class FailedTask (message:String, objectId:Long, taskType:String)
  extends TaskResult with Serializable
private[spark] case class TaskProblem(message:String, objectId:Long, taskType:String, severity: ProblemSeverity.Value)
  extends TaskResult with Serializable
object ProblemSeverity extends Enumeration{
  val CRITICAL=Value("CRITICAL")
  val MAJOR=Value("MAJOR")
  val MINOR=Value("MINOR")
  val TRIVIAL=Value("TRIVIAL")
}
trait SparkJob extends Serializable{
  def keyspaceName:String
  def jobName=this.getClass.getSimpleName
  def jobId=java.util.UUID.randomUUID().toString
  println("RUNNING JOB:"+jobName+" WITH UUID:"+jobId)
 // val sc = SparkContextLoader.getSC
 // sc.setLogLevel("WARN")
  val sparkSession:SparkSession=SparkContextLoader.getSparkSession
  sparkSession.sparkContext.setLogLevel("WARN")

  val failedTasksAccu=sparkSession.sparkContext.collectionAccumulator[FailedTask]("failedTasks")
  val taskProblemsAccu=sparkSession.sparkContext.collectionAccumulator[TaskProblem]("taskProblems")
  val summaryAccu=new TaskSummaryAccumulator
  sparkSession.sparkContext.register(summaryAccu,"summaryAccumulator")
  val sc=sparkSession.sparkContext
  //val jobLoggerDAO=new JobLoggerDAO(keyspaceName)
  //jobLoggerDAO.insertJobLog(jobId,LogType.STAGE,LogSeverity.START,"Job started")
  println("JOB STARTED..."+jobName)
  summaryAccu.add(new TaskSummary(jobId, jobName, System.currentTimeMillis(),0))


  def finishJob()={
    println("FINISH JOB INSERT LOG")
    //jobLoggerDAO.insertJobLog(jobId,LogType.STAGE,LogSeverity.END,"Job started")
    summaryAccu.add(new TaskSummary("","", 0,System.currentTimeMillis()))
    println("SUMMARY:"+summaryAccu.value.toString)

    val resource=SparkApplicationConfig.conf.getString("elasticsearch.jobsIndex")
    //val resource=indexRecommendationDataName+"/"+similarUsersIndexType

    val rddFailed= sparkSession.sparkContext.makeRDD(failedTasksAccu.value.toArray)
    val rddProblems= sparkSession.sparkContext.makeRDD(taskProblemsAccu.value.toArray)
    val rddSummary= sparkSession.sparkContext.makeRDD(Seq(summaryAccu.value))
     val mapping=Map("es.mapping.id"->"jobId")
      EsSpark.saveToEs(rddFailed,resource+"/failed")
    EsSpark.saveToEs(rddProblems,resource+"/problems")
   EsSpark.saveToEs(rddSummary,resource+"/summary")
  }
}
