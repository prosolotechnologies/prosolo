package org.prosolo.bigdata.dal.cassandra.impl

import com.datastax.driver.core.Row
import org.prosolo.bigdata.scala.clustering.userprofiling.ClusterName
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable
/**
  * Created by zoran on 04/03/17.
  */
/**
  * zoran 04/03/17
  */
class ProfilesDAO (val dbName:String) extends Entity with Serializable{
  val logger = LoggerFactory.getLogger(getClass)
    override val keyspace=dbName

  def updateStudentProfileInCourse(profile:String, profileFullName:String, sequence:java.util.List[String], course:java.lang.Long, user:java.lang.Long):Unit={
    val query=s"UPDATE $keyspace." + TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE + " SET profile=?, profilefullname=?, sequence=? WHERE course=? AND userid=?;"
    DBManager.connector.withSessionDo { session ⇒
      session.execute(query, profile, profileFullName,sequence, course, user)
    }
  }

  def insertUserQuartileFeaturesByProfile(userProfile:Iterable[(Long,String,Long,Long,String)]): Unit ={
    val query = s"INSERT INTO $keyspace." + TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE + "(course,  profile,date, userid, sequence) VALUES (?, ?, ?,?,?) ";
    DBManager.connector.withSessionDo {
      session => {
        userProfile.foreach(record => {
          logger.debug("USER PROFILE RECORD:" + record._1 + " " + record._2 + " " + record._3 + " " + record._4 + " " + record._5)
          session.execute(query, record._1.asInstanceOf[java.lang.Long], record._2.asInstanceOf[String], record._3.asInstanceOf[java.lang.Long], record._4.asInstanceOf[java.lang.Long], record._5.asInstanceOf[String])
        })

      }
    }
  }
  def findUserQuartileFeaturesByProfile(courseId:java.lang.Long, clusterName:ClusterName.Value):List[Row] = {
    DBManager.connector.withSessionDo {
      session =>
        val rs = session.execute(s"SELECT * FROM $keyspace."+  TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE + " WHERE course=? and profile=? ",courseId ,clusterName.toString)

        val rows= rs.all().map {
          case row =>
            row
        }
        rows.toList
    }
  }
  def insertUserQuartileFeaturesByDate(userProfile:Iterable[(Long,String,Long,Long,String)]): Unit ={
    val bydatequery = s"INSERT INTO $keyspace." + TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE + "(course,  date, userid,profile, sequence) VALUES (?, ?, ?,?,?) ";
    DBManager.connector.withSessionDo {
      session => {
        userProfile.foreach(record => {
          logger.debug("USER PROFILE RECORD BY DATE:" + record._1 + " " + record._3 + " " + record._4 + " " + record._2 + " " + record._5)
          session.execute(bydatequery, record._1.asInstanceOf[java.lang.Long], record._3.asInstanceOf[java.lang.Long], record._4.asInstanceOf[java.lang.Long], record._2.asInstanceOf[String], record._5.asInstanceOf[String])
        })

      }
    }
  }
  def findUserQuartileFeaturesByDate(courseId:Long, endDateSinceEpoch:Long):List[(String,Long)] = {
    DBManager.connector.withSessionDo {
      session =>
        val rs = session.execute(s"SELECT * FROM $keyspace."+  TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE + " WHERE course=" + courseId + " AND date=" + endDateSinceEpoch)
        val rows= rs.all().map {
          case row =>
            (row.getString("sequence"), row.getLong("userid"))
        }
        rows.toList
    }
  }
  def findUserProfileObservationsByDate(date: java.lang.Long, courseId:java.lang.Long):List[Row] ={
    val query = s"SELECT date,  userid, attach,  progress,  comment,  creating,  evaluation,join,like, login ,posting,content_access,message,search FROM $keyspace." + TablesNames.PROFILE_USERPROFILE_ACTIONS_OBSERVATIONS_BYDATE +  " WHERE date=? and course=?"
    DBManager.connector.withSessionDo {
      session =>
        val rs = session.execute(query, date, courseId)
       val rows= rs.all().map {
          case row =>
            row
        }
        rows.toList
      }
  }


}
