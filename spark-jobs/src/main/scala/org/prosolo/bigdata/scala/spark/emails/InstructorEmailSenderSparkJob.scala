package org.prosolo.bigdata.scala.spark.emails



import org.prosolo.bigdata.dal.cassandra.impl.TablesNames
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkJob}
import com.datastax.spark.connector._
import org.apache.spark.rdd.RDD
//import org.prosolo.bigdata.services.email.InstructorStudentsEmailService
//import org.prosolo.bigdata.services.email.impl.InstructorStudentsEmailServiceImpl

import scala.collection.JavaConverters._

case class Record(courseId: java.lang.Long,
                  timestamp: java.lang.Long,
                  instructorId: java.lang.Long,
                  assigned: Seq[java.lang.Long],
                  unassigned: Seq[java.lang.Long])
case class CourseInstructorEmail(val courseId: Long, val instructorId: Long, val assigned:java.util.List[java.lang.Long], val unassigned:java.util.List[java.lang.Long])


class InstructorEmailSenderSparkJob(kName:String) extends SparkJob{
  val keyspaceName=kName
 // val sc = SparkContextLoader.getSC

  def runSparkJob(credentialsIds: java.util.List[java.lang.Long], dbName: String, bucket: Long): Array[CourseInstructorEmail] = {
    val scalaCourseIds: Seq[java.lang.Long] = credentialsIds.asScala.toSeq

    val courseRDD = sc.parallelize(scalaCourseIds).map(Tuple1(_))
      .repartitionByCassandraReplica(dbName, TablesNames.STUDENT_ASSIGN_EVENTS, 1)
   // val studentAssignManager = StudentAssignEventDBManagerImpl.getInstance
   val joinedCourseRDD = courseRDD
     .joinWithCassandraTable[Record](dbName, TablesNames.STUDENT_ASSIGN_EVENTS,
     SomeColumns("courseid" as "courseId", "timestamp", "instructorid" as "instructorId", "assigned", "unassigned"))
     .where("timestamp = " + bucket)
    //wait for all eventual updates in cassandra with old bucket value to finish
    Thread.sleep(2000)

    val emailsToSend:RDD[CourseInstructorEmail]=joinedCourseRDD.map {
          case (_, rec) =>
          {
            CourseInstructorEmail(rec.courseId, rec.instructorId,
              rec.assigned.asJava, rec.unassigned.asJava)
          }
    }
    emailsToSend.collect()
  }
}
