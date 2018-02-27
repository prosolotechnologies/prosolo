package org.prosolo.bigdata.scala.spark.emails

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.scala.spark.{ SparkJob}
import org.apache.spark.sql._
import org.prosolo.bigdata.dal.cassandra.impl.TablesNames
import scala.collection.immutable.HashMap

//case class Notification(id:Long, receiver:Long, actor:Long, actType:String)

object UserNotificationEmailsSparkJob{
  val BATCH_SIZE=50
  val NOTIFICATION_TYPE_SIZE=3
}
class UserNotificationEmailsSparkJob(kName:String)extends SparkJob with Serializable{
  override def keyspaceName: String = kName;
   import sparkSession.sqlContext.implicits._


  def runSparkJob(date:Long):Array[Array[NotificationReceiverSummary]]={
    import sparkSession.implicits._
   //val connector=CassandraConnector.apply(sparkSession.sparkContext.getConf)

    //Retrieving all notifications from cassandra for specific date
    val notificationsDF:DataFrame=sparkSession.read.format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace"->keyspaceName,"table"->TablesNames.NOTIFICATION_DATA)).load()

    notificationsDF.show
    notificationsDF.createOrReplaceTempView("notificationsView")
    val date2=17588
    val dayNotificationsDF:DataFrame=sparkSession.sql("select * from notificationsView where date="+date2)

    println("DAY:"+date+" TEMPORARY DATE:"+date2)

    dayNotificationsDF.show()
    //Create notification instances and group it by receiver id
    val domain=System.getProperty("app.domain");
    val receiversDF=dayNotificationsDF.map{
      case Row(date:Long,notificationType:String, id:Long, actorfullname:String, actorId:Long, email:String, link:String, objectTitle:String,
      objectType:String, receiverFullName:String, receiverId:Long)=>{
        val url= if(domain.endsWith("/") && link.startsWith("/"))  domain+link.substring(1) else domain+link
        (receiverId,Notification(date,notificationType, id,actorfullname, actorId, email, url, objectTitle,
          objectType, receiverFullName, receiverId))
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

         /*var notificationByType=notificationsByType.getOrElse(n.notificationType,new Array[Notification](UserNotificationEmailsSparkJob.NOTIFICATION_TYPE_SIZE))
          if(notificationByType.length<UserNotificationEmailsSparkJob.NOTIFICATION_TYPE_SIZE){
            notificationByType:+=n
          }*/
          //notificationsByType+=(n.notificationType->(notificationByType))



        }
        println("RECEIVER:"+receiver)
        println(notificationsByType.mkString(","))


          (receiver,NotificationsSummary(receiver,total,notCounter,notificationsByType))
      }}
    println("RESULTS:"+res.count)

    res.collect().foreach(n=>println(n))
    //joining NotificationSummary with Receiver
    val notificationsReceivers=res.join(receiversNames.rdd)
    println("NOTIFICATIONS RECEIVERS:"+notificationsReceivers.count())
    notificationsReceivers.collect().foreach(n=>println(n))
    val notificationsReceiversSummary=notificationsReceivers.map{
      case (receiverid:Long, (notificationSummary:NotificationsSummary, receiver:Receiver))=>
        NotificationReceiverSummary(receiver,notificationSummary)
    }




   /*  val today=new Date()
    val from=getPreviousDateString(today,-20)
    val to=getPreviousDateString(today,1)
    println("FROM:"+from+" TO:"+to)
   val sql= "SELECT receiver,id, actor, type as act_type from "+MySQLTablesNames.NOTIFICATIONS+ " where created>=date('"+from+"') and created<=date('"+to+"') and notify_by_email='T'";
    val notificationsDF= MySQLDBManager.createDataFrame(MySQLTablesNames.NOTIFICATIONS,sql)
    println("NOTIFICATIONS:")
    notificationsDF.show()
     val receiversDF= notificationsDF.map{
        case Row(receiver:Long,id:Long, actor:Long, actType:String)=>{
        (receiver,Notification(id,receiver,actor,actType ))
      }}.rdd.groupByKey
 val res:RDD[NotificationsSummary]=receiversDF.map{
  case (receiver:Long,notifications:Iterable[Notification])=>{
    val total=notifications.size
    var notCounter=new  HashMap[String,Int]()
    notifications.foreach(n=>{
        val tempNot= notCounter.getOrElse(n.actType,0)
      notCounter+=(n.actType->(tempNot+1))
    })
    (receiver,total,notCounter)
    NotificationsSummary(receiver,total,notCounter)
}}
    println("RESULTS:"+res.count)
    res.collect().foreach(n=>println(n))
    */
   // res.collect().foreach(n=>println(n))
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

}
