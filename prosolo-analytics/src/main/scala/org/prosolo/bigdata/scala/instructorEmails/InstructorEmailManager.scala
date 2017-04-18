package org.prosolo.bigdata.scala.instructorEmails

import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.StudentAssignEventDBManagerImpl
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl
import org.prosolo.bigdata.scala.spark.emails.{CourseInstructorEmail, InstructorEmailSenderSparkJob}
import org.prosolo.bigdata.services.email.InstructorStudentsEmailService
import org.prosolo.bigdata.services.email.impl.InstructorStudentsEmailServiceImpl
import org.prosolo.common.config.CommonSettings
import org.slf4j.LoggerFactory

/**
  * Created by zoran on 17/04/17.
  */
object InstructorEmailManager  {

  def runJob(): Unit ={
    val logger = LoggerFactory.getLogger(getClass)
    logger.info("Instructor email sender job executing")

    val courseDAO = new CourseDAOImpl(false)
    val courseIds: java.util.List[java.lang.Long] = courseDAO.getAllCredentialIds
    val dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName +
      CommonSettings.getInstance().config.getNamespaceSufix();
    val studentAssignManager = StudentAssignEventDBManagerImpl.getInstance
    val bucket = studentAssignManager.getBucket;
    //val bucket=7

    studentAssignManager.setBucket(bucket + 1)
    studentAssignManager.updateCurrentTimestamp(bucket + 1)

   val emailsToSend:Array[CourseInstructorEmail]= InstructorEmailSenderSparkJob.runSparkJob(courseIds,dbName, bucket)
    val emailService: InstructorStudentsEmailService = new InstructorStudentsEmailServiceImpl
    emailsToSend.foreach { courseInstructorEmail =>
      emailService.sendEmailToInstructor(courseInstructorEmail.courseId, courseInstructorEmail.instructorId,courseInstructorEmail.assigned,courseInstructorEmail.unassigned)
    }



  }
}
