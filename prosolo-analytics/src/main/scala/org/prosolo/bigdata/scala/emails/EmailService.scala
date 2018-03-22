package org.prosolo.bigdata.scala.emails

import org.prosolo.bigdata.scala.spark.emails.{EmailSuccess, EmailSummary}
import org.slf4j.LoggerFactory

import scala.collection.mutable

trait EmailService[S<:EmailSummary] {
  val logger = LoggerFactory.getLogger(getClass)

  def sendEmail(emailSummary:S)
  def sendEmailBatches(emailBatch  : Array[S]):Tuple2[mutable.Map[String,EmailSuccess],mutable.Map[String,EmailSuccess]]

}
