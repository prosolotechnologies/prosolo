package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.twitter.StreamListData
import org.prosolo.common.twitter.{PropertiesFacade, TwitterSiteProperties}
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
import scala.collection.mutable.Buffer
import org.prosolo.bigdata.dal.persistence.HibernateUtil
import org.hibernate.Session

/**
 * @author zoran
 */
object TwitterHashtagsStreamsManager extends TwitterStreamsManager{
 // val propFacade = new PropertiesFacade()
 
  val logger = LoggerFactory.getLogger(getClass)
  /** Keeps information about each hashtag and which users or learning goals are interested in it. Once nobody is interested in hashtag it can be removed   */
  val hashtagsAndReferences:collection.mutable.Map[String,StreamListData]=new collection.mutable.HashMap[String,StreamListData]
  /** Keeps reference to twitter stream based on stream id, and list of hashtags in filter  */
   val twitterStreamsAndHashtags:collection.mutable.Map[Int,(TwitterStream,ListBuffer[String])]=new collection.mutable.HashMap[Int,(TwitterStream,ListBuffer[String])]
 
  
 // val currentFilterList:ListBuffer[String]=new ListBuffer[String]
  
  
  
  /**
   * At the applicaiton startup reads all hashtags from database and initialize required number of spark twitter streams to listen for it on Twitter
   */
 def initialize() {
  //  val filters = new Array[String](1)
    logger.info("INITIALIZE TWITTER STREAMING")
    val twitterDAO = new TwitterStreamingDAOImpl()
    val session:Session= HibernateUtil.getSessionFactory().openSession()
    val hashtagsAndRefs:collection.mutable.Map[String,StreamListData]=twitterDAO.readAllHashtagsAndLearningGoalsIds(session).asScala
    hashtagsAndReferences++=hashtagsAndRefs;
    val hashTagsUserIds:collection.mutable.Map[String,java.util.List[java.lang.Long]]=twitterDAO.readAllUserPreferedHashtagsAndUserIds(session).asScala
   session.close();
    for((hashtag,userIds) <- hashTagsUserIds){
       val listData:StreamListData= hashtagsAndReferences.getOrElse(hashtag, new StreamListData(hashtag))
       listData.addUsersIds(userIds)
       hashtagsAndReferences.put(hashtag,listData)
    }
    
    startStreamsForInitialSetOfData
  }
  /**
   * Start streams for all hashtags pulled from database
   */
  def startStreamsForInitialSetOfData(){
     val currentFilterList:ListBuffer[String]=new ListBuffer[String]()
    for((tag,streamListData) <-hashtagsAndReferences){
      currentFilterList+=(tag)
      currentFilterList.size match {
        case x if x > STREAMLIMIT =>initializeNewCurrentListAndStream(currentFilterList)
        case _ => 
      }
      streamListData.setStreamId(streamsCounter)      
    }
   initializeNewCurrentListAndStream(currentFilterList)
  }
  def getLatestStreamAndList():Tuple2[TwitterStream,ListBuffer[String]]={
     twitterStreamsAndHashtags.get(streamsCounter-1) match{
      case None =>  null   
      case x:Option[(TwitterStream,ListBuffer[String])] => x.get
        
    }
  }
    def getLatestStreamList():ListBuffer[String]={
      twitterStreamsAndHashtags.get(streamsCounter-1) match{
      case None =>  new ListBuffer[String]
      case x:Option[(TwitterStream,ListBuffer[String])] => x.get._2
      
    }
  }
  def initializeNewCurrentListAndStream(newCurrentFilterList:ListBuffer[String]){
     if(newCurrentFilterList.size>0){
       val (stream, streamId):Tuple2[TwitterStream,Int] =initializeNewStream(newCurrentFilterList)
       twitterStreamsAndHashtags.put(streamId,(stream,newCurrentFilterList))
     }
     
  } 
  
  def addNewHashTags(hashtags:ListBuffer[String], userId:java.lang.Long, goalId:java.lang.Long):Boolean={
    var changed=false 
        val currentFilterList:ListBuffer[String]=getLatestStreamList
   
    
     
    for(hashtag <- hashtags){
       if(currentFilterList.size>STREAMLIMIT ){
         restartStream(getLatestStreamAndList._1, getLatestStreamAndList._2)
         initializeNewCurrentListAndStream(currentFilterList)
        changed=false
      }
      hashtagsAndReferences.contains(hashtag) match{        
        case true => 
        case _ => if(!currentFilterList.contains(hashtag)) {
             currentFilterList+=(hashtag); changed=true; 
        }
      }     
       val listData:StreamListData= hashtagsAndReferences.getOrElseUpdate(hashtag, new StreamListData(hashtag, userId, goalId))
       val streamid=if(streamsCounter==0) 0 else streamsCounter-1
       listData.setStreamId(streamid)
     }
      if(streamsCounter==0){
         initializeNewCurrentListAndStream(currentFilterList)
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
   println("updateHashTagsFromBufferAndRestartStream called. Current streamsCounter:"+streamsCounter+" streams size:"+twitterStreamsAndHashtags.size)
  val changedIds:ListBuffer[Int]=new ListBuffer[Int]()
   for(eventTuple <- eventsTuples){
     val addedHashtags:ListBuffer[String]= eventTuple._1
     val removedHashtags:ListBuffer[String]=eventTuple._2
      val userid:Int=eventTuple._3
      val goalid:Int=eventTuple._4
      println("ADDED :"+addedHashtags+" removed:"+removedHashtags)+"..."
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
 if(currentStreamChanged && streamsCounter>0){
   println("changed ids:"+changedIds)
    if(changedIds.contains(streamsCounter-1)){
      changedIds-=streamsCounter-1
    }
   // val currentFilterList:ListBuffer[String]=getLatestStreamAndList._2
   restartStream(getLatestStreamAndList._1, getLatestStreamAndList._2)
 } 
 if(!changedIds.isEmpty){
   for(streamid <- changedIds){
     val streamTagsTuple=twitterStreamsAndHashtags.get(streamid).get
     if(streamTagsTuple._2.size>0){
       restartStream(streamTagsTuple._1,streamTagsTuple._2)
     }else{
       terminateStream(streamTagsTuple._1)
       twitterStreamsAndHashtags.remove(streamid)
     }
      
    }
 }
  }
    /**
   * Initialize new stream for an array of hashtags
   */
  def initializeNewStream(filters: Buffer[String]):Tuple2[TwitterStream,Int]={
   // val filterQuery:FilterQuery=new FilterQuery().track(filters:_*)
    super.initializeNewStream(new FilterQuery().track(filters:_*))
  }
 
  def restartStream(twitterStream:TwitterStream, filters: ListBuffer[String]){//twitterStream:TwitterStream, streamId:Int){
      super.restartStream(twitterStream, new FilterQuery().track(filters:_*))
   }
  def terminateStream(twitterStream:TwitterStream){
     twitterStream.shutdown()

  }

  
}