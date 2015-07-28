package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.twitter.{ PropertiesFacade, TwitterSiteProperties, StreamListData }
import org.prosolo.bigdata.events.pojo.AnalyticsEvent

import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import org.prosolo.bigdata.spark.SparkLauncher
 
import org.apache.spark.streaming.dstream.ReceiverInputDStream
//import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{StreamingContext,Seconds}
import org.apache.spark.SparkContext._

import twitter4j.conf.ConfigurationBuilder
import twitter4j.{HashtagEntity, Status}
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
  import org.apache.spark.SparkContext._
  import org.apache.spark.rdd.RDD
  import org.apache.spark.streaming.dstream.DStream
  //import org.apache.spark.streaming.Seconds
  import scala.reflect.ClassTag
 

/**
 * @author zoran Jul 21, 2015
 */
object TestStreaming extends App{
  val propFacade = new PropertiesFacade()
  /** Credentials used to connect with Twitter user streams.*/
  var twitterProperties:java.util.Queue[TwitterSiteProperties] = propFacade.getAllTwitterSiteProperties
  def initialize() {
    val filters = new Array[String](1)
   println("INITIALIZE TWITTER STREAMING")
   val ssc:StreamingContext = SparkLauncher.getSparkScalaStreamingContext()
// val sc: org.apache.spark.SparkContext=new org.apache.spark.SparkContext
    val config = getTwitterConfigurationBuilder.build()

    /* create the required authorization object for twitterStream */
    val auth: Option[twitter4j.auth.Authorization] = Some(new twitter4j.auth.OAuthAuthorization(config))    
  //  val stream = TwitterUtils.createStream(ssc, auth)   
      val stream:DStream[Status] = null//TwitterUtils.createStream(ssc, auth)   

 // val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#"))).map(x => (x.toLowerCase,1))
 val filtered_statuses = stream.transform(rdd =>{

      //Instead of hardcoding, you can fetch these from a MySQL or a file or whatever
      val sampleHashTags = Array("#teenchoice".toLowerCase,"#android".toLowerCase,"#iphone".toLowerCase)

      val filtered = rdd.filter(status =>{
        var found = false
        for(tag <- sampleHashTags){
          if(status.getText.toLowerCase.contains(tag)) found = true
        }
        found
      })
      

      filtered
    })
    filtered_statuses.foreachRDD(rdd => {
      rdd.collect.foreach(t => {
      //  val (username, text, hashtags)=t
        println(t)
      })
      })
     // stream.transform(rdd=>{
      //  val sampleHashTags = Array("#teenchoice".toLowerCase,"#android".toLowerCase,"#iphone".toLowerCase)
      //  val newRDD = sampleHashTags.map { x => (x,1) }
       // rdd.join(sampleHashTags)
    //  })
//    val topCounts10 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(1))
//      .map{case (topic, count) => (count, topic)}
//       .transform(_.sortByKey(false)).map(x => x._2)
//       println("TOP COUNTS")
//   topCounts10.print()
     
   /* val topList=  topCounts10.foreach(rdd => {
      val topList = rdd.take(500)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (count, tag) => println("%s (%s tweets)".format(tag, count))}
    })*/

//    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#"))).map(x => (x.toLowerCase,1))
//       val filteredStream = topCounts10.transform(rdd =>{
//      val samplehashtags = ssc.sparkContext.parallelize(Array("#music".toLowerCase,"#movie".toLowerCase,"#iphone".toLowerCase))
//      val newRDD = samplehashtags.map { x => (x,1) }
//      val joined = newRDD.join(rdd)  
//
//      joined
//         }) 
  println("FILTERED STREAM")
  //  filteredStream.print()
 
     
   /* val statuses = stream.map(tweet => {
      (tweet.getUser.getName, tweet.getText, tweet.getHashtagEntities)
    })
    statuses.foreachRDD(rdd => {
      rdd.collect.foreach(t => {
        val (username, text, hashtags)=t
        println(t)
      })
    })*/
    ssc.start()
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
  initialize
}