package org.prosolo.bigdata.scala.twitter

import twitter4j.{HashtagEntity, Status,TwitterStream,TwitterStreamFactory,FilterQuery}
/**
 * @author zoran Aug 6, 2015
 */
trait TwitterStreamsManager {
  /** Twitter Stream can listen for maximum of 400 hashtags or users*/
   val STREAMLIMIT=398
   var streamsCounter:Int=0
  var currentHashTagsStream:TwitterStream=null
  
  def initialize()
 
  
}