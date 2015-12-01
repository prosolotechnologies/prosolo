package org.prosolo.bigdata.scala.clustering

import org.prosolo.common.domainmodel.activities.events.EventType
import org.prosolo.bigdata.events.pojo.LogEvent
import org.prosolo.bigdata.events.analyzers.ObservationType
import scala.collection.mutable.{ Buffer, ListBuffer, ArrayBuffer, Map, HashMap }
import java.io.InputStream
import scala.collection.JavaConversions._

object EventsChecker {
  val eventTypesFile = "files/events.csv"

  val eventsType: Map[String, Tuple3[ObservationType, EventType, String]] = new HashMap[String, Tuple3[ObservationType, EventType, String]]()
  val eventTypes: ListBuffer[EventType]=new ListBuffer[EventType]()
  def eventsTypeKey(eventType:EventType, objectType:String):String={
    if(objectType!=null && objectType.length()>0 ){
      eventType.name() + "_" + objectType
    }else{
      eventType.name()
    }
 
  }
  def initializeEventTypes() {
    val eventLines = readEventTypesFromFile(eventTypesFile)
    eventLines.foreach { line =>
      val cols: Array[String] = line.split(",").map(_.trim)
      val eventType: EventType = EventType.valueOf(cols(1))
      val objectType: String = if (cols.isDefinedAt(2)) cols(2) else ""
      val observationType: ObservationType = ObservationType.valueOf(cols(0))
     val key=eventsTypeKey(eventType,objectType)
       eventsType.put(key, (observationType, eventType, objectType))
       if(!eventTypes.contains(eventType)){
        eventTypes+=eventType
      } 
    }
  }
   initializeEventTypes()
  def getSupportedEventTypes()={
    val jlEventTypes:java.util.List[EventType]=eventTypes
    jlEventTypes
  }
  def isEventObserved(event:LogEvent):Boolean={
    eventsType.contains(eventsTypeKey(event.getEventType(),event.getObjectType))
   }
  def getObservationType(event:LogEvent):ObservationType={    
      eventsType.get(eventsTypeKey(event.getEventType(),event.getObjectType)).get._1
  }

  def readEventTypesFromFile(file: String): Array[String] = {
    val stream: InputStream = getClass.getClassLoader.getResourceAsStream(file)
    val lines: Array[String] = scala.io.Source.fromInputStream(stream).getLines.toArray
    lines
  }

 
}
