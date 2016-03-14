package org.prosolo.bigdata.scala.instructorEmails

import org.slf4j.LoggerFactory
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl
import scala.collection.mutable.Buffer
import org.apache.spark.rdd.RDD
import scala.collection.JavaConverters._
import com.datastax.spark.connector._
import org.prosolo.bigdata.config.Settings
import org.prosolo.common.config.CommonSettings
import org.prosolo.bigdata.services.instructorEmail.InstructorStudentsEmailService
import org.prosolo.bigdata.services.instructorEmail.impl.InstructorStudentsEmailServiceImpl
import org.prosolo.bigdata.dal.cassandra.impl.StudentAssignEventDBManagerImpl

object InstructorEmailSender {
  
  val logger = LoggerFactory.getLogger(getClass)

  case class Record(courseId: java.lang.Long,
                    timestamp: java.lang.Long,
                    instructorId: java.lang.Long, 
                    assigned: Seq[java.lang.Long], 
                    unassigned: Seq[java.lang.Long])
  
  def sendEmailsToInstructors() {
    try {
      logger.info("Instructor email sender job executing")
      
      val courseDAO = new CourseDAOImpl
  
      val sc = SparkContextLoader.getSC
      
      val dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName + 
          CommonSettings.getInstance().config.getNamespaceSufix();
      
      val courseIds: java.util.List[java.lang.Long] = courseDAO.getAllCourseIds
      if(courseIds != null && !courseIds.isEmpty()) {
        val scalaCourseIds: Seq[java.lang.Long] = courseIds.asScala.toSeq
        
        val courseRDD = sc.parallelize(scalaCourseIds).map(Tuple1(_))
          .repartitionByCassandraReplica(dbName, "student_assign_events", 1)
        val studentAssignManager = StudentAssignEventDBManagerImpl.getInstance;
    
        val bucket = studentAssignManager.getBucket;
        studentAssignManager.setBucket(bucket + 1)
        studentAssignManager.updateCurrentTimestamp(bucket + 1)
        
        val joinedCourseRDD = courseRDD
          .joinWithCassandraTable[Record](dbName, "student_assign_events", 
              SomeColumns("courseid" as "courseId", "timestamp", "instructorid" as "instructorId", "assigned", "unassigned"))
              .where("timestamp = " + bucket)
        
        //wait for all updates in cassandra with old bucket value to finish
        Thread.sleep(2000)
        
        sendEmails(joinedCourseRDD)
      }
    } catch {
       case e: Exception => {
         e.printStackTrace()
       }
    }
  }

  private def sendEmails(courseRDD: RDD[(Tuple1[java.lang.Long], Record)]) {
    courseRDD.foreachPartition {
      partitionRecords =>
        {
          val emailService: InstructorStudentsEmailService = new InstructorStudentsEmailServiceImpl
          partitionRecords.foreach {
            case (_, rec) =>
              {
                emailService.sendEmailToInstructor(rec.courseId, rec.instructorId, 
                   rec.assigned.asJava, rec.unassigned.asJava)
              }
          }
        }
    }
  } 

}