package org.prosolo.bigdata.scala.twitter
import com.google.gson.JsonObject
import org.prosolo.bigdata.events.pojo.AnalyticsEvent
import scala.collection.mutable.ListBuffer
import twitter4j.{HashtagEntity, Status,TwitterStream,TwitterStreamFactory,FilterQuery}
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
/**
 * @author zoran Aug 6, 2015
 */
object TwitterUsersStreamsManager {
   /** Keeps information about each twitter user and which stream his account is followed in   */
  val usersAndStreamsIds:collection.mutable.Map[BigInt,Int]=new collection.mutable.HashMap[BigInt, Int]
   val twitterStreamsAndUsers:collection.mutable.Map[Int,(TwitterStream,ListBuffer[BigInt])]=new collection.mutable.HashMap[Int,(TwitterStream,ListBuffer[BigInt])]
  def initialize(){
    val twitterDAO = new TwitterStreamingDAOImpl()
   val twitterIds:java.util.List[java.lang.Long]=twitterDAO.getAllTwitterUsersTokensUserIds
  }
  def updateTwitterUserFromAnalyticsEvent(event: AnalyticsEvent){
    val data:JsonObject=event.getData()
    val userid:Int=data.get("twitterId").getAsInt
    val shouldAdd:Boolean=data.get("add").getAsBoolean
    if(shouldAdd)addNewTwitterUser(userid) else removeTwitterUser(userid)
  }
  private def addNewTwitterUser(userid:Int){
    println("Adding new twitter user to follow:"+userid)
  }
  private def removeTwitterUser(userid:Int){
     println("Removing twitter user to follow:"+userid)
  }

}