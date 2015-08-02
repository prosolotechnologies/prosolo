package org.prosolo.bigdata.scala.twitter

import twitter4j._

/**
 * @author zoran Jul 26, 2015
 */
object StatusListener {
  def listener=new StatusListener(){
    def onStatus(status: Status) {
       // println("ON STATUS:"+status.getText)
        TwitterStatusBuffer.addStatus(status)
      }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) { ex.printStackTrace }
    def onScrubGeo(arg0: Long, arg1: Long) {}
    def onStallWarning(warning: StallWarning) {}
  }
}