package org.prosolo.bigdata.dal.cassandra.impl

import java.util.List

import com.datastax.driver.core.Row
import org.slf4j.LoggerFactory

/**
  * Created by zoran on 30/04/17.
  */
class RecommendationsDAO (val dbName:String) extends Entity with Serializable{
  val logger = LoggerFactory.getLogger(getClass)
  override val keyspace: String = dbName
  def deleteStudentNew(user: Long) ={
    val query= s"DELETE FROM $keyspace." + TablesNames.USERRECOM_NEWUSERS + " WHERE userid=?; "
   // val query= s"SELECT * FROM $keyspace." + TablesNames.SNA_SOCIAL_INTERACTIONS_COUNT + " where course=?";
    DBManager.connector.withSessionDo {
      session =>
        val rs = session.execute(query,user.asInstanceOf[java.lang.Long])
        logger.debug("DELETED STUDENT NEW:"+user)

    }
  }

  def insertClusterUsers(cluster: Long, users: java.util.List[java.lang.Long]) {
    val query= s"INSERT INTO $keyspace." + TablesNames.USERRECOM_CLUSTERUSERS + "(cluster, users) VALUES(?,?); "
    DBManager.connector.withSessionDo { session ⇒
      session.execute(query, cluster.asInstanceOf[java.lang.Long], users)
      logger.debug("INSERT CLUSTER USERS:"+cluster+" users:"+users.toString)
    }


  }
}
