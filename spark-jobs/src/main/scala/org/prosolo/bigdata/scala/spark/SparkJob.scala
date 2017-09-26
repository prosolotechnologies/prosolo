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
    println("FINISH JOB INSERT LOG")
    submitTaskProblem("test",111,"type",ProblemSeverity.TRIVIAL)
    submitFailedTask("test",222,"nnn")
    //jobLoggerDAO.insertJobLog(jobId,LogType.STAGE,LogSeverity.END,"Job started")
    summaryAccu.add(new TaskSummary("","", 0,System.currentTimeMillis()))
    println("SUMMARY:"+summaryAccu.value.toString)

    val resource=SparkApplicationConfig.conf.getString("elasticsearch.jobsIndex")
    //val resource=indexRecommendationDataName+"/"+similarUsersIndexType
println("RESOURCE FAILED:"+failedTasksAccu.value.size())
   // val failedTasks=failedTasksAccu.value.asScala.map(task=>(task.jobId,task)).toMap
   //failedTasks.foreach(f=>println(f.toString))

    import org.elasticsearch.spark._
    //val mapping=Map("es.mapping.id"->"jobId")
   // val numbers = Map("one" -> 1, "two" -> 2, "three" -> 3)
   val airports = Map("arrival" -> "Otopeni", "SFO" -> "San Fran")
    val upcomingTrip = org.prosolo.bigdata.scala.spark.Trip("OTP", "SFO")
    val lastWeekTrip = org.prosolo.bigdata.scala.spark.Trip("MUC", "OTP")

    val rdd = sc.makeRDD(Seq(upcomingTrip, lastWeekTrip))
    EsSpark.saveToEs(rdd, resource+"spark/docs2")
   // sc.makeRDD(Seq(numbers,airports)).saveToEs(resource+"/failed");
    //val rddFailed= sparkSession.sparkContext.makeRDD(Seq(failedTasks)).saveToEs(resource+"/failed", mapping)
   // val rddProblems= sparkSession.sparkContext.makeRDD(Seq(taskProblemsAccu.value))
  //  val rddSummary= sparkSession.sparkContext.makeRDD(Seq(summaryAccu.value))

   // rddFailed.foreach(n=>println("FAILED:"+n.toString))
      //   rddFailed.saveToEs(rddFailed,resource+"/failed", mapping)
   // EsSpark.saveToEs(rddProblems,resource+"/problems", mapping)
  // EsSpark.saveToEs(rddSummary,resource+"/summary")
  }
}
