package org.prosolo.bigdata.spark.scala.clustering

import java.util
import java.util.List

import com.datastax.driver.core.Row
import org.apache.spark.rdd.RDD
import org.prosolo.bigdata.dal.cassandra.impl.SNAClustersDAO
import org.prosolo.bigdata.scala.clustering.sna.{DirectedNetwork, UserLink, UserNode}
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkJob}
import org.prosolo.bigdata.utils.DateUtil
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
  * Created by zoran on 18/03/17.
  */
/**
  * zoran 18/03/17
  */
class SNAClusteringSparkJob(kName:String) extends SparkJob with Serializable{
  val keyspaceName=kName
//  val sc = SparkContextLoader.getSC
  val edgesToRemove=2

  def runSparkJob(deliveriesIds: java.util.List[java.lang.Long], dbName: String, timestamp: Long): Unit = {
    val deliveriesIdsScala: Seq[java.lang.Long] = deliveriesIds.asScala.toSeq
    //deliveriesIdsScala:+6l
    //val deliveriesIdsScala:Seq[java.lang.Long] =Seq[java.lang.Long](6l,7l)
    println("ALL DELIVERIES:" + deliveriesIdsScala.mkString(","))


    /*val deliveriesRDD: RDD[Long] = sc.parallelize(deliveriesIdsScala.map {
      Long2long
    })*/
   // val timestamp=System.currentTimeMillis()
    //deliveriesRDD.foreachPartition {
      val dbManager=new SNAClustersDAO(dbName)
      //deliveriesIt => {

        deliveriesIdsScala.foreach{
          deliveryId=>
            println("RUNNING SNA CLUSTERING FOR CREDENTIAL:" + deliveryId+ " timestamp:"+timestamp)
              identifyClustersInCredential(timestamp, deliveryId,dbManager )
            //val socialInteractionsData=readCourseData(deliveryId,dbManager)
       // }
     // }
    }
    def identifyClustersInCredential(timestamp:Long, deliveryId:Long,dbManager:SNAClustersDAO): Unit ={
      println("identify clusters in credential:"+deliveryId)
      val socialInteractionsData=readCourseData(deliveryId,dbManager)
      val directedNetwork=new DirectedNetwork()
      socialInteractionsData.foreach {
        row =>
          val sourcenode=directedNetwork.getOrCreateUserNode(row._1)
          val targetnode=directedNetwork.getOrCreateUserNode(row._2)
          val link = new UserLink(row._3, sourcenode, targetnode)
          directedNetwork.addLink(link,sourcenode,targetnode)
      }
       println("Users:"+directedNetwork.getNodes().size+" LINKS:"+directedNetwork.getLinks().size)
       if(directedNetwork.getLinks().size>0){
        val finalUserNodes:ArrayBuffer[UserNode]=if(directedNetwork.getLinks().size<edgesToRemove){
          directedNetwork.calculateEdgeBetweennessClustering(directedNetwork.getLinks().size)
        }else directedNetwork.calculateEdgeBetweennessClustering(edgesToRemove)

        storeUserNodesClustersForCourse(timestamp, deliveryId,finalUserNodes, directedNetwork.getLinks(),dbManager)
      }

    }
    def readCourseData(courseId:Long,dbManager:SNAClustersDAO):Array[Tuple3[Long,Long,Long]] ={
      println("READ COURSE DATA FOR:"+courseId+" keyspace:"+keyspaceName)
      val rows:List[Row] =dbManager.getSocialInteractions(courseId)
      println("GOT COURSE DATA FOR:"+courseId+" rows:"+rows.size())
     val courseData:Array[Tuple3[Long,Long,Long]]=rows.asScala.toArray.map{row:Row=>new Tuple3(row.getLong("source"),row.getLong("target"),row.getLong("count"))}
      courseData

    }
    def storeUserNodesClustersForCourse(timestamp:Long, courseId:Long,userNodes:ArrayBuffer[UserNode],userLinks:Iterable[UserLink],dbManager:SNAClustersDAO): Unit ={

      println("STORE USER NODES FOR COURSE:"+courseId)
      var sourceInteractions:Map[Tuple3[Long,Int,Long],ArrayBuffer[Tuple2[Long, Int]]]=new HashMap[Tuple3[Long,Int,Long],ArrayBuffer[Tuple2[Long, Int]]]()
      var targetInteractions:Map[Tuple4[Long,Int,Long,String],ArrayBuffer[Tuple3[Long, Int, Int]]]=new HashMap[Tuple4[Long,Int,Long,String],ArrayBuffer[Tuple3[Long, Int,Int]]]()
      userLinks.foreach{
        userLink=>
          println("USER LINK:"+userLink.weight+" source:" +userLink.source.id+" cluster:"+userLink.source.cluster+" target:"+userLink.target.id+" target cl:"+userLink.target.cluster)
          if(userLink.source.cluster==userLink.target.cluster) {
            val sourcekey = new Tuple3(courseId, userLink.source.cluster, userLink.source.id)
            val inClusterUserInteractions = sourceInteractions.getOrElse(sourcekey, new ArrayBuffer[Tuple2[Long, Int]]())
            inClusterUserInteractions += new Tuple2(userLink.target.id, userLink.weight.toInt)
            sourceInteractions += sourcekey -> inClusterUserInteractions
            println("SOURCE INTERACTIONS:"+sourceInteractions.toString())
          }else{
            val sourceOutKey=new Tuple4(courseId,userLink.source.cluster,userLink.source.id,"SOURCE")
            val sourceOutClusterUserInteractions=targetInteractions.getOrElse(sourceOutKey,new ArrayBuffer[Tuple3[Long,Int,Int]]())
            sourceOutClusterUserInteractions+=new Tuple3(userLink.target.id,userLink.target.cluster, userLink.weight.toInt)
            targetInteractions+=sourceOutKey->sourceOutClusterUserInteractions

            val targetOutKey=new Tuple4(courseId,userLink.target.cluster,userLink.target.id,"TARGET")
            val targetOutClusterUserInteractions=targetInteractions.getOrElse(targetOutKey,new ArrayBuffer[Tuple3[Long,Int,Int]]())
            targetOutClusterUserInteractions+=new Tuple3(userLink.source.id, userLink.source.cluster, userLink.weight.toInt)
            targetInteractions+=targetOutKey->targetOutClusterUserInteractions
             println("TARGET INTERACTIONS:"+targetInteractions.toString())
          }
      }
      val sourceInteractionsConv=sourceInteractions.map{
        case(key,interactions)=>
          (key, interactions.map { i =>
            val jsonObject=Json.obj("target"->i._1,"count"->i._2)
            Json.stringify(jsonObject)
          }.toList.asJava)
      }.foreach{
        case(key,interactions)=>
          dbManager.insertInsideClusterInteractions(timestamp,key._1,key._2.toLong,key._3,interactions)
          dbManager.insertStudentCluster(timestamp,key._1,key._3,key._2.toLong)
      }
      println("SOURCE INTERACTIONS CONVERTED:"+sourceInteractionsConv.toString)
      val targetInteractionsConv=targetInteractions.map{
        case(key,interactions)=>
          (key, interactions.map { i =>
            val jsonObject=Json.obj("target"->i._1,"cluster"->i._2, "count"->i._3)
            Json.stringify(jsonObject)
          }.toList.asJava)
      }.foreach{
        case(key,interactions)=>
          dbManager.insertOutsideClusterInteractions(timestamp,key._1,key._3,key._2.toLong, key._4, interactions)
      }

      println("TARGET INTERACTIONS CONVERTED:"+targetInteractionsConv.toString)
    }


  }
}
