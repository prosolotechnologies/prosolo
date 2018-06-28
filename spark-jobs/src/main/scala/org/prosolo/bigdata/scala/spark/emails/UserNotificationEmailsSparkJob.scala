package org.prosolo.bigdata.scala.spark.emails

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.scala.spark.SparkJob
import org.apache.spark.sql._
import org.prosolo.bigdata.dal.cassandra.impl.TablesNames
import com.google.gson.Gson


import scala.collection.immutable.HashMap
import scala.collection.mutable

//case class Notification(id:Long, receiver:Long, actor:Long, actType:String)

object UserNotificationEmailsSparkJob{
  val BATCH_SIZE=50
  val NOTIFICATION_TYPE_SIZE=3
}
class UserNotificationEmailsSparkJob(kName:String)extends SparkJob with Serializable{
  override def keyspaceName: String = kName;
   import sparkSession.sqlContext.implicits._


  def runSparkJob(date:Long, role: String):Array[Array[NotificationReceiverSummary]]={
    import sparkSession.implicits._
   //val connector=CassandraConnector.apply(sparkSession.sparkContext.getConf)

    //Retrieving all notifications from cassandra for specific date
    val notificationsDF:DataFrame=sparkSession.read.format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace"->keyspaceName,"table"->TablesNames.NOTIFICATION_DATA)).load()

    notificationsDF.show
    notificationsDF.createOrReplaceTempView("notificationsView")

    val dayNotificationsDF:DataFrame=sparkSession.sql("select * from notificationsView where date="+date+" and section='" + role+"'")



    dayNotificationsDF.show()
    //Create notification instances and group it by receiver id
    val domain=System.getProperty("app.domain");
    val receiversDF=dayNotificationsDF.map{
      case Row(date:Long,notificationType:String, id:Long, actorfullname:String, actorid:Long, email:String, link:String,
      objectid:Long,objectTitle:String, objectType:String, predicate:String, receiverFullName:String, receiverId:Long, relationtotarget:String, section:String, targetid:Long, targettitle:String
      )=>{
        val url= if(domain.endsWith("/") && link.startsWith("/"))  domain+link.substring(1) else domain+link
        (receiverId,Notification(date,notificationType, id,actorfullname, actorid, email, url, objectTitle,
          objectType, receiverFullName, receiverId,objectid,targetid,targettitle,section,relationtotarget,predicate))
      }
    }.rdd.groupByKey
    //create RDD of receivers containing id, fullname and email
    val receiversNames=dayNotificationsDF.select("receiverid","receiverfullname","email").distinct().map{
      case Row(receiver:Long, fullname:String, email:String )=>
        (receiver,Receiver(receiver,fullname,email))
    }
    receiversNames.show()
    //create NotificationSummary and group by receiver id
    val res:RDD[(Long,NotificationsSummary)]=receiversDF.map{
      case (receiver:Long,notifications:Iterable[Notification])=>{
        val total=notifications.size
        var notCounter=new  HashMap[String,Int]()
        var notificationsByType=new HashMap[String,Array[Notification]]
       val notificationsIt=notifications.iterator
        while(notificationsIt.hasNext){
          val n=notificationsIt.next()
          val tempNot= notCounter.getOrElse(n.notificationType,0)
          notCounter+=(n.notificationType->(tempNot+1))
          //we are retrieving only 3 notifications to be displayed in email
         // val x= (Array(Notification(1,"",12,21,"a","","","","",21)))
           //var newArray=new Array[String](NOTIFICATION_TYPE_SIZE);
           val notificationByType=notificationsByType.getOrElse(n.notificationType, Array())


             if(notificationByType.length<UserNotificationEmailsSparkJob.NOTIFICATION_TYPE_SIZE){
               notificationsByType-=n.notificationType
              val modifiedNotificationByType=notificationByType++Array(n)
                 notificationsByType+=(n.notificationType->(modifiedNotificationByType))
             }
        }

          (receiver,NotificationsSummary(receiver,total,notCounter,notificationsByType))
      }}

    res.collect().foreach(n=>println(n))
    //joining NotificationSummary with Receiver
    val notificationsReceivers=res.join(receiversNames.rdd)
    println("NOTIFICATIONS RECEIVERS:"+notificationsReceivers.count())
    notificationsReceivers.collect().foreach(n=>println(n))
    val notificationsReceiversSummary=notificationsReceivers.map{
      case (receiverid:Long, (notificationSummary:NotificationsSummary, receiver:Receiver))=>
        NotificationReceiverSummary(receiver,notificationSummary)
    }

    val emailBatches:Array[Array[NotificationReceiverSummary]]=notificationsReceiversSummary.collect().grouped(UserNotificationEmailsSparkJob.BATCH_SIZE).toArray

    //notificationsDF.groupBy("receiver").agg()
    println("FINISHED RUN SPARK JOB")
    emailBatches
  }
  def getPreviousDateString(date:Date,before:Int): String ={
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val cal=Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DATE,before)
    format.format(cal.getTime)

  }
  def addSuccessEmails(success:mutable.Map[String,EmailSuccess]): Unit ={
    println("ADD SUCCESS EMAILS:"+success.size)
    success.foreach{
      case(email, emailSuccess)=>
      {
        val gson=new Gson
        submitTaskPoint(gson.toJson(emailSuccess),0,emailSuccess.template)
      }
    }
  }
  def addFailedEmails(failure:mutable.Map[String,EmailSuccess]): Unit ={
     failure.foreach{
       case(email, emailSuccess)=>
         {
          val gson=new Gson
           submitFailedTask(gson.toJson(emailSuccess),0,emailSuccess.template)
         }
     }

  }

}
