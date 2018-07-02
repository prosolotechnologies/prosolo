package org.prosolo.bigdata.scala.twitter.util

import scala.collection.mutable.ListBuffer
import com.google.gson.JsonObject
import org.prosolo.bigdata.events.pojo.AnalyticsEvent
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.slf4j.LoggerFactory

/**
  * @author zoran Jul 18, 2015
  */
object TwitterUtils {
  val logger = LoggerFactory.getLogger(getClass)

  def extractHashTagsFromEvents(events: ListBuffer[AnalyticsEvent]): ListBuffer[Tuple4[ListBuffer[String], ListBuffer[String], Int, Int]] = {
    var eventsTuples = new ListBuffer[Tuple4[ListBuffer[String], ListBuffer[String], Int, Int]]()
    for (event <- events) {
      val data: JsonObject = event.getData()
      val userid: Int = data.get("userid").getAsInt
      val goalid: Int = data.get("goalid").getAsInt
      val newhtags = data.get("newhashtags").getAsString
      val newhashtags: Array[String] = newhtags.toString().split(",").filter { x => x.length > 0 }.map {
        "#" + _.toString()
      }
      val oldhtags = data.get("oldhashtags").getAsString
      val oldhashtags: Array[String] = oldhtags.toString().split(",").filter { x => x.length > 0 }.map {
        "#" + _.toString()
      }
      var addedHashtags: ListBuffer[String] = new ListBuffer[String]()
      var removedHashtags: ListBuffer[String] = new ListBuffer[String]()
      for (nTag <- newhashtags) {
        if (!oldhashtags.contains(nTag)) {
          addedHashtags.+=(nTag)
        }
      }
      for (oTag <- oldhashtags) {
        if (!newhashtags.contains(oTag)) removedHashtags.+=(oTag)
      }
      eventsTuples.+=((addedHashtags, removedHashtags, userid, goalid));
      logger.debug("ADDED HASHTAGS TO PROCESS:" + addedHashtags.size + " REMOVED HASHTAGS:" + removedHashtags.size + " USER ID:" + userid + " GOAL ID:" + goalid)
    }
    eventsTuples
  }

}