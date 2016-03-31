package org.prosolo.bigdata.scala.clustering.userprofiling

import com.datastax.driver.core.Row
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import scala.collection.JavaConverters._

/**
  * Created by zoran on 29/03/16.
  */
object UserProfileInteractionsManager {
  val clusteringDAOManager = new ClusteringDAOImpl
  val dbManager = SocialInteractionStatisticsDBManagerImpl.getInstance()
  val sc = SparkContextLoader.getSC

  def runAnalyser() = {
    println("RUN ANALYZER")

    val coursesIds = clusteringDAOManager.getAllCoursesIds
    val coursesIdsScala: Seq[java.lang.Long] = coursesIds.asScala.toSeq
    val coursesRDD: RDD[Long] = sc.parallelize(coursesIdsScala.map {
      Long2long
    })
    coursesRDD.foreachPartition {
      courses => {
        courses.foreach { courseid => {
          println("RUNNING USER PROFILE ANALYZER FOR COURSE:" + courseid)
           runStudentInteractionsGeneralOverviewAnalysis(courseid)
          runStudentInteractionsByTypeOverviewAnalysis(courseid)
        }
        }
      }
    }
  }

  def runStudentInteractionsGeneralOverviewAnalysis(courseId: Long) = {
    val rows: java.util.List[Row] = dbManager.getSocialInteractions(courseId)
    val socialInteractionsCountData: Array[Tuple3[Long, Long, Long]] = rows.asScala.toArray.map { row: Row => new Tuple3(row.getLong("source"), row.getLong("target"), row.getLong("count")) }
    val socialInteractionsCountDataRDD = sc.parallelize(socialInteractionsCountData) //.map(tuple=>(tuple._1,))
    val mapOut = socialInteractionsCountDataRDD.map(t => (t._1, ("OUT", t._2, t._3))).groupByKey() //groups by studentid -> interactions TO to other students and count
    val mapIn = socialInteractionsCountDataRDD.map(t => (t._2, ("IN", t._1, t._3))).groupByKey() //groups by studentid -> interactions FROM other students and count
    val interUnions = mapOut.union(mapIn).reduceByKey(_ ++ _)
    val calculatedpercentage = interUnions.map(studinteractions => {
      val total = studinteractions._2.foldLeft(0l)((s: Long, t: Tuple3[String, Long, Long]) => s + t._3)
      val newtuple = studinteractions._2.map(t => {
        Tuple4(t._1, t._2, t._3, t._3.toFloat / total)
      })
      (studinteractions._1, newtuple)
    })
    println("CALCULATED PERCENTAGE OF INTERACTIONS:" + calculatedpercentage.collect.mkString(", "))
  }
  def runStudentInteractionsByTypeOverviewAnalysis(courseId: Long) = {

    val rows: java.util.List[Row] = dbManager.getSocialInteractionsByType(courseId)
    println("FOUND ROWS:"+rows.size()+" for course:"+courseId)
    val socialInteractionsCountData:Array[Tuple4[Long,String,Long,Long]]=rows.asScala.toArray.map{row: Row=>new Tuple4(row.getLong("student"), row.getString("interactiontype"), row.getLong("fromuser"), row.getLong("touser"))}
    println("data:"+socialInteractionsCountData.size)
    val socialInteractionsCountDataRDD = sc.parallelize(socialInteractionsCountData)

    val socialInteractionsCountDataByStudent=socialInteractionsCountDataRDD.map(t=>(t._1,(t._2,t._3,t._4))).groupByKey
    println(socialInteractionsCountDataByStudent.collect.mkString(", "))
    val calculatedpercentage=socialInteractionsCountDataByStudent.map(studinteractions=>{
      val total=studinteractions._2.foldLeft(0l)((s:Long, t:Tuple3[String,Long,Long])=>s+t._2+t._3)
      println("student:"+studinteractions._1+" has total:"+total)
      val newtuple=studinteractions._2.map(t=>{
        Tuple5(t._1,t._2,t._2.toFloat/total,t._3,t._3.toFloat/total)
      })
      (studinteractions._1,newtuple)
    })
    println("CALCULATED PERCENTAGE OF INTERACTIONS BY TYPE:" + calculatedpercentage.collect.mkString(", "))
  }

  //runAnalyser()

}
