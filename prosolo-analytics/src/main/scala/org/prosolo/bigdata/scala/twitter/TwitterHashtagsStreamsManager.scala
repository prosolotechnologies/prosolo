package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.twitter.{ PropertiesFacade, TwitterSiteProperties, StreamListData }

import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import org.prosolo.bigdata.spark.SparkLauncher
 
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.StreamingContext

import twitter4j.conf.ConfigurationBuilder
import twitter4j.{HashtagEntity, Status}

import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

/**
 * @author zoran
 */
object TwitterHashtagsStreamsManager {
  val propFacade = new PropertiesFacade()
  /** Credentials used to connect with Twitter user streams.*/
  var twitterProperties:java.util.Queue[TwitterSiteProperties] = propFacade.getAllTwitterSiteProperties
  val logger = LoggerFactory.getLogger(getClass)
  /** Keeps information about each hashtag and which users or learning goals are interested in it. Once nobody is interested in hashtag it can be removed   */
  var hashtagsAndReferences:collection.mutable.Map[String,StreamListData]=new collection.mutable.HashMap[String,StreamListData]
  /** Keeps reference between twitter stream by id and hashtags that we are listening within that stream   */
  var streamsAndHashtags:collection.mutable.Map[Int,Array[String]]=new collection.mutable.HashMap[Int,Array[String]]
  /** Keeps reference to twitter stream based on stream id, so we can restart stream  */
  var twitterStreams:collection.mutable.Map[Int,ReceiverInputDStream[Status]]=new collection.mutable.HashMap[Int,ReceiverInputDStream[Status]]
  /** Keeps track of twitter streams properties in use for later reuse*/
   var twitterStreamsPropertiesInUse:collection.mutable.Map[Int,TwitterSiteProperties]=new collection.mutable.HashMap[Int,TwitterSiteProperties]
  /** Twitter Stream can listen for maximum of 400 hashtags */
   val STREAMLIMIT=398
  var currentHashTagsList:Array[String]=new Array[String](STREAMLIMIT)
  var streamsCounter:Int=0
  var currentHashTagsStream:ReceiverInputDStream[Status]=null
 
  
  /**
   * At the applicaiton startup reads all hashtags from database and initialize required number of spark twitter streams to listen for it on Twitter
   */
  def initialize() {
    val filters = new Array[String](1)
    logger.info("INITIALIZE TWITTER STREAMING")
    val twitterDAO = new TwitterStreamingDAOImpl()
    hashtagsAndReferences=twitterDAO.readAllHashtagsAndLearningGoalsIds().asScala
    val hashTagsUserIds:collection.mutable.Map[String,java.util.List[java.lang.Long]]=twitterDAO.readAllUserPreferedHashtagsAndUserIds().asScala
 
    for((hashtag,userIds) <- hashTagsUserIds){
      println("key:"+hashtag+" value:"+userIds)
       val listData:StreamListData= hashtagsAndReferences.getOrElse(hashtag, new StreamListData(hashtag))
       listData.addUsersIds(userIds)
       hashtagsAndReferences.put(hashtag,listData)
    }
    println("hashtags and references:"+hashtagsAndReferences)
    startStreamsForHashTags
  }
  /**
   * Start streams for all hashtags pulled from database
   */
  def startStreamsForHashTags(){
    for((tag,streamListData) <-hashtagsAndReferences){
      currentHashTagsList.+("#"+tag)
      
      currentHashTagsList.size match {
        case x if x > STREAMLIMIT =>initializeNewCurrentHashTagsListAndStream
        case _ => 
      }
      
    }
    println("INITIALIZING STREAMING HERE")
    initializeNewCurrentHashTagsListAndStream
  }
  def initializeNewCurrentHashTagsListAndStream(){
    val (stream,ssc, streamId) =initializeNewStream(currentHashTagsList)
    streamsAndHashtags.put(streamId,currentHashTagsList)
    twitterStreams.put(streamId,stream)
    startStreamListening(stream,ssc)
    currentHashTagsStream=stream
  }
 
 /**
  * Returns next available Twitter configuration that can be used for new stream. 
  */
  def getTwitterConfigurationBuilder(): ConfigurationBuilder = {
    logger.info("INITIALIZE TWITTER BUILDER")
    var builder = new ConfigurationBuilder
    val siteProperties = twitterProperties.poll
    builder.setOAuthAccessToken(siteProperties.getAccessToken)
    builder.setOAuthAccessTokenSecret(siteProperties.getAccessTokenSecret)
    builder.setOAuthConsumerKey(siteProperties.getConsumerKey)
    builder.setOAuthConsumerSecret(siteProperties.getConsumerSecret)
    builder
  }
  /**
   * Initialize new stream for an array of hashtags
   */
  def initializeNewStream(filters: Array[String]):(ReceiverInputDStream[Status],StreamingContext, Int) ={
    val ssc = SparkLauncher.getSparkScalaStreamingContext()
    val config = getTwitterConfigurationBuilder.build()

    /* create the required authorization object for twitterStream */
    val auth: Option[twitter4j.auth.Authorization] = Some(new twitter4j.auth.OAuthAuthorization(config))
    val stream = TwitterUtils.createStream(ssc, auth, filters)   
    streamsCounter +=1
    (stream,ssc, streamsCounter-1)
  }
  /**
   * Listener for individual twitter stream
   */
  def startStreamListening(stream:ReceiverInputDStream[Status], ssc:StreamingContext){
     val statuses = stream.map(tweet => {
      (tweet.getUser.getName, tweet.getText, tweet.getHashtagEntities)
    })
    statuses.foreachRDD(rdd => {
      rdd.collect.foreach(t => {
        val username: String = t._1
        val text: String = t._2
        val hashtags: Array[HashtagEntity] = t._3
        println(t)
      })
    })
    ssc.start()
  }
  def addNewHashTagsAndRestartStream(hashtags:Array[String], userId:java.lang.Long, goalId:java.lang.Long){
    var changed=false; 
    for(hashtag <- hashtags){
      hashtagsAndReferences.contains(hashtag) match{
        case true => 
        case _ => currentHashTagsList.++:("#"+hashtag);changed=true
      }
       val listData:StreamListData= hashtagsAndReferences.getOrElseUpdate(hashtag, new StreamListData(hashtag, userId, goalId))
     }
    restartHashTagsStream(currentHashTagsStream,streamsCounter-1)
  }
  def restartHashTagsStream(stream:ReceiverInputDStream[Status], streamId:Int){
    println("Restarting stream id:"+streamId)
   stream.stop()
   //Returns site properties to queue for later reuse
   val siteproperties:Option[TwitterSiteProperties]=twitterStreamsPropertiesInUse.get(streamId)
   if(siteproperties.nonEmpty){
     twitterProperties.add(siteproperties.get)
   }   
   val newStream=initializeNewStream(currentHashTagsList)
   twitterStreams.put(streamId, newStream._1)
   
  }
}