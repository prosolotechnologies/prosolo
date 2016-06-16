package org.prosolo.bigdata.scala.clustering.userprofiling

import com.datastax.driver.core.Row
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{SocialInteractionsStatements, SocialInteractionStatisticsDBManagerImpl}
import SocialInteractionsStatements._
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.common.config.CommonSettings
import scala.collection.JavaConverters._
import play.api.libs.json.Json
import com.datastax.spark.connector._
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionsStatements

/**
  * Created by zoran on 29/03/16.
  */
object UserProfileInteractionsManager{
  val clusteringDAOManager = new ClusteringDAOImpl
  val dbManager = SocialInteractionStatisticsDBManagerImpl.getInstance()
  val sc = SparkContextLoader.getSC
  val dbName = Settings.getInstance.config.dbConfig.dbServerConfig.dbName + CommonSettings.getInstance.config.getNamespaceSufix

  def runAnalyser() = {
    println("RUN ANALYZER")
    val credentialsIds = clusteringDAOManager.getAllCredentialsIds
    val credentialsIdsScala: Seq[java.lang.Long] = credentialsIds.asScala.toSeq
     val credentialsRDD: RDD[Long] = sc.parallelize(credentialsIdsScala.map {
      Long2long
    })
    credentialsIdsScala.foreach { credentialid =>
      println("RUNNING USER PROFILE ANALYZER FOR credential:" + credentialid)
      runStudentInteractionsGeneralOverviewAnalysis(credentialid)
      runStudentInteractionsByTypeOverviewAnalysis(credentialid)
      println("FINISHED ANALYZER FOR credential:"+credentialid)
    }
      /*credentialsRDD.foreachPartition {
       credentials => {
         credentials.foreach { credentialid => {
           println("RUNNING USER PROFILE ANALYZER FOR credential:" + credentialid)
           runStudentInteractionsGeneralOverviewAnalysis(credentialid)
           println("KT-1")
            runStudentInteractionsByTypeOverviewAnalysis(credentialid)
           println("FINISHED ANALYZER FOR credential:" + credentialid)
         }
         }
       }
     }*/
  }
  case class SocialInteractionCount(credential:Long, source:Long, target:Long, count:Int)
  def runStudentInteractionsGeneralOverviewAnalysis(credentialId: Long) = {
    println("a1 dbName:"+dbName+" credentialId:"+credentialId)
     val socialInteractionsbypeers_table =sc.cassandraTable(dbName, "sna_socialinteractionscount").where("course=?",credentialId)
    val socialInteractionsCountDataRDD =socialInteractionsbypeers_table.map(row=> {
      new Tuple3(row.getLong("source"), row.getLong("target"), row.getLong("count")) })
  //  println("SIZE:"+socialInteractions_table.toArray.size)
   // val rows=socialInteractions_table.map(r=>new SocialInteractionCount(r.getLong("credential"),r.getLong("source"),r.getLong("target"),r.getInt("count")))
    //val socialInteractionsCountDataRDD =socialInteractions_table.map(row=> {
   //  println("NEW ROW")
   //  new Tuple3(row.getLong("source"), row.getLong("target"), row.getLong("count")) })

    ///val rows: java.util.List[Row] = dbManager.getSocialInteractions(credentialId)
   // val socialInteractionsCountData: Array[Tuple3[Long, Long, Long]] = rows.toArray.map { row: Row => new Tuple3(row.getLong("source"), row.getLong("target"), row.getLong("count")) }
    //val socialInteractionsCountDataRDD = sc.parallelize(socialInteractionsCountData) //.map(tuple=>(tuple._1,))
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

     calculatedInteractionsForDB.saveToCassandra(dbName,"sna_studentinteractionbypeersoverview")



   // println("CALCULATED PERCENTAGE OF INTERACTIONS:" + calculatedpercentage.collect.mkString(", "))

  }
  case class StudentInteractionsInCourse(course:Long, student:Long, interactions:List[String])

  def runStudentInteractionsByTypeOverviewAnalysis(credentialId: Long) = {

    //val rows: java.util.List[Row] = dbManager.getSocialInteractionsByType(credentialId)
    val socialInteractionsbytype_table=sc.cassandraTable(dbName, "sna_interactionsbytypeforstudent").where("course=?",credentialId)
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

      calculatedInteractionsForDB.saveToCassandra(dbName,"sna_studentinteractionbytypeoverview")



  }

  //runAnalyser()

}
