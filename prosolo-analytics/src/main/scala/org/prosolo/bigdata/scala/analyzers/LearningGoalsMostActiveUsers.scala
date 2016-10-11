package org.prosolo.bigdata.scala.analyzers

import java.util
import java.util.{Collections, Comparator}

import com.google.gson.{JsonObject, JsonPrimitive}
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.common.dal.pojo.{MostActiveUsersForLearningGoal, UserLearningGoalActivitiesCount}
import org.prosolo.bigdata.common.events.pojo.{DataName, DataType}
import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl
import org.prosolo.bigdata.es.impl.RecommendationDataIndexerImpl
import org.prosolo.bigdata.events.pojo.AnalyticsEvent
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.common.util.date.DateEpochUtil

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by zoran on 09/01/16.
  */
/**
  * zoran 09/01/16
  */
class LearningGoalsMostActiveUsers {

  def analyzeLearningGoalsMostActiveUsersForDay(daysSinceEpoch:Long)={
    //val dbManager = new AnalyticalEventDBManagerImpl;
    println("analyzeLearningGoalsMostActiveUsersForDay")
    val sc=SparkContextLoader.getSC
    val activitiesCounters = AnalyticalEventDBManagerImpl.getInstance().findUserLearningGoalActivitiesByDate(daysSinceEpoch);
   val activitiesCountersRDD:RDD[UserLearningGoalActivitiesCount]= sc.parallelize(activitiesCounters.asScala)
    val actCountersByLearningGoals:RDD[(Long,Iterable[UserLearningGoalActivitiesCount])]=activitiesCountersRDD.groupBy{
      counter:UserLearningGoalActivitiesCount=>
        counter.getLearningGoalId
    }

    val actCountersByLearningGoalsSorted:RDD[(Long,ArrayBuffer[UserLearningGoalActivitiesCount])] =actCountersByLearningGoals.mapValues{
       counters: Iterable[UserLearningGoalActivitiesCount] =>
         val sortedList:List[UserLearningGoalActivitiesCount]=counters.toList.sortWith(_.getCounter<_.getCounter)
         println("SORTED LIST")
         counters.foreach(count=>println(count.getCounter))
       /* val list:util.List[UserLearningGoalActivitiesCount]= new util.ArrayList[UserLearningGoalActivitiesCount]
          counters.foreach(counter=>list.add(counter))
         Collections.sort(list)*/
          val lastindex=if(sortedList.size<10) sortedList.size else 10
         //val shortList= new util.ArrayList[UserLearningGoalActivitiesCount]
         val shortList=new ArrayBuffer[UserLearningGoalActivitiesCount]
         sortedList.slice(0, lastindex).foreach{el=>shortList+=el}
         shortList
    }

    actCountersByLearningGoalsSorted.foreach{
      case (learningGoalId:Long,counters:ArrayBuffer[UserLearningGoalActivitiesCount])
      =>
        val data=new JsonObject
        data.add("date", new JsonPrimitive(daysSinceEpoch))
        data.add("learninggoalid", new JsonPrimitive(learningGoalId))
        val mostactiveusers = new JsonObject
        counters.foreach(counter=>mostactiveusers.add(String.valueOf(counter.getUserid),
          new JsonPrimitive(counter.getCounter)))
        data.add("mostactiveusers", new JsonPrimitive(mostactiveusers.toString))
        val event = new AnalyticsEvent;
        event.setDataName(DataName.MOSTACTIVEUSERSFORLEARNINGGOALBYDATE)
        event.setDataType(DataType.RECORD)
        event.setData(data)
        AnalyticalEventDBManagerImpl.getInstance().insertAnalyticsEventRecord(event)

    };

  }
  def analyzeLearningGoalsMostActiveUsersForWeek(): Unit ={
    println("NOT IMPLEMENTED YET")

    val indexer = new RecommendationDataIndexerImpl
    val daysSinceEpoch=DateEpochUtil.getDaysSinceEpoch()
    val daysToAnalyze=daysSinceEpoch-7 to daysSinceEpoch
    val sc=SparkContextLoader.getSC
    val daysToAnalyzeRDD=sc.parallelize(daysToAnalyze)
    val mostActiveUsersByDateRDD:RDD[(Long,mutable.Buffer[MostActiveUsersForLearningGoal])]=daysToAnalyzeRDD.map{
      date=>
      val mostActiveUsersList:mutable.Buffer[MostActiveUsersForLearningGoal]=AnalyticalEventDBManagerImpl.getInstance().findMostActiveUsersForGoalsByDate(date).asScala
        (date,mostActiveUsersList)
    }
    val mostActiveUsersByDateRDDGrouped=mostActiveUsersByDateRDD.groupByKey()
    val mostActiveUsersMergedRDD:RDD[ArrayBuffer[MostActiveUsersForLearningGoal]]=mostActiveUsersByDateRDDGrouped.map{
    case(date:Long, list:Iterable[mutable.Buffer[MostActiveUsersForLearningGoal]])=>
      val mergedList=new ArrayBuffer[MostActiveUsersForLearningGoal]
        list.foreach{
          mostActiveUsersBuffer:mutable.Buffer[MostActiveUsersForLearningGoal]=>
           mostActiveUsersBuffer.foreach{
             mostActiveUsers=>
               mergedList+=mostActiveUsers
           }
        }
        mergedList
    }
    val mostActiveUsersAllMerged:ArrayBuffer[MostActiveUsersForLearningGoal]=mostActiveUsersMergedRDD.reduce{
      case(buffer1,buffer2)=>
        buffer1.++=(buffer2)
    }
    val mostActiveUsersAllMergedRDD:RDD[MostActiveUsersForLearningGoal]= sc.parallelize(mostActiveUsersAllMerged)
   val mostActiveUsersByLearningGoalsPairRDD= mostActiveUsersAllMergedRDD.map{
      mostActiveUsers=>
        (mostActiveUsers.getLearninggoal,mostActiveUsers)
    }.groupByKey()
    val weeklyValuesByGoals=mostActiveUsersByLearningGoalsPairRDD.mapValues{
     val weeklyCounter = new MostActiveUsersForLearningGoal
     val weeklyUserPoints = new util.HashMap[Long, Long]
          var learningGoal:Long=0
      iter:Iterable[MostActiveUsersForLearningGoal]=>
        iter.foreach{
          mostactive=>
           learningGoal=mostactive.getLearninggoal
            val usersForDay=mostactive.getUsers
            val users=usersForDay.keySet
            var prevCounter:Long= 0
            var prevPoints=users.size
            var realPoints=users.size
            users.asScala.foreach{
              user=>
                val counter=usersForDay.get(user)
               val currPoints=if(counter==prevCounter)prevPoints else realPoints
                realPoints=realPoints-1
                prevPoints=currPoints
                prevCounter=counter
                val userPointsCounter:Long=if(weeklyUserPoints.containsKey(user)) usersForDay.get(user) else currPoints
                weeklyUserPoints.put(user, userPointsCounter)

            }
        }
        println("USERS FOR WEEK:" + weeklyUserPoints.toString);
        val topTen:util.Set[util.Map.Entry[Long,Long]]=weeklyUserPoints.entrySet()
        val topTenSorted=topTen.asScala.toList.sortWith{_.getValue<_.getValue}
        val ten=if(topTenSorted.size<10) topTenSorted else topTenSorted.slice(0,10)
        ten.foreach{
          topUser=>
          weeklyCounter.addUser(topUser.getKey, topUser.getValue)
        }
        weeklyCounter.setLearninggoal(learningGoal)
        weeklyCounter.setDate(System.currentTimeMillis())
        println("WEEKLY COUNTER:"+weeklyCounter.toString)
        indexer.updateMostActiveUsersForLearningGoal(weeklyCounter)

    }

  }
}
