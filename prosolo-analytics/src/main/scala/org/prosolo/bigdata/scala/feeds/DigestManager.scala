package org.prosolo.bigdata.scala.feeds

import org.slf4j.LoggerFactory

/**
 * @author zoran
 */
object DigestManager {
  
  val logger = LoggerFactory.getLogger(getClass)

  def createFeedDiggestsAndSendEmails(){
//    logger.debug("Create feed diggest and send emails called")
//    val cal:Calendar=Calendar.getInstance
//    cal.add(Calendar.DATE,0)
//     val yesterday:Date=cal.getTime
//    logger.debug("THIS IS TODAY. CHANGE TO YESTERDAY...")
//    val diggestGeneratorDAO=new DiggestGeneratorDAOImpl
//    val sc=SparkContextLoader.getSC
//
//    val usersIds:java.util.List[java.lang.Long] = diggestGeneratorDAO.getAllUsersIds
//    val usersIdsScala:Seq[java.lang.Long]=usersIds.asScala.toSeq
//     val scalaUsersIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(usersIds) //disabled for testing
//   val usersRDD:RDD[Long]=sc.parallelize(scalaUsersIds.map{Long2long})//temporary dispabled for testing
//     val coursesIds:java.util.List[java.lang.Long] = diggestGeneratorDAO.getAllActiveCoursesIds()
//    val coursesIdsScala:Seq[java.lang.Long]=coursesIds.asScala.toSeq
//     //val scalaCoursesIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(coursesIdsScala)
//     val coursesRDD:RDD[Long]=sc.parallelize(coursesIdsScala.map { Long2long})
//
//
//  val createDailyUserSubscribedRSSFeedDigests=(feedsAgregator:FeedsAgregator,userid:Long, date:Date)=>{
//     feedsAgregator.generateDailySubscribedRSSFeedsDigestForUser(userid, date)
//  }
//  createDailyUserDigest(yesterday, usersRDD, createDailyUserSubscribedRSSFeedDigests)
//
//  val createPersonalBlogs=(feedsAgregator:FeedsAgregator,userid:Long, date:Date)=>{
//    feedsAgregator.aggregatePersonalBlogOfUser(userid)
//  }
//  createDailyUserDigest(yesterday, usersRDD, createPersonalBlogs)
//
//  val createDailyFriendsRSSFeedDigests=(feedsAgregator:FeedsAgregator,userid:Long, date:Date)=>{
//     feedsAgregator.generateDailyFriendsRSSFeedDigest(userid, date)
//  }
//  createDailyUserDigest(yesterday, usersRDD, createDailyFriendsRSSFeedDigests)
//
//  val createDailySubscribedHashtagsDigests=(feedsAgregator:FeedsAgregator,userid:Long, date:Date)=>{
//     feedsAgregator.generateDailySubscribedTwitterHashtagsDigestForUser(userid, date)
//  }
//  createDailyUserDigest(yesterday, usersRDD, createDailySubscribedHashtagsDigests)
//
//   val generateDailyCourseRSSFeedsDigest=(feedsAgregator:FeedsAgregator, courseid:Long, date:Date)=>{
//   feedsAgregator.generateDailyCourseRSSFeedsDigest(courseid,date);
// }
//   createDailyCourseDigest(yesterday,coursesRDD,generateDailyCourseRSSFeedsDigest)
//
//   val generateDailySubscribedTwitterHashtagsDigestForUser=(feedsAgregator:FeedsAgregator, courseid:Long, date:Date)=>{
//   feedsAgregator.generateDailyCourseTwitterHashtagsDigest(courseid,date)
// }
//   createDailyCourseDigest(yesterday,coursesRDD,generateDailySubscribedTwitterHashtagsDigestForUser)
//
// val sendEmailWithFeedsForUser=(feedsAgregator:FeedsAgregator, userid:Long, date:Date)=>{
//   feedsAgregator.sendEmailWithFeeds(userid,date)
//
//  }
//  createDailyUserDigest(yesterday, usersRDD, sendEmailWithFeedsForUser)
//  }
//   /**
//   * Higher order function processing courses
//   */
//    private def createDailyCourseDigest(date:Date, coursesRDD:RDD[Long], f:(FeedsAgregator,Long, Date)=>Any){
//    coursesRDD.foreachPartition {
//       courses => {
//          val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
//          courses.foreach { courseid =>
//            {
//              f(feedsAgregator,courseid,date)
//            }
//          }
//       }
//    }
//    }
//
//  /**
//   * Higher order function processing users
//   */
// private def createDailyUserDigest(date:Date, usersRDD:RDD[Long], f:(FeedsAgregator,Long, Date)=>Any){
//   usersRDD.foreachPartition {
//       users =>  {
//         val feedsAgregator:FeedsAgregator =new FeedsAgregatorImpl
//         users.foreach {
//           userid =>
//             {
//              f(feedsAgregator,userid,date)
//             }
//
//       }
//     }
//   }
  }
  
 
 
}