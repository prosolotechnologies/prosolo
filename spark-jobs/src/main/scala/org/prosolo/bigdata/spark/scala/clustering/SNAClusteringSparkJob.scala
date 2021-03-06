package org.prosolo.bigdata.spark.scala.clustering


import java.util.List

import com.datastax.driver.core.Row
import org.prosolo.bigdata.dal.cassandra.impl.SNAClustersDAO
import org.prosolo.bigdata.scala.clustering.sna.{DirectedNetwork, UserLink, UserNode}
import org.prosolo.bigdata.scala.spark.SparkJob
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
   val keyspaceName: String =kName

  val edgesToRemove=2

  def runSparkJob(deliveriesIds: java.util.List[java.lang.Long], dbName: String, timestamp: Long): Unit = {
    val deliveriesIdsScala: Seq[java.lang.Long] = deliveriesIds.asScala.toSeq
    logger.debug("ALL DELIVERIES:" + deliveriesIdsScala.mkString(","))
      val dbManager=new SNAClustersDAO(dbName)
         deliveriesIdsScala.foreach{
          deliveryId=>
            logger.debug("RUNNING SNA CLUSTERING FOR CREDENTIAL:" + deliveryId+ " timestamp:"+timestamp)
              identifyClustersInCredential(timestamp, deliveryId,dbManager )
    }
    def identifyClustersInCredential(timestamp:Long, deliveryId:Long,dbManager:SNAClustersDAO): Unit ={
      logger.debug("identify clusters in credential:"+deliveryId)
      val socialInteractionsData=readCourseData(deliveryId,dbManager)
      val directedNetwork=new DirectedNetwork()
      socialInteractionsData.foreach {
        row =>
          val sourcenode=directedNetwork.getOrCreateUserNode(row._1)
          val targetnode=directedNetwork.getOrCreateUserNode(row._2)
          val link = new UserLink(row._3, sourcenode, targetnode)
          directedNetwork.addLink(link,sourcenode,targetnode)
      }
       logger.debug("Users:"+directedNetwork.getNodes().size+" LINKS:"+directedNetwork.getLinks().size)
       if(directedNetwork.getLinks().size>0){
        val finalUserNodes:ArrayBuffer[UserNode]=if(directedNetwork.getLinks().size<edgesToRemove){
          directedNetwork.calculateEdgeBetweennessClustering(directedNetwork.getLinks().size)
        }else directedNetwork.calculateEdgeBetweennessClustering(edgesToRemove)

        storeUserNodesClustersForCourse(timestamp, deliveryId,finalUserNodes, directedNetwork.getLinks(),dbManager)
      }

    }
    def readCourseData(courseId:Long,dbManager:SNAClustersDAO):Array[(Long, Long, Long)] ={
      logger.debug("READ COURSE DATA FOR:"+courseId+" keyspace:"+keyspaceName)
      val rows:List[Row] =dbManager.getSocialInteractions(courseId)
      logger.debug("GOT COURSE DATA FOR:"+courseId+" rows:"+rows.size())
     val courseData:Array[(Long, Long, Long)]=rows.asScala.toArray.map{ row:Row=>new Tuple3(row.getLong("source"),row.getLong("target"),row.getLong("count"))}
      courseData

    }
    def storeUserNodesClustersForCourse(timestamp:Long, courseId:Long,userNodes:ArrayBuffer[UserNode],userLinks:Iterable[UserLink],dbManager:SNAClustersDAO): Unit ={

      logger.debug("STORE USER NODES FOR COURSE:"+courseId)
      var sourceInteractions:Map[(Long, Int, Long),ArrayBuffer[(Long, Int)]]=new HashMap[(Long, Int, Long),ArrayBuffer[(Long, Int)]]()
      var targetInteractions:Map[(Long, Int, Long, String),ArrayBuffer[(Long, Int, Int)]]=new HashMap[(Long, Int, Long, String),ArrayBuffer[(Long, Int, Int)]]()
      userLinks.foreach{
        userLink=>
          logger.debug("USER LINK:"+userLink.weight+" source:" +userLink.source.id+" cluster:"+userLink.source.cluster+" target:"+userLink.target.id+" target cl:"+userLink.target.cluster)
          if(userLink.source.cluster==userLink.target.cluster) {
            val sourcekey = Tuple3(courseId, userLink.source.cluster, userLink.source.id)
            val inClusterUserInteractions = sourceInteractions.getOrElse(sourcekey, new ArrayBuffer[Tuple2[Long, Int]]())
            inClusterUserInteractions += Tuple2(userLink.target.id, userLink.weight.toInt)
            sourceInteractions += sourcekey -> inClusterUserInteractions
            logger.debug("SOURCE INTERACTIONS:"+sourceInteractions.toString())
          }else{
            val sourceOutKey=Tuple4(courseId,userLink.source.cluster,userLink.source.id,"SOURCE")
            val sourceOutClusterUserInteractions=targetInteractions.getOrElse(sourceOutKey,new ArrayBuffer[Tuple3[Long,Int,Int]]())
            sourceOutClusterUserInteractions+= Tuple3(userLink.target.id,userLink.target.cluster, userLink.weight.toInt)
            targetInteractions+=sourceOutKey->sourceOutClusterUserInteractions

            val targetOutKey=Tuple4(courseId,userLink.target.cluster,userLink.target.id,"TARGET")
            val targetOutClusterUserInteractions=targetInteractions.getOrElse(targetOutKey,new ArrayBuffer[Tuple3[Long,Int,Int]]())
            targetOutClusterUserInteractions+= Tuple3(userLink.source.id, userLink.source.cluster, userLink.weight.toInt)
            targetInteractions+=targetOutKey->targetOutClusterUserInteractions
             logger.debug("TARGET INTERACTIONS:"+targetInteractions.toString())
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
      logger.debug("SOURCE INTERACTIONS CONVERTED:"+sourceInteractionsConv.toString)
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

      logger.debug("TARGET INTERACTIONS CONVERTED:"+targetInteractionsConv.toString)
    }


  }
}
