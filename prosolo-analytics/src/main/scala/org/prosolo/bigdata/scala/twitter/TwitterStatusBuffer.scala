package org.prosolo.bigdata.scala.twitter

import java.util.{TimerTask, Timer}
import scala.collection.mutable.ListBuffer
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.bigdata.scala.messaging.BroadcastDistributer
import org.prosolo.common.messaging.data.{ServiceType=>MServiceType}
import org.prosolo.common.domainmodel.user.{User,AnonUser,ServiceType,UserType}
import org.prosolo.common.domainmodel.organization.VisibilityType
import twitter4j.Status
import org.prosolo.common.domainmodel.content.TwitterPost
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import org.prosolo.bigdata.dal.persistence.TwitterStreamingDAO
/**
 * @author zoran Jul 28, 2015
 */
object TwitterStatusBuffer {
    val buffer: ListBuffer[Status]=ListBuffer()
    val profanityFilter:BadWordsCensor=new BadWordsCensor
    //val twitterStreamingDao:TwitterStreamingDAO=new TwitterStreamingDAOImpl
    
  /** heartbeat scheduler timer. */
  private[this] val timer = new Timer("Statuses Updates Monitor", true)
  timer.scheduleAtFixedRate(new TimerTask {
    def run() {
       processBufferStatuses
    }
  }, 1000, 10000)
    def addStatus(status:Status){
    buffer+=(status)
    }
  def pullStatuses(): ListBuffer[Status]={
    var statuses:ListBuffer[Status]=new ListBuffer[Status]()
    statuses=statuses++buffer
    buffer.clear()
    statuses
  }
  
  def processBufferStatuses(){
    println("PRoces buffer statuses")
    val statuses=pullStatuses
    val sc=SparkContextLoader.getSC
    val statusesRDD=sc.parallelize(statuses)
    val filteredStatusesRDD=statusesRDD.filter{isAllowed }   
    filteredStatusesRDD.foreach { status:Status => {
     
      processStatus(status)
    } }
  }
  def isAllowed(status:Status):Boolean={
    val isPolite:Boolean=profanityFilter.isPolite(status.getText)
    isPolite
  }
  def processStatus(status:Status){
     val twitterUser=status.getUser
     val twitterHashtags:java.util.List[String]=new java.util.ArrayList[String]()
     status.getHashtagEntities.map { htent => twitterHashtags.add(htent.getText.toLowerCase) }
     val(twitterId,creatorName,screenName,profileImage)=(twitterUser.getId,twitterUser.getName,twitterUser.getScreenName,twitterUser.getProfileImageURL)
     val profileUrl="https://twitter.com/"+screenName
    val twitterStreamingDao:TwitterStreamingDAO=new TwitterStreamingDAOImpl
    var poster:User=null
     if({poster=twitterStreamingDao.getUserByTwitterUserId(twitterId);poster==null}){
       poster=initAnonUser(creatorName,profileUrl,screenName,profileImage)
     }
     
     val(text,created,postLink)=(status.getText,status.getCreatedAt,"https://twitter.com/" + twitterUser.getScreenName + "/status/" + status.getId)
      val statusText=text.replaceAll("[^\\x00-\\x7f-\\x80-\\xad]", "")
    val post:TwitterPost = twitterStreamingDao.createNewTwitterPost(poster, created, postLink, twitterId, creatorName, screenName, profileUrl, profileImage, statusText,VisibilityType.PUBLIC, twitterHashtags,true);
     val twitterPostSocialActivity=twitterStreamingDao.createTwitterPostSocialActivity(post)
     if(twitterPostSocialActivity !=null){
       val parameters:java.util.Map[String,String]=new java.util.HashMap[String,String]()
       parameters.put("socialActivityId", twitterPostSocialActivity.getId.toString())
       BroadcastDistributer.distributeMessage(MServiceType.BROADCAST_SOCIAL_ACTIVITY, parameters)
     }else{
       println("ERROR: TwitterPostSocialActivity was not initialized")
     }
    
     
  }
  def initAnonUser(creatorName:String,profileUrl:String,screenName:String, profileImage:String):AnonUser={
   val anonUser:AnonUser=new AnonUser
    anonUser.setName(creatorName)
    anonUser.setProfileUrl(profileUrl)
    anonUser.setNickname(screenName)
    anonUser.setAvatarUrl(profileImage)
    anonUser.setServiceType(ServiceType.TWITTER)
    anonUser.setUserType(UserType.TWITTER_USER)
    anonUser.setDateCreated(new java.util.Date)
    anonUser
  }
    
}