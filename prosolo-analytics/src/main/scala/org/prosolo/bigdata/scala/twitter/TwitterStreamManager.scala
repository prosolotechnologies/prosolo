package org.prosolo.bigdata.scala.twitter

import scala.collection.mutable.ArrayBuffer
import org.prosolo.bigdata.twitter.{ PropertiesFacade, TwitterSiteProperties, StreamListData }
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import scala.collection.JavaConverters._
import org.apache.spark.streaming.{StreamingContext,Seconds}
import org.prosolo.bigdata.spark.SparkLauncher
import twitter4j.conf.ConfigurationBuilder
//import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.dstream.DStream
import org.slf4j.LoggerFactory
import twitter4j.{HashtagEntity, Status}

/**
 * @author zoran Jul 24, 2015
 */
@deprecated
object TwitterStreamManager {
  val propFacade = new PropertiesFacade()
  /** Credentials used to connect with Twitter user streams.*/
  val twitterProperties:java.util.Queue[TwitterSiteProperties] = propFacade.getAllTwitterSiteProperties
  val logger = LoggerFactory.getLogger(getClass)
   val hashTagsList:ArrayBuffer[String]=new ArrayBuffer[String]()
   val ssc:StreamingContext = SparkLauncher.getSparkScalaStreamingContext()
   
    /** Keeps information about each hashtag and which users or learning goals are interested in it. Once nobody is interested in hashtag it can be removed   */
  val hashtagsAndReferences:collection.mutable.Map[String,StreamListData]=new collection.mutable.HashMap[String,StreamListData]

    /**
   * At the applicaiton startup reads all hashtags from database and initialize required number of spark twitter streams to listen for it on Twitter
   */
  def initialize() {
     logger.info("INITIALIZE TWITTER STREAMING")
    val twitterDAO = new TwitterStreamingDAOImpl()
      hashtagsAndReferences.++=(twitterDAO.readAllHashtagsAndLearningGoalsIds().asScala)
      println("HASHTAGS AND REFERENCES:"+hashtagsAndReferences)
       val hashTagsUserIds:collection.mutable.Map[String,java.util.List[java.lang.Long]]=twitterDAO.readAllUserPreferedHashtagsAndUserIds().asScala
 
    for((hashtag,userIds) <- hashTagsUserIds){
      println("key:"+hashtag+" value:"+userIds)
       val listData:StreamListData= hashtagsAndReferences.getOrElse(hashtag, new StreamListData(hashtag))
       listData.addUsersIds(userIds)
       hashtagsAndReferences.put(hashtag,listData)
    }
     println("hashtags and references:"+hashtagsAndReferences)
     hashtagsAndReferences.foreach(tag=>{
         hashTagsList+=("#"+tag._1)
     })
     initializeStreaming
  }
  def initializeStreaming(){
   val config = getTwitterConfigurationBuilder.build()
   val auth: Option[twitter4j.auth.Authorization] = Some(new twitter4j.auth.OAuthAuthorization(config))    
   val stream:DStream[Status]  = null//TwitterUtils.createStream(ssc, auth)  
   val filtered_statuses = stream.transform(rdd =>{
    val filtered = rdd.filter(status =>{
    var found = false        
        for(tag <- hashTagsList){            
          if(status.getText.toLowerCase.contains(tag)) {
            found = true
            }
        }
        found
      })
      filtered
    })
    filtered_statuses.foreachRDD(rdd => {
      rdd.collect.foreach(t => {
        println(t)
      })
   })
    ssc.start()
  }
  def updateFilter(){
    hashTagsList+="#android".toLowerCase

    println("UPDATED HASHTAGS TO:"+hashTagsList)
  }
  def getTwitterConfigurationBuilder(): ConfigurationBuilder = {
    println("INITIALIZE TWITTER BUILDER")
    val builder = new ConfigurationBuilder
    val siteProperties = twitterProperties.poll
    builder.setOAuthAccessToken(siteProperties.getAccessToken)
    builder.setOAuthAccessTokenSecret(siteProperties.getAccessTokenSecret)
    builder.setOAuthConsumerKey(siteProperties.getConsumerKey)
    builder.setOAuthConsumerSecret(siteProperties.getConsumerSecret)
    builder
  }
    /**
   * Receives an array of events from buffer and add it to stream, followed by stream restart
   */
 def addHashTagsFromBufferAndUpdateStream(eventsTuples:ArrayBuffer[Tuple4[ArrayBuffer[String],ArrayBuffer[String],Int,Int]]){
   println("Add hashtags from buffer and restart stream")
   for(eventTuple <- eventsTuples){
     val(addedHashtags, removedHashtags, userid, goalid)=eventTuple
      println("Added hashtags:"+addedHashtags.size+" added hashtags:"+addedHashtags+" event tuple:"+eventTuple._1)
      if(addedHashtags.nonEmpty){
        println("added hashtags not empty")
        addNewHashTags(addedHashtags,userid, goalid)
      }
      if(removedHashtags.nonEmpty)println("SHOULD REMOVE HASHTAGS")
   }
  }
 def addNewHashTags(hashtags:ArrayBuffer[String], userId:java.lang.Long, goalId:java.lang.Long){
    var changed=false; 
    for(hashtag <- hashtags){
      hashtagsAndReferences.contains(hashtag) match{
        case true => 
        case _ => hashTagsList+=("#"+hashtag); changed=true
      }
       val listData:StreamListData= hashtagsAndReferences.getOrElseUpdate(hashtag, new StreamListData(hashtag, userId, goalId))
     }
    println("New hashtags list:"+hashTagsList)
  }
 
}