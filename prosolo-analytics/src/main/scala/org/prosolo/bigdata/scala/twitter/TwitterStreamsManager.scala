package org.prosolo.bigdata.scala.twitter

import twitter4j.{TwitterStream, TwitterStreamFactory, FilterQuery}

/**
  * @author zoran Aug 6, 2015
  */
trait TwitterStreamsManager {
  /** Twitter Stream can listen for maximum of 400 hashtags or users */
  val STREAMLIMIT = 398
  var streamsCounter: Int = 0

  def initialize()

  def initializeNewStream[T <: Any](filterQuery: FilterQuery): (TwitterStream, Int) = {
    val config = TwitterPropertiesHolder.getTwitterConfigurationBuilder.build()
    val twitterStream = new TwitterStreamFactory(config).getInstance
    twitterStream.addListener(StatusListener.listener)

    twitterStream.filter(filterQuery)
    streamsCounter += 1
    (twitterStream, streamsCounter - 1)
  }

  def restartStream(twitterStream: TwitterStream, filterQuery: FilterQuery) {
    //twitterStream:TwitterStream, streamId:Int){
    twitterStream.cleanUp()
    twitterStream.filter(filterQuery)
  }

}