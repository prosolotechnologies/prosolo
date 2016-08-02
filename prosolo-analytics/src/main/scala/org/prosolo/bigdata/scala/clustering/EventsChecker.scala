package org.prosolo.bigdata.scala.clustering


import org.prosolo.common.domainmodel.activities.events.EventType
import org.prosolo.bigdata.events.pojo.LogEvent
import org.prosolo.bigdata.events.analyzers.ObservationType
import scala.collection.mutable.{ Buffer, ListBuffer, ArrayBuffer, Map, HashMap }
import java.io.InputStream
import scala.collection.JavaConversions._
trait EventsChecker{
  val eventTypesFile:String;
  val eventsType: Map[String, Tuple4[ObservationType, EventType, String, Double]] = new HashMap[String, Tuple4[ObservationType, EventType, String,Double]]()
  val eventTypes: ListBuffer[EventType]=new ListBuffer[EventType]()
  val objectTypes: ListBuffer[String]=new ListBuffer[String]()
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
      val weight:Double=if(cols.isDefinedAt(3)) cols(3).toDouble else 0.0
      val observationType: ObservationType = ObservationType.valueOf(cols(0))
      val key=eventsTypeKey(eventType,objectType)
      eventsType.put(key, (observationType, eventType, objectType, weight))
      if(!eventTypes.contains(eventType)){
        eventTypes+=eventType
      }
      if(!objectTypes.contains(objectType)){objectTypes+=objectType}
    }
  }

  def readEventTypesFromFile(file: String): Array[String] = {
    val stream: InputStream = getClass.getClassLoader.getResourceAsStream(file)
    val lines: Array[String] = scala.io.Source.fromInputStream(stream).getLines.toArray
    lines
  }
  def getSupportedEventTypes()={
    val jlEventTypes:java.util.List[EventType]=eventTypes
    jlEventTypes
  }
  def isEventObserved(event:LogEvent):Boolean={
    eventsType.contains(eventsTypeKey(event.getEventType(),event.getObjectType))
  }
  def isObjectTypeObserved(objectType:String):Boolean={
    objectTypes.contains(objectType)
  }
  def getObservationType(event:LogEvent):ObservationType={
    eventsType.get(eventsTypeKey(event.getEventType(),event.getObjectType)).get._1
  }
  def getEventWeight(event:LogEvent):Double={
    eventsType.get(eventsTypeKey(event.getEventType(),event.getObjectType)).get._4
  }
}
object ProfileEventsChecker extends EventsChecker{
  val eventTypesFile = "files/events.csv"
  initializeEventTypes()
 
}
object SNAEventsChecker extends EventsChecker {
  val eventTypesFile = "files/interactionsevents.csv"
  initializeEventTypes()
}

object StudentPreferenceChecker extends EventsChecker {
  val eventTypesFile = "files/studentpreferenceweights.csv"
  initializeEventTypes()
}
