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
//import org.prosolo.bigdata.dal.persistence.HibernateUtil
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
  // val session=HibernateUtil.getSessionFactory().openSession()
   // diggestGeneratorDAO.setSession(session)
    val sc=SparkContextLoader.getSC
    
    val usersIds:java.util.List[java.lang.Long] = diggestGeneratorDAO.getAllUsersIds
    val usersIdsScala:Seq[java.lang.Long]=usersIds.asScala.toSeq
   //  val scalaUsersIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(usersIds) //disabled for testing
  // val usersRDD:RDD[Long]=sc.parallelize(scalaUsersIds.map{Long2long})//temporary dispabled for testing
     
    val scalaUsersIds=Seq[Long](2,8,15,14,6)//Just for testing purposes    //testing
    val usersRDD:RDD[Long]=sc.parallelize(scalaUsersIds)
     
     
     val coursesIds:java.util.List[java.lang.Long] = diggestGeneratorDAO.getAllActiveCoursesIds()
    val coursesIdsScala:Seq[java.lang.Long]=coursesIds.asScala.toSeq
     //val scalaCoursesIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(coursesIdsScala)
     val coursesRDD:RDD[Long]=sc.parallelize(coursesIdsScala.map { Long2long})
     
    //
    
   
    
    println("CFD-7")
   // session.close
    
   // createDailyUserSubscribedRSSFeedDigests(yesterday, usersRDD)
   // createDailyFriendsRSSFeedDigests(yesterday, usersRDD)
   
     createDailyCoursesFeedsDigests(yesterday, coursesRDD)
     
    createDailySubscribedHashtagsDigests(yesterday)
    createDailyCourseHashtagsDigests(yesterday)
    sendEmailsWithFeedDigests()
    
  }
 
  private def createDailyUserSubscribedRSSFeedDigests(date:Date, usersRDD:RDD[Long]){
     usersRDD.foreachPartition {       
       users =>  {
         val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
         users.foreach { 
           userid => {
             println("STARTING USER:"+userid);
             feedsAgregator.generateDailySubscribedRSSFeedsDigestForUser(userid, date)}
           }
         
       }      
     }
  }
   private def createDailyFriendsRSSFeedDigests(date:Date, usersRDD:RDD[Long]){
    println("createDailyFriendsRSSFeedDigests")
     usersRDD.foreachPartition {       
       users =>  {
          val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
         users.foreach { 
            userid =>
              {
                println("STARTING FRIENDS OF USER:"+userid);
                feedsAgregator.generateDailyFriendsRSSFeedDigest(userid, date)
              }
            }  
         
       } 
     }
  }
/*  private def generateDailySubscribedRSSFeedsDigestForUsersInPartition(users:Iterator[Long],date:Date){
    val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
     users.foreach { userid => println("STARTING USER:"+userid);feedsAgregator.generateDailySubscribedRSSFeedsDigestForUser(userid, date)}
  }*/

  private def createDailyCoursesFeedsDigests(date:Date, coursesRDD:RDD[Long]){
     println("createDailyCourseFeedsDigests")
     coursesRDD.foreachPartition { 
       courses => {
          val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
          courses.foreach { courseid => 
            {
              println("STARTING COURSE FEED PROCESSING...");
               feedsAgregator.generateDailyCourseRSSFeedsDigest(courseid, date)
          } }
       }
      
     }
     
    
    
     
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