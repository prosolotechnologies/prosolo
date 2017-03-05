package org.prosolo.bigdata.dal.cassandra.impl

/**
  * Created by zoran on 04/03/17.
  */
/**
  * zoran 04/03/17
  */
class ProfilesDAO (val dbName:String) extends Entity{
    override val keyspace=dbName

  def updateStudentProfileInCourse(profile:String, profileFullName:String, sequence:java.util.List[String], course:java.lang.Long, user:java.lang.Long):Unit={
    val query="UPDATE " + TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE + " SET profile=?, profilefullname=?, sequence=? WHERE course=? AND userid=?;"
    DBManager.connector.withSessionDo { session ⇒
      session.execute(s"UPDATE " + TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE + " SET profile=?, profilefullname=?, sequence=? WHERE course=? AND userid=?",
        profile, profileFullName,sequence, course, user)
    }
  }
}
