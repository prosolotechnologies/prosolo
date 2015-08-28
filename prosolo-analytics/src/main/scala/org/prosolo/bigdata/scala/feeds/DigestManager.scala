package org.prosolo.bigdata.scala.feeds

import java.util.{Calendar,Date}
import org.slf4j.LoggerFactory
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.bigdata.feeds.impl.FeedsAgregatorImpl
import org.prosolo.bigdata.feeds.FeedsAgregator

import scala.collection.mutable.Buffer
import org.apache.spark.rdd.RDD
import scala.collection.JavaConverters._
import org.prosolo.bigdata.dal.persistence.HibernateUtil
import org.hibernate.Session

/**
 * @author zoran
 */
object DigestManager {
  
  val logger = LoggerFactory.getLogger(getClass)
  
  
  def createFeedDiggestsAndSendEmails(){
    println("Create feed diggest and send emails called")
    val cal:Calendar=Calendar.getInstance
    cal.add(Calendar.DATE,-1)
    val yesterday:Date=cal.getTime
    val diggestGeneratorDAO=new DiggestGeneratorDAOImpl
   val session=HibernateUtil.getSessionFactory().openSession()
    diggestGeneratorDAO.setSession(session)
    val usersIds:java.util.List[java.lang.Long] = diggestGeneratorDAO.getAllUsersIds
    val usersIdsScala:Seq[java.lang.Long]=usersIds.asScala.toSeq
    val scalaUsersIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(usersIds)
    val sc=SparkContextLoader.getSC
    val usersRDD:RDD[Long]=sc.parallelize(scalaUsersIds.map{Long2long})
    println("CFD-7")
   // session.close
    createDailyFriendsRSSFeedDigests(yesterday)
    createDailyUserSubscribedRSSFeedDigests(yesterday, usersRDD, session)
    createDailyCourseFeedsDigests(yesterday)
    createDailySubscribedHashtagsDigests(yesterday)
    createDailyCourseHashtagsDigests(yesterday)
    sendEmailsWithFeedDigests()
    
  }
  private def createDailyFriendsRSSFeedDigests(date:Date){
    println("createDailyFriendsRSSFeedDigests")
  }
  private def createDailyUserSubscribedRSSFeedDigests(date:Date, usersRDD:RDD[Long], session:Session){
     println("createDailyUserSubscribedRSSFeedDigests")
     usersRDD.foreachPartition {       
       users =>  generateDailySubscribedRSSFeedsDigestForUsersInPartition(users,date)       
     }
  }
  private def generateDailySubscribedRSSFeedsDigestForUsersInPartition(users:Iterator[Long],date:Date){
    val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
    // val session=HibernateUtil.getSessionFactory().openSession()
     users.foreach { userid => feedsAgregator.generateDailySubscribedRSSFeedsDigestForUser(userid, date)}
  }

  private def createDailyCourseFeedsDigests(date:Date){
     println("createDailyCourseFeedsDigests")
  }
  private def createDailySubscribedHashtagsDigests(date:Date){
     println("createDailySubscribedHashtagsDigests")
  }
  private def createDailyCourseHashtagsDigests(date:Date){
     println("createDailyCourseHashtagsDigests")
  }
  private def sendEmailsWithFeedDigests(){
     println("sendEmailsWithFeedDigests")
  }
}