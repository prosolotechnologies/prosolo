package org.prosolo.bigdata.scala.twitter
import com.google.gson.JsonObject
import org.prosolo.bigdata.events.pojo.AnalyticsEvent
import scala.collection.mutable.ListBuffer
import twitter4j.{HashtagEntity, Status,TwitterStream,TwitterStreamFactory,FilterQuery}
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import scala.collection.mutable.Buffer
/**
 * @author zoran Aug 6, 2015
 */
object TwitterUsersStreamsManager extends TwitterStreamsManager {
  /** Keeps information about each twitter user and which stream his account is followed in   */
  val usersAndStreamsIds: collection.mutable.Map[Long, Int] = new collection.mutable.HashMap[Long, Int]
  val twitterStreamsAndUsers: collection.mutable.Map[Int, (TwitterStream, ListBuffer[Long])] = new collection.mutable.HashMap[Int, (TwitterStream, ListBuffer[Long])]
  //val currentFilterList:Buffer[Long]= new ListBuffer[Long]

  /**
   * At the application startup reads all users who registered their Twitter accounts to start following them on the Twitter
   */
  def initialize() {
     val twitterDAO = new TwitterStreamingDAOImpl()
     val twitterIds:java.util.List[java.lang.Long]=twitterDAO.getAllTwitterUsersTokensUserIds
     val scalaTwitterIds:Buffer[java.lang.Long]= scala.collection.JavaConversions.asScalaBuffer(twitterIds)
     startStreamsForInitialSetOfData(scalaTwitterIds.map { Long2long})
  }
 
  def startStreamsForInitialSetOfData(map:Buffer[Long]){     
     
    val currentFilterList:ListBuffer[Long]= new ListBuffer[Long]
    for(twitterId<-map){
      println("adding twitter id:"+twitterId)
      currentFilterList+=twitterId
      currentFilterList.size match {
        case x if x > STREAMLIMIT => initializeNewCurrentListAndStream(currentFilterList)
        case _ =>
      }
    }
  initializeNewCurrentListAndStream (currentFilterList)
  }
  def getLatestStreamAndList():Tuple2[TwitterStream,ListBuffer[Long]]={
    twitterStreamsAndUsers.get(streamsCounter-1) match{
       case None =>  null  
       case x:Option[(TwitterStream,ListBuffer[Long])] => x.get
    }
  }
 def getLatestStreamList():ListBuffer[Long]={
      twitterStreamsAndUsers.get(streamsCounter-1) match{
      case None =>  new ListBuffer[Long]
      case x:Option[(TwitterStream,ListBuffer[Long])] => x.get._2
      
    }
  }
   
  def initializeNewCurrentListAndStream(newCurrentFilterList:ListBuffer[Long]){
    println("INITIALIZE NEW CURRENT LIST AND STREAM")
     //val newCurrentFilterList:ListBuffer[Long]=new ListBuffer[Long]
     if(newCurrentFilterList.size>0){
      val (stream, streamId):Tuple2[TwitterStream,Int] =initializeNewStream(newCurrentFilterList)
      twitterStreamsAndUsers.put(streamId,(stream,newCurrentFilterList))
      newCurrentFilterList.foreach { userid => usersAndStreamsIds.put(userid, streamId) }
     }
    //currentFilterList.remove(0, currentFilterList.size)
  }
  /**
   * Receives analytical event from Prosolo when user register or de-register his Twitter account
   */
  def updateTwitterUserFromAnalyticsEvent(event: AnalyticsEvent){
    val data:JsonObject=event.getData()
    val userid:Long=data.get("twitterId").getAsLong
    val shouldAdd:Boolean=data.get("add").getAsBoolean
    if(shouldAdd)addNewTwitterUser(userid) else removeTwitterUser(userid)
  }
  private def addNewTwitterUser(userid:Long){
    println("Adding new twitter user to follow:"+userid)
    if(!usersAndStreamsIds.contains(userid)){
      println("User is not followed. Start following now")
        val currentFilterList:ListBuffer[Long]= getLatestStreamAndList._2
        currentFilterList+=userid
       if(currentFilterList.size>STREAMLIMIT){
        println("SHOULD RESTART OLD IN THIS CASE")
        restartStream(getLatestStreamAndList._1,getLatestStreamAndList._2)
         initializeNewCurrentListAndStream(currentFilterList)
        // currentFilterList.remove(0,currentFilterList.size);
      }else{
        restartStream(getLatestStreamAndList._1,getLatestStreamAndList._2)
      }
    }
  }
  private def removeTwitterUser(userid:Long){
     println("Removing twitter user to follow:"+userid)
      if(usersAndStreamsIds.contains(userid)){
          println("User found should be removed now:"+userid)
          val streamId=usersAndStreamsIds.get(userid);
          val streamUsersTuple:(TwitterStream,ListBuffer[Long])= twitterStreamsAndUsers.get(streamId.get).get
          val newusers:ListBuffer[Long]= streamUsersTuple._2.filterNot(p => p==userid)
          println("SHOULD BE REMOVED HERE USERS:"+userid+" new users:" +newusers)
          twitterStreamsAndUsers.put(streamId.get,(streamUsersTuple._1,newusers))
          usersAndStreamsIds.remove(userid)
          restartStream(streamUsersTuple._1, newusers)
      }
  }
  
    /**
   * Initialize new stream for an array of users ids
   */
  def initializeNewStream(filters: Buffer[Long]):Tuple2[TwitterStream,Int]={
   val filterQuery:FilterQuery=new FilterQuery().follow(filters:_*)
   super.initializeNewStream(filterQuery,filters)

  }
  def restartStream(twitterStream:TwitterStream, filters: ListBuffer[Long]){//twitterStream:TwitterStream, streamId:Int){
  twitterStream.cleanUp()
   println("RESTART WITH:"+filters)
   val filterQuery:FilterQuery=new FilterQuery().follow(filters:_*)
    twitterStream.filter(filterQuery)
  }
    def terminateStream(twitterStream:TwitterStream){
     twitterStream.shutdown()

  } 

}