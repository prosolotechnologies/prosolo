package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager._
import org.slf4j.LoggerFactory
import twitter4j._

/**
 * @author zoran Jul 26, 2015
 */
object StatusListener {
  val logger = LoggerFactory.getLogger(getClass)
  def listener=new StatusListener(){
    def onStatus(status: Status) {
      // logger.debug("ON STATUS:"+status.getText)
      logger.debug("Twitter status:"+status.getText)
        TwitterStatusBuffer.addStatus(status)
      }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) { ex.printStackTrace }
    def onScrubGeo(arg0: Long, arg1: Long) {}
    def onStallWarning(warning: StallWarning) {}
  }
}