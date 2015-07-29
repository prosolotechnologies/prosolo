package org.prosolo.bigdata.scala.twitter

import java.util.{TimerTask, Timer}
import scala.collection.mutable.ListBuffer
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import twitter4j.Status
/**
 * @author zoran Jul 28, 2015
 */
object TwitterStatusBuffer {
    val buffer: ListBuffer[Status]=ListBuffer()
    val profanityFilter:BadWordsCensor=new BadWordsCensor
    
  /** heartbeat scheduler timer. */
  private[this] val timer = new Timer("Statuses Updates Monitor", true)
  timer.scheduleAtFixedRate(new TimerTask {
    def run() {
     //processBufferEvents
    }
  }, 1000, 10000)
    def addStatus(status:Status){
    buffer+=(status)
    }
  def pullStatuses(): ListBuffer[Status]={
    var statuses:ListBuffer[Status]=new ListBuffer[Status]()
    statuses=statuses++buffer
    buffer.clear()
    statuses
  }
  
  def processBufferStatuses(){
    val statuses=pullStatuses
    val sc=SparkContextLoader.getSC
    val statusesRDD=sc.parallelize(statuses)
    val filteredStatusesRDD=statusesRDD.filter{isAllowed }    
  }
  def isAllowed(status:Status):Boolean={
    val isPolite:Boolean=profanityFilter.isPolite(status.getText)
    isPolite
  }
    
}