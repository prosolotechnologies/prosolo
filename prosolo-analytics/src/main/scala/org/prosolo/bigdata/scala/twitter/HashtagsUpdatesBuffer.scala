package org.prosolo.bigdata.scala.twitter

import scala.collection.mutable.ListBuffer
import org.prosolo.bigdata.events.pojo.AnalyticsEvent
import java.util.{Timer, TimerTask}
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager.updateHashTagsFromBufferAndRestartStream
import org.prosolo.bigdata.scala.twitter.util.TwitterUtils._

/**
  * @author zoran Jul 18, 2015
  */
object HashtagsUpdatesBuffer {
  val buffer: ListBuffer[AnalyticsEvent] = ListBuffer()
  /** heartbeat scheduler timer. */
  private[this] val timer = new Timer("Hashtags Updates Monitor", true)
  if (Settings.getInstance.config.schedulerConfig.streamingJobs.twitterStreaming) {
    timer.scheduleAtFixedRate(new TimerTask {
      def run() {
        processBufferEvents
      }
    }, 1000, 10000)
  }

  def addEvent(event: AnalyticsEvent) {
    buffer += (event)
  }

  def pullEvents(): ListBuffer[AnalyticsEvent] = {
    var events: ListBuffer[AnalyticsEvent] = new ListBuffer[AnalyticsEvent]()
    events = events ++ buffer
    buffer.clear()
    events
  }

  def processBufferEvents() {
    val events = pullEvents
    val extractedEvents: ListBuffer[(ListBuffer[String], ListBuffer[String], Int, Int)] = extractHashTagsFromEvents(events)
    if (extractedEvents.size > 0) updateHashTagsFromBufferAndRestartStream(extractedEvents)
  }
}