package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.twitter.{ PropertiesFacade, TwitterSiteProperties, StreamListData }
import org.prosolo.bigdata.events.pojo.AnalyticsEvent

import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import org.prosolo.bigdata.spark.SparkLauncher
 
import org.apache.spark.streaming.dstream.ReceiverInputDStream
//import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.StreamingContext

import twitter4j.{HashtagEntity, Status,TwitterStream,TwitterStreamFactory,FilterQuery}


import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * @author zoran
 */
object TwitterHashtagsStreamsManager extends TwitterStreamsManager{
 // val propFacade = new PropertiesFacade()
 
  val logger = LoggerFactory.getLogger(getClass)
  /** Keeps information about each hashtag and which users or learning goals are interested in it. Once nobody is interested in hashtag it can be removed   */
  var hashtagsAndReferences:collection.mutable.Map[String,StreamListData]=new collection.mutable.HashMap[String,StreamListData]
  /** Keeps reference to twitter stream based on stream id, and list of hashtags in filter  */
   val twitterStreamsAndHashtags:collection.mutable.Map[Int,(TwitterStream,ListBuffer[String])]=new collection.mutable.HashMap[Int,(TwitterStream,ListBuffer[String])]
 
  
  val currentHashTagsList:ListBuffer[String]=new ListBuffer[String]
  
 
  
  /**
   * At the applicaiton startup reads all hashtags from database and initialize required number of spark twitter streams to listen for it on Twitter
   */
 def initialize() {
  //  val filters = new Array[String](1)
    logger.info("INITIALIZE TWITTER STREAMING")
    val twitterDAO = new TwitterStreamingDAOImpl()
    hashtagsAndReferences=twitterDAO.readAllHashtagsAndLearningGoalsIds().asScala
    val hashTagsUserIds:collection.mutable.Map[String,java.util.List[java.lang.Long]]=twitterDAO.readAllUserPreferedHashtagsAndUserIds().asScala
 
    for((hashtag,userIds) <- hashTagsUserIds){
       val listData:StreamListData= hashtagsAndReferences.getOrElse(hashtag, new StreamListData(hashtag))
       listData.addUsersIds(userIds)
       hashtagsAndReferences.put(hashtag,listData)
    }
    startStreamsForHashTags
  }
  /**
   * Start streams for all hashtags pulled from database
   */
  def startStreamsForHashTags(){
    for((tag,streamListData) <-hashtagsAndReferences){
      currentHashTagsList+=(tag)
      currentHashTagsList.size match {
        case x if x > STREAMLIMIT =>initializeNewCurrentHashTagsListAndStream
        case _ => 
      }
      streamListData.setStreamId(streamsCounter)      
    }
    initializeNewCurrentHashTagsListAndStream
  }
  def initializeNewCurrentHashTagsListAndStream(){
    if(currentHashTagsList.size>0){
      val (stream, streamId) =initializeNewStream(currentHashTagsList)
      twitterStreamsAndHashtags.put(streamId,(stream,currentHashTagsList))
      currentHashTagsStream=stream
    }
  }
 

  /**
   * Initialize new stream for an array of hashtags
   */
  def initializeNewStream(filters: ListBuffer[String])={
    val config = TwitterPropertiesHolder.getTwitterConfigurationBuilder.build()
    val twitterStream = new TwitterStreamFactory(config).getInstance
    twitterStream.addListener(StatusListener.listener)
     val track:String=filters.mkString(",")
    twitterStream.filter(new FilterQuery().track(track))
    streamsCounter +=1
    (twitterStream,streamsCounter-1)
  }
  
  
  def addNewHashTags(hashtags:ListBuffer[String], userId:java.lang.Long, goalId:java.lang.Long):Boolean={
    var changed=false; 
    for(hashtag <- hashtags){
      hashtagsAndReferences.contains(hashtag) match{
        case true => 
        case _ => currentHashTagsList+=(hashtag); changed=true; 
      }
       val listData:StreamListData= hashtagsAndReferences.getOrElseUpdate(hashtag, new StreamListData(hashtag, userId, goalId))
       listData.setStreamId(streamsCounter-1)
     }
   changed
  }
  def removeHashTags(hashtags:ListBuffer[String], userId:java.lang.Long, goalId:java.lang.Long):ListBuffer[Int]={
 
    val changedIds:ListBuffer[Int]=new ListBuffer[Int]()
    for(hashtag <- hashtags){
      val listData:StreamListData= hashtagsAndReferences.get(hashtag).get
      if(goalId>0) listData.removeLearningGoalId(goalId)
      if(userId>0) listData.removeUserId(userId)
      if(listData.isFreeToRemove()) {
        val changedId=listData.getStreamId
        if(!changedIds.contains(changedId)){
          changedIds+=changedId
        }
        hashtagsAndReferences.remove(hashtag)        
        val streamHashtagsTuple:(TwitterStream,ListBuffer[String])=twitterStreamsAndHashtags.get(changedId).get
       val newhashtags:ListBuffer[String]= streamHashtagsTuple._2.filterNot(p => p==hashtag)
        println("SHOULD BE REMOVED HERE:"+hashtags+" hashtag:"+hashtag+" newhashtags:"+newhashtags)
       twitterStreamsAndHashtags.put(changedId, (streamHashtagsTuple._1,newhashtags))
       
      }
    }
    changedIds
  }
  /**
   * Receives an array of events from buffer and add it to stream, followed by stream restart
   */
 def updateHashTagsFromBufferAndRestartStream(eventsTuples:ListBuffer[Tuple4[ListBuffer[String],ListBuffer[String],Int,Int]]){
   var currentStreamChanged=false
  val changedIds:ListBuffer[Int]=new ListBuffer[Int]()
   for(eventTuple <- eventsTuples){
     val addedHashtags:ListBuffer[String]= eventTuple._1
     val removedHashtags:ListBuffer[String]=eventTuple._2
      val userid:Int=eventTuple._3
      val goalid:Int=eventTuple._4
      if(addedHashtags.nonEmpty){
        if(addNewHashTags(addedHashtags,userid, goalid)){
          currentStreamChanged=true
        }        
       }      
      if(removedHashtags.nonEmpty){
       val changedStreamsIds=removeHashTags(removedHashtags, userid, goalid)
           changedIds++= changedStreamsIds
      }
      
   }
   println("CURRENT STREAM CHANGED:"+currentStreamChanged+" CHANGED IDS:"+changedIds)
 if(currentStreamChanged){
   println("changed ids:"+changedIds)
    if(changedIds.contains(streamsCounter-1)){
      changedIds-=streamsCounter-1
    }
   restartHashTagsStream(currentHashTagsStream, currentHashTagsList)
 }
 if(!changedIds.isEmpty){
   for(streamid <- changedIds){
     println("Restarting stream :"+streamid)
     val streamTagsTuple=twitterStreamsAndHashtags.get(streamid).get
      restartHashTagsStream(streamTagsTuple._1,streamTagsTuple._2)
    }
 }
  }
    
  def restartHashTagsStream(twitterStream:TwitterStream, filters: ListBuffer[String]){//twitterStream:TwitterStream, streamId:Int){
  twitterStream.cleanUp()
   println("RESTART WITH:"+filters)
       val track:String=filters.mkString(",")
    twitterStream.filter(new FilterQuery().track(track))
  }
  
}