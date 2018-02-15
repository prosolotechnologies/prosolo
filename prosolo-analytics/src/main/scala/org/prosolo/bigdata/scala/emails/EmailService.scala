package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.scala.spark.emails.EmailSummary
import org.slf4j.LoggerFactory

trait EmailService[S<:EmailSummary] {
  val logger = LoggerFactory.getLogger(getClass)

  def sendEmail(emailSummary:S)
  def sendEmailBatches(emailBatch  : Array[S])

}
