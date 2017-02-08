package org.prosolo.bigdata.spark.scala.clustering

import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.dal.cassandra.impl.TablesNames
import com.datastax.spark.connector._
import org.apache.spark.SparkContext

import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import org.apache.spark.rdd.RDD
import play.api.libs.json.Json
import com.datastax.spark.connector._
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import scala.collection.Seq


/**
  * Created by zoran on 05/02/17.
  */
/**
  * zoran 05/02/17
  */
object UserProfileInteractionsSparkJob {

  val sc = SparkContextLoader.getSC

  def runSparkJob(credentialsIds:java.util.List[java.lang.Long],dbName:String):Unit={
    val credentialsIdsScala: Seq[java.lang.Long] = credentialsIds.asScala.toSeq
   /* val credentialsRDD: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })*/
    credentialsIdsScala.foreach { credentialid =>
      println("RUNNING USER PROFILE ANALYZER FOR credential:" + credentialid)
      runStudentInteractionsGeneralOverviewAnalysis(credentialid,dbName)
      runStudentInteractionsByTypeOverviewAnalysis(credentialid,dbName)
      println("FINISHED ANALYZER FOR credential:"+credentialid)
    }
  }
  case class SocialInteractionCount(credential:Long, source:Long, target:Long, count:Int)
  def runStudentInteractionsGeneralOverviewAnalysis(credentialId: Long,dbName:String) = {
    println("a1.. dbName:"+dbName+" credentialId:"+credentialId)
    val socialInteractionsbypeers_table =sc.cassandraTable(dbName, TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT).where("course=?",credentialId)
    val socialInteractionsCountDataRDD =socialInteractionsbypeers_table.map(row=> {
      new Tuple3(row.getLong("source"), row.getLong("target"), row.getLong("count")) })

  val mapOut = socialInteractionsCountDataRDD.map(t => {(t._1, ("OUT", t._2, t._3))}).groupByKey() //groups by studentid -> interactions TO to other students and count
    val mapIn = socialInteractionsCountDataRDD.map(t => (t._2, ("IN", t._1, t._3))).groupByKey() //groups by studentid -> interactions FROM other students and count
    val interUnions = mapOut.union(mapIn).reduceByKey(_ ++ _)

    val calculatedpercentage = interUnions.map(studinteractions => {
      val total = studinteractions._2.foldLeft(0l)((s: Long, t: Tuple3[String, Long, Long]) => s + t._3)
      val newtuple = studinteractions._2.map(t => {
        Tuple4(t._1, t._2, t._3, t._3.toFloat / total)
      })
      (studinteractions._1, newtuple)
    })
    val calculatedInteractionsForDB = calculatedpercentage.map {
      case (student, interactions) =>

        (student, interactions.map {
          i =>
            val jsonObject = Json.obj("direction" -> i._1, "peer" -> i._2, "count" -> i._3, "percentage" -> i._4)
            Json.stringify(jsonObject)
        }.toList)
    }.map{
      case(student, interactions) =>
        val studentinteractionbypeer=new StudentInteractionsInCourse(credentialId,student,interactions)
        studentinteractionbypeer
      //dbManager.insertStudentInteractionsByPeer(credentialId, student, interactions)
    }
    println("CALCULATED PERCENTAGE OF INTERACTIONS BY PEERS:" + calculatedInteractionsForDB.collect.size+" :"+calculatedInteractionsForDB.collect.mkString(", "))

    calculatedInteractionsForDB.saveToCassandra(dbName,TablesNames.SNA_STUDENT_INTERACTION_BYPEERS_OVERVIEW)



    // println("CALCULATED PERCENTAGE OF INTERACTIONS:" + calculatedpercentage.collect.mkString(", "))

  }
  case class StudentInteractionsInCourse(course:Long, student:Long, interactions:List[String])

  def runStudentInteractionsByTypeOverviewAnalysis(credentialId: Long,dbName:String) = {

    //val rows: java.util.List[Row] = dbManager.getSocialInteractionsByType(credentialId)
    val socialInteractionsbytype_table=sc.cassandraTable(dbName, TablesNames.SNA_STUDENT_INTERACTION_BYTYPE_FOR_STUDENT ).where("course=?",credentialId)
    val socialInteractionsCountDataRDD = socialInteractionsbytype_table.map(row=>new Tuple4(row.getLong("student"), row.getString("interactiontype"), row.getLong("fromuser"),  row.getLong("touser") ))
    println("FOUND ROWS:"+socialInteractionsCountDataRDD.collect.size+" for credential:"+credentialId)
    // val socialInteractionsCountData:Array[Tuple4[Long,String,Long,Long]]=rows.asScala.toArray.map{row: Row=>new Tuple4(row.getLong("student"), row.getString("interactiontype"), row.getLong("fromuser"), row.getLong("touser"))}
    //  println("data:"+socialInteractionsCountData.size)
    // val socialInteractionsCountDataRDD = sc.parallelize(socialInteractionsCountData)

    val socialInteractionsCountDataByStudent=socialInteractionsCountDataRDD.map(t=>(t._1,(t._2,t._3,t._4))).groupByKey
    println(socialInteractionsCountDataByStudent.collect.mkString(", "))
    val calculatedpercentage=socialInteractionsCountDataByStudent.map(studinteractions=>{
      val total=studinteractions._2.foldLeft(0l)((s:Long, t:Tuple3[String,Long,Long])=>s+t._2+t._3)
      val newtuple=studinteractions._2.map(t=>{
        Tuple5(t._1,t._2,t._2.toFloat/total,t._3,t._3.toFloat/total)
      })
      (studinteractions._1,newtuple)
    })
    val calculatedInteractionsForDB = calculatedpercentage.map {
      case (student, interactions) =>
        (student, interactions.map {
          i =>
            val jsonObject = Json.obj("type" -> i._1, "fromusercount" -> i._2, "fromuserpercentage" -> i._3, "tousercount" -> i._4,"touserpercentage"->i._5)
            Json.stringify(jsonObject)
        }.toList)
    }.map{
      case(student,interactions)=>
        val studentinteractionsbytype=new StudentInteractionsInCourse(credentialId,student,interactions)
        studentinteractionsbytype
    }
    /*.foreach{
    case(student, interactions) =>
      dbManager.insertStudentInteractionsByType(credentialId, student, interactions)
  }*/
    println("CALCULATED PERCENTAGE OF INTERACTIONS BY TYPE:" + calculatedInteractionsForDB.collect.mkString(", "))

    calculatedInteractionsForDB.saveToCassandra(dbName,TablesNames.SNA_STUDENT_INTERACTION_BYTYPE_OVERVIEW)



  }
}
