package org.prosolo.bigdata.spark.scala.clustering

import org.prosolo.bigdata.dal.cassandra.impl.TablesNames

import scala.collection.JavaConverters._
import play.api.libs.json.Json
import com.datastax.spark.connector._
import org.prosolo.bigdata.scala.spark.SparkJob
import org.slf4j.LoggerFactory

import scala.collection.Seq


/**
  * Created by zoran on 05/02/17.
  */

case class StudentInteractionsInCourse(course: Long, student: Long, interactions: List[String])

case class SocialInteractionCount(credential: Long, source: Long, target: Long, count: Int)

class UserProfileInteractionsSparkJob(kName: String) extends SparkJob {
  val keyspaceName = kName

  def runSparkJob(credentialsIds: java.util.List[java.lang.Long], dbName: String): Unit = {
    logger.debug("JOB NAME:" + jobName)
    val credentialsIdsScala: Seq[java.lang.Long] = credentialsIds.asScala.toSeq
    credentialsIdsScala.foreach { credentialid =>
      logger.debug("RUNNING USER PROFILE ANALYZER FOR credential:" + credentialid)
      runStudentInteractionsGeneralOverviewAnalysis(credentialid, dbName)
      runStudentInteractionsByTypeOverviewAnalysis(credentialid, dbName)
      logger.debug("FINISHED ANALYZER FOR credential:" + credentialid)
    }
  }

  def runStudentInteractionsGeneralOverviewAnalysis(credentialId: Long, dbName: String) = {
    logger.debug("dbName:" + dbName + " credentialId:" + credentialId)
    val socialInteractionsbypeers_table = sc.cassandraTable(dbName, TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT).where("course=?", credentialId)
    val socialInteractionsCountDataRDD = socialInteractionsbypeers_table.map(row => {
      new Tuple3(row.getLong("source"), row.getLong("target"), row.getLong("count"))
    })

    val mapOut = socialInteractionsCountDataRDD.map(t => {
      (t._1, ("OUT", t._2, t._3))
    }).groupByKey() //groups by studentid -> interactions TO to other students and count
    val mapIn = socialInteractionsCountDataRDD.map(t => (t._2, ("IN", t._1, t._3))).groupByKey() //groups by studentid -> interactions FROM other students and count
    val interUnions = mapOut.union(mapIn).reduceByKey(_ ++ _)
    val calculatedpercentage = interUnions.map(studinteractions => {
      val total = studinteractions._2.foldLeft(0l)((s: Long, t: (String, Long, Long)) => s + t._3)
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
    }
      .map {
        case (student, interactions) =>
          val studentinteractionbypeer = new StudentInteractionsInCourse(credentialId, student, interactions)
          studentinteractionbypeer
      }
    logger.debug("CALCULATED PERCENTAGE OF INTERACTIONS BY PEERS:" + calculatedInteractionsForDB.collect.size + " :" + calculatedInteractionsForDB.collect.mkString(", "))

    calculatedInteractionsForDB.saveToCassandra(dbName, TablesNames.SNA_STUDENT_INTERACTION_BYPEERS_OVERVIEW)
  }

  def runStudentInteractionsByTypeOverviewAnalysis(credentialId: Long, dbName: String) = {
    val socialInteractionsbytype_table = sc.cassandraTable(dbName, TablesNames.SNA_STUDENT_INTERACTION_BYTYPE_FOR_STUDENT).where("course=?", credentialId)
    val socialInteractionsCountDataRDD = socialInteractionsbytype_table.map(row => Tuple4(row.getLong("student"), row.getString("interactiontype"), row.getLong("fromuser"), row.getLong("touser")))
    logger.debug("FOUND ROWS:" + socialInteractionsCountDataRDD.collect.size + " for credential:" + credentialId)
    val socialInteractionsCountDataByStudent = socialInteractionsCountDataRDD.map(t => (t._1, (t._2, t._3, t._4))).groupByKey
    logger.debug(socialInteractionsCountDataByStudent.collect.mkString(", "))
    val calculatedpercentage = socialInteractionsCountDataByStudent.map(studinteractions => {
      val total = studinteractions._2.foldLeft(0l)((s: Long, t: (String, Long, Long)) => s + t._2 + t._3)
      val newtuple = studinteractions._2.map(t => {
        Tuple5(t._1, t._2, t._2.toFloat / total, t._3, t._3.toFloat / total)
      })
      (studinteractions._1, newtuple)
    })
    val calculatedInteractionsForDB = calculatedpercentage.map {
      case (student, interactions) =>
        (student, interactions.map {
          i =>
            val jsonObject = Json.obj("type" -> i._1, "fromusercount" -> i._2, "fromuserpercentage" -> i._3, "tousercount" -> i._4, "touserpercentage" -> i._5)
            Json.stringify(jsonObject)
        }.toList)
    }.map {
      case (student, interactions) =>
        val studentinteractionsbytype = new StudentInteractionsInCourse(credentialId, student, interactions)
        studentinteractionsbytype
      //interactions
    }
    /*.foreach{
    case(student, interactions) =>
      dbManager.insertStudentInteractionsByType(credentialId, student, interactions)
  }*/
    logger.debug("CALCULATED PERCENTAGE OF INTERACTIONS BY TYPE:" + calculatedInteractionsForDB.collect.mkString(", "))

    calculatedInteractionsForDB.saveToCassandra(dbName, TablesNames.SNA_STUDENT_INTERACTION_BYTYPE_OVERVIEW)


  }
}
