package org.prosolo.bigdata.scala.instructorEmails

import java.util

import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.StudentAssignEventDBManagerImpl
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl
import org.prosolo.bigdata.scala.spark.emails.{CourseInstructorEmail, InstructorEmailSenderSparkJob}
import org.prosolo.bigdata.services.email.InstructorStudentsEmailService
import org.prosolo.bigdata.services.email.impl.InstructorStudentsEmailServiceImpl
import org.prosolo.common.config.CommonSettings
import org.slf4j.LoggerFactory
import collection.JavaConversions._

/**
  * Created by zoran on 17/04/17.
  */
object InstructorEmailManager  extends App{
runJob()
  def runJob(): Unit ={
    val logger = LoggerFactory.getLogger(getClass)
    logger.info("Instructor email sender job executing")

    val courseDAO = new CourseDAOImpl(false)
    val courseIds: java.util.List[java.lang.Long] = courseDAO.getAllCredentialIds
    val dbName = Settings.getInstance().config.dbConfig.dbServerConfig.dbName +
      CommonSettings.getInstance().config.getNamespaceSufix();
    val studentAssignManager = StudentAssignEventDBManagerImpl.getInstance
    //val bucket = studentAssignManager.getBucket;
     val bucket=7

    studentAssignManager.setBucket(bucket + 1)
    studentAssignManager.updateCurrentTimestamp(bucket + 1)

   val emailsToSend:Array[CourseInstructorEmail]= InstructorEmailSenderSparkJob.runSparkJob(courseIds,dbName, bucket)
    val emailService: InstructorStudentsEmailService = new InstructorStudentsEmailServiceImpl
    val emailsBatches:Array[Array[CourseInstructorEmail]]=emailsToSend.grouped(50).toArray
  // val emailsBatchesList:Array[java.util.Collection[CourseInstructorEmail]]= emailsBatches.map(batch=>util.Arrays.asList(batch))
    emailsBatches.foreach{
      emailBatch=>
        println("EMAIL BATCH")
        val emails: java.util.List[CourseInstructorEmail] = emailBatch.toSeq
        emailService.sendEmailsToInstructors(emails)
    }


  }
}
