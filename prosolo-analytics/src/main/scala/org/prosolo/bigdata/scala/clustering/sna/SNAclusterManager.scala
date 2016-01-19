package org.prosolo.bigdata.scala.clustering.sna


import java.util

import com.datastax.driver.core.Row
import org.json4s.native.Serialization


import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl.TableNames
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import play.api.libs.json.Json


import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._


/**
  * Created by zoran on 21/12/15.
  */
/**
  * zoran 21/12/15
  */
object SNAclusterManager extends App{

val edgesToRemove=5
  //val moocCourses:Array[Long]=Array(1,32768,32769,32770,65536,98304,98305,98306,131072,131073,131074)
   val allCourses:Array[Long]=Array(1)
  val dbManager=SocialInteractionStatisticsDBManagerImpl.getInstance()

  println("INITIALIZED SNA CLUSTER MANAGER")
  val clusteringDAOManager=new ClusteringDAOImpl


  def identifyFakeClusters(): Unit ={
    val timestamp=System.currentTimeMillis()
   // val allCourses=clusteringDAOManager.getAllCoursesIds
    allCourses.foreach(courseid=> {
      println("RUNNING CLUSTERING FOR COURSE:" + courseid)
      identifyClustersInCourse(timestamp,courseid)
      updateTimestamp(timestamp)
    })
  }
  identifyFakeClusters()
def identifyClusters(): Unit ={
  val timestamp=System.currentTimeMillis()
  val allCourses=clusteringDAOManager.getAllCoursesIds
  allCourses.asScala.foreach(courseid=> {
    println("RUNNING CLUSTERING FOR COURSE:" + courseid)
    identifyClustersInCourse(timestamp, courseid)
    updateTimestamp(timestamp)
  })
}
  def updateTimestamp(timestamp:Long)={
    dbManager.updateCurrentTimestamp(TableNames.INSIDE_CLUSTER_INTERACTIONS,timestamp)
  }
  def identifyClustersInCourse(timestamp:Long, courseId:Long): Unit ={
    val socialInteractionsData=readCourseData(courseId)
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
      val finalUserNodes:ArrayBuffer[UserNode]=directedNetwork.calculateEdgeBetweennessClustering(edgesToRemove)
      storeUserNodesClustersForCourse(timestamp, courseId,finalUserNodes, directedNetwork.getLinks())
    }

  }
  def readCourseData(courseId:Long):Array[Tuple3[Long,Long,Long]] ={
    val rows: java.util.List[Row] =dbManager.getSocialInteractions(courseId)
    val courseData:Array[Tuple3[Long,Long,Long]]=rows.asScala.toArray.map{row:Row=>new Tuple3(row.getLong("source"),row.getLong("target"),row.getLong("count"))}
    courseData
  }
  def storeUserNodesClustersForCourse(timestamp:Long, courseId:Long,userNodes:ArrayBuffer[UserNode],userLinks:Iterable[UserLink]): Unit ={

    println("STORE USER NODES FOR COURSE:"+courseId)
    //sourceInteractions:key-courseid, cluster, sourceid //value-targetid, weight
    var sourceInteractions:Map[Tuple3[Long,Int,Long],ArrayBuffer[Tuple2[Long, Int]]]=new HashMap[Tuple3[Long,Int,Long],ArrayBuffer[Tuple2[Long, Int]]]()
   //targetInteractions:key-courseid, cluster, sourceid(targetid), direction(Source or target) // value-targetid(sourceid), cluster, weight
    var targetInteractions:Map[Tuple4[Long,Int,Long,String],ArrayBuffer[Tuple3[Long, Int, Int]]]=new HashMap[Tuple4[Long,Int,Long,String],ArrayBuffer[Tuple3[Long, Int,Int]]]()
    userLinks.foreach{
      userLink=>
        println("USER LINK:"+userLink.weight+" source:" +userLink.source.id+"cluster:"+userLink.source.cluster+" target:"+userLink.target.id+" target cl:"+userLink.target.cluster)
       val sourcekey=new Tuple3(courseId,userLink.source.cluster,userLink.source.id)
       // val targetkey=new Tuple3(courseId,userLink.target.cluster,userLink.target.id)
       // val x=new ArrayBuffer[Tuple3[Long,Long,Int]]()
       // val x=new ArrayBuffer[Tuple3[Long,Long,Int]]]()
          val inClusterUserInteractions=sourceInteractions.getOrElse(sourcekey,new ArrayBuffer[Tuple2[Long, Int]]())
          inClusterUserInteractions+=new Tuple2(userLink.target.id,userLink.weight.toInt)
          sourceInteractions+=sourcekey->inClusterUserInteractions
        println("SOURCE INTERACTIONS:"+sourceInteractions.toString())

        if(userLink.source.cluster!=userLink.target.cluster){
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
          val jsonObject=Json.obj("target"->1,"count"->2)
           Json.stringify(jsonObject)
     }.asJavaCollection)
    }
     println("SOURCE INTERACTIONS CONVERTED:"+sourceInteractionsConv.toString)

  }
  //identifyClusters()

  //implicit val formats = DefaultFormats

}