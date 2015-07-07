package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.twitter.{ PropertiesFacade, TwitterSiteProperties, StreamListData }
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import org.prosolo.bigdata.spark.SparkLauncher
 

import twitter4j.conf.ConfigurationBuilder
import org.apache.spark.streaming.twitter.TwitterUtils
import twitter4j.HashtagEntity

import org.slf4j.LoggerFactory

/**
 * @author zoran
 */
object TwitterHashtagsStreamsManager {
  val propFacade = new PropertiesFacade()
  val twitterProperties = propFacade.getAllTwitterSiteProperties
  val logger = LoggerFactory.getLogger(getClass)
  def initialize() {
    val filters = new Array[String](1)
    filters(0) = "music"
    logger.info("INITIALIZE TWITTER STREAMING")
    val twitterDAO = new TwitterStreamingDAOImpl()
    val hashtagsAndLearningGoals:java.util.Map[String, StreamListData]= twitterDAO.readAllHashtagsAndLearningGoalsIds()
   // initializeNewStream(filters)
  }
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
  def initializeNewStream(filters: Array[String]) {
    val ssc = SparkLauncher.getSparkScalaStreamingContext()
    val config = getTwitterConfigurationBuilder.build()

    /* create the required authorization object for twitterStream */
    val auth: Option[twitter4j.auth.Authorization] = Some(new twitter4j.auth.OAuthAuthorization(config))
    val tweets = TwitterUtils.createStream(ssc, auth, filters)
    val statuses = tweets.map(tweet => {
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
}