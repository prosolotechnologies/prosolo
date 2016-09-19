package org.prosolo.bigdata.scala.twitter
import com.google.gson.JsonObject
import org.prosolo.bigdata.events.pojo.AnalyticsEvent
import scala.collection.mutable.ListBuffer
import twitter4j.{HashtagEntity, Status,TwitterStream,TwitterStreamFactory,FilterQuery}
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import scala.collection.mutable.Buffer
import org.prosolo.bigdata.dal.persistence.HibernateUtil
import org.hibernate.Session
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
      val session:Session= HibernateUtil.getSessionFactory().openSession()
     val twitterIds:java.util.List[java.lang.Long]=twitterDAO.getAllTwitterUsersTokensUserIds(session)
     session.close();
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
    println("")
    val userid:Long=data.get("twitterId").getAsLong
    val shouldAdd:Boolean=data.get("add").getAsBoolean
    if(shouldAdd)addNewTwitterUser(userid) else removeTwitterUser(userid)
  }
  private def addNewTwitterUser(userid:Long){
    println("Adding new twitter user:"+userid)
    if(!usersAndStreamsIds.contains(userid)){
        val currentFilterList:ListBuffer[Long]= getLatestStreamAndList._2
        currentFilterList+=userid
      println("new users list:"+currentFilterList.mkString(","))
       if(currentFilterList.size>STREAMLIMIT){
        restartStream(getLatestStreamAndList._1,getLatestStreamAndList._2)
         initializeNewCurrentListAndStream(currentFilterList)
      }else{
        restartStream(getLatestStreamAndList._1,getLatestStreamAndList._2)
      }
    }
  }
  private def removeTwitterUser(userid:Long){
    println("Removing twitter user:"+userid)
      if(usersAndStreamsIds.contains(userid)){
          val streamId=usersAndStreamsIds.get(userid);
          val streamUsersTuple:(TwitterStream,ListBuffer[Long])= twitterStreamsAndUsers.get(streamId.get).get
          val newusers:ListBuffer[Long]= streamUsersTuple._2.filterNot(p => p==userid)
          twitterStreamsAndUsers.put(streamId.get,(streamUsersTuple._1,newusers))
          usersAndStreamsIds.remove(userid)
        println("new users list:"+newusers.mkString(","))
          restartStream(streamUsersTuple._1, newusers)
      }
  }
  
    /**
   * Initialize new stream for an array of users ids
   */
  def initializeNewStream(filters: Buffer[Long]):Tuple2[TwitterStream,Int]={
  // val filterQuery:FilterQuery=new FilterQuery().follow(filters:_*)
   super.initializeNewStream(new FilterQuery().follow(filters:_*))

  }
  def restartStream(twitterStream:TwitterStream, filters: ListBuffer[Long]){//twitterStream:TwitterStream, streamId:Int){
   super.restartStream(twitterStream, new FilterQuery().follow(filters:_*))
  }
    def terminateStream(twitterStream:TwitterStream){
     twitterStream.shutdown()

  } 

}