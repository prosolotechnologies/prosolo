package org.prosolo.bigdata.dal.cassandra.impl

import java.util
import java.util.List

import com.datastax.driver.core.Row

import scala.collection.JavaConversions._

/**
  * Created by zoran on 18/03/17.
  */
/**
  * zoran 18/03/17
  */
class SNAClustersDAO (val dbName:String) extends Entity with Serializable{
  override val keyspace=dbName

  def updateCurrentTimestamp(tableName:TableNames, timestamp:java.lang.Long): Unit ={
   val query= s"UPDATE $keyspace." + TablesNames.CURRENT_TIMESTAMPS + "  SET timestamp=? WHERE tablename=?"
    DBManager.connector.withSessionDo { session ⇒
      session.execute(query, timestamp, tableName.toString)
    }
  }

  def getSocialInteractions(credentialId: java.lang.Long): List[Row] ={
    val query= s"SELECT * FROM $keyspace." + TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT + " where course=?";
    DBManager.connector.withSessionDo {
      session =>
        val rs = session.execute(query,credentialId)
        val rows= rs.all().map {
          case row =>
            row
        }
        rows.toList
    }
  }
  def insertOutsideClusterInteractions(timestamp:java.lang.Long, credentialId:java.lang.Long, studentId: java.lang.Long, cluster: java.lang.Long, direction: String, interactions: util.List[String]): Unit ={

   val query= s"INSERT INTO $keyspace." + TablesNames.SNA_OUTSIDE_CLUSTER_INTERACTIONS + "(timestamp, course,  student,direction, cluster, interactions) VALUES(?,?,?,?,?,?) "
    DBManager.connector.withSessionDo { session ⇒
      session.execute(query, timestamp, credentialId,  studentId,direction, cluster, interactions)
    }
  }

  def insertInsideClusterInteractions(timestamp: java.lang.Long, credentialId: java.lang.Long, cluster: java.lang.Long, student: java.lang.Long, interactions: util.List[String]) {
    println("Insert Inside cluster keyspace:"+keyspace+" time:"+timestamp+" credential:"+credentialId+" cluster:"+cluster+" student:"+student);
    val query= s"INSERT INTO $keyspace." + TablesNames.SNA_INSIDE_CLUSTER_INTERACTIONS + "(timestamp, course, cluster, student, interactions) VALUES(?,?,?,?,?) "
    DBManager.connector.withSessionDo { session ⇒
      session.execute(query, timestamp, credentialId, cluster, student, interactions)
    }

  }

  def insertStudentCluster(timestamp: java.lang.Long, credentialId: java.lang.Long, student: java.lang.Long, cluster: java.lang.Long) {
    val query= s"INSERT INTO $keyspace." + TablesNames.SNA_STUDENT_CLUSTER + "(timestamp, course,  student,cluster) VALUES(?,?,?,?); "
    DBManager.connector.withSessionDo { session ⇒
      session.execute(query, timestamp, credentialId,  student,cluster)
    }
  }

}
