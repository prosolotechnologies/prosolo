package org.prosolo.bigdata.scala.spark

import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark.rdd.EsSpark
import collection.JavaConverters._

/**
  * Created by zoran on 26/07/17.
  */
case class Trip(departure: String, arrival: String)
case class TaskSummary(var jobId:String,var jobName:String, var jobStarted:Long,var jobFinished:Long) extends Serializable
case class FailedTask (jobId:String,jobName:String, message:String, objectId:Long, taskType:String) extends Serializable
case class TaskProblem(jobId:String,jobName:String,message:String, objectId:Long, taskType:String, severity: String) extends Serializable
object ProblemSeverity extends Enumeration{
  val CRITICAL=Value("CRITICAL")
  val MAJOR=Value("MAJOR")
  val MINOR=Value("MINOR")
  val TRIVIAL=Value("TRIVIAL")
}
trait SparkJob extends Serializable{
  def keyspaceName:String
  val jobName=this.getClass.getSimpleName
  val jobId=java.util.UUID.randomUUID().toString
  println("RUNNING JOB:"+jobName+" WITH UUID:"+jobId)
 // val sc = SparkContextLoader.getSC
 // sc.setLogLevel("WARN")
 // val sparkSession:SparkSession=SparkContextLoader.getSparkSession
  val sparkSession:SparkSession=SparkManager.sparkContextLoader.getSparkSession
  val sc=sparkSession.sparkContext
  sparkSession.sparkContext.setLogLevel("WARN")

  val failedTasksAccu=sparkSession.sparkContext.collectionAccumulator[FailedTask]("failedTasks")
  val taskProblemsAccu=sparkSession.sparkContext.collectionAccumulator[TaskProblem]("taskProblems")
  val summaryAccu=new TaskSummaryAccumulator
  sc.register(summaryAccu,"summaryAccumulator")
 // val sc=sparkSession.sparkContext
  //val jobLoggerDAO=new JobLoggerDAO(keyspaceName)
  //jobLoggerDAO.insertJobLog(jobId,LogType.STAGE,LogSeverity.START,"Job started")
  println("JOB STARTED..."+jobName)
  summaryAccu.add(new TaskSummary(jobId, jobName, System.currentTimeMillis(),0))

  def submitFailedTask(message:String, objectId:Long, taskType:String): Unit ={
    failedTasksAccu.add(new FailedTask(jobId,jobName,message,objectId,taskType))
  }
  def submitTaskProblem(message:String, objectId:Long, taskType:String, severity: ProblemSeverity.Value): Unit ={
    taskProblemsAccu.add(new TaskProblem(jobId,jobName,message,objectId,taskType, severity.toString))
  }

  def finishJob()={
    summaryAccu.add(new TaskSummary("","", 0,System.currentTimeMillis()))
    val resource=SparkApplicationConfig.conf.getString("elasticsearch.jobsIndex")
    val failedTasks:Seq[FailedTask]=failedTasksAccu.value.asScala
    val mapping=Map("es.mapping.id"->"jobId")
   val rddFailed= sparkSession.sparkContext.makeRDD(failedTasks)
  val tasks:Seq[TaskProblem]= taskProblemsAccu.value.asScala
    val rddProblems= sparkSession.sparkContext.makeRDD(tasks)
    val rddSummary=sc.makeRDD(Seq(summaryAccu.value))
    EsSpark.saveToEs(rddFailed,resource+"/failed", mapping)
    EsSpark.saveToEs(rddProblems,resource+"/problems", mapping)
   EsSpark.saveToEs(rddSummary,resource+"/summary")
  }
}
