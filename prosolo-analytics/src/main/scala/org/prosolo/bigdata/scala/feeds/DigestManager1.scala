package org.prosolo.bigdata.scala.feeds

import java.util.{Calendar, Date}

import org.slf4j.LoggerFactory
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkManager}
import org.prosolo.bigdata.feeds.impl.FeedsAgregatorImpl
import org.prosolo.bigdata.feeds.FeedsAgregator

import scala.collection.mutable.Buffer
import org.apache.spark.rdd.RDD

import scala.collection.JavaConverters._
import org.hibernate.Session
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl
import java.time.LocalDateTime
import java.time.DayOfWeek

import org.apache.spark.Accumulator
import org.prosolo.common.util.date.DateUtil

object DigestManager1 {
  
  val logger = LoggerFactory.getLogger(getClass)
  
  
  def createFeedDigestsAndSendEmails() {
    logger.info("Create feed diggest and send emails called")
    val ldt = LocalDateTime.now()
    val createWeeklyDigest = ldt.getDayOfWeek == DayOfWeek.MONDAY
    val diggestGeneratorDAO=new DiggestGeneratorDAOImpl
    val courseDAO = new CourseDAOImpl(false)
    val sc=SparkManager.sparkContextLoader.getSC
     
    val credIds:java.util.List[java.lang.Long] = courseDAO.getAllCredentialIds
    val credIdsScala:Seq[java.lang.Long]=credIds.asScala.toSeq
    val credentialsRDD:RDD[Long]=sc.parallelize(credIdsScala.map { Long2long})
  
    val to = DateUtil.getYesterdayEndDateTime(ldt)
    val dailyFrom = DateUtil.getDayBeginningDateTime(to)
    val weeklyFrom = DateUtil.getNDaysAgoDayStartDateTime(to, 6);
    val generateDailyFeedDigestForCredential = (feedsAgregator:FeedsAgregator, 
        credId:Long, ldt:LocalDateTime)=>{
       //generate daily digest
       feedsAgregator.generateCredentialTwitterHashtagsDigest(credId, ldt, dailyFrom, to);
       //generate weekly digest
       if(createWeeklyDigest) {
         feedsAgregator.generateCredentialTwitterHashtagsDigest(credId, ldt, weeklyFrom, to);
       }
    }
    createCredentialDigest(ldt, credentialsRDD, generateDailyFeedDigestForCredential)
    
    val usersIds:java.util.List[java.lang.Long] = diggestGeneratorDAO.getAllUsersIds
    //val usersIdsScala:Seq[java.lang.Long]=usersIds.asScala.toSeq
    val scalaUsersIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(usersIds) //disabled for testing
    val usersRDD:RDD[Long]=sc.parallelize(scalaUsersIds.map{Long2long})//temporary dispabled for testing
    
    val sendEmailWithFeedsForUser=(feedsAgregator:FeedsAgregator, userid:Long, ldt:LocalDateTime)=>{
       feedsAgregator.sendEmailWithFeeds(userid, ldt, createWeeklyDigest);
    }
    sendEmails(ldt, usersRDD, sendEmailWithFeedsForUser)
  }
   /**
   * Higher order function processing credentials
   */
    private def createCredentialDigest(ldt:LocalDateTime, credentialsRDD:RDD[Long], 
        f:(FeedsAgregator,Long, LocalDateTime)=>Any) {
      credentialsRDD.foreachPartition { 
       credentials => {
          val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
          credentials.foreach { credId => 
            { 
              f(feedsAgregator, credId, ldt) 
            }
          }
       }
    }
    }
 
  /**
   * Higher order function processing users
   */
 private def sendEmails(ldt:LocalDateTime, usersRDD:RDD[Long], f:(FeedsAgregator,Long, LocalDateTime)=>Any){
   usersRDD.foreachPartition {       
       users =>  {
         val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
         users.foreach { 
           userid => 
             {
              f(feedsAgregator,userid,ldt)
             }
         }      
     }
   }
  }
  
 
 
}