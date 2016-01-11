package org.prosolo.bigdata.scala.clustering.sna

import java.io._
import java.net.URL

import com.datastax.driver.core.Row
import edu.uci.ics.jung.graph.util.EdgeType
import org.prosolo.bigdata.dal.cassandra.impl.SocialInteractionStatisticsDBManagerImpl
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

/**
  * Created by zoran on 21/12/15.
  */
/**
  * zoran 21/12/15
  */
object SNAclusterManager{
val edgesToRemove=5
  //val moocCourses:Array[Long]=Array(1,32768,32769,32770,65536,98304,98305,98306,131072,131073,131074)
  //val moocCourses:Array[Long]=Array(1)
  val dbManager=new SocialInteractionStatisticsDBManagerImpl
  println("INITIALIZED SNA CLUSTER MANAGER")
  val clusteringDAOManager=new ClusteringDAOImpl



def identifyClusters(): Unit ={
  val allCourses=clusteringDAOManager.getAllCoursesIds
  allCourses.asScala.foreach(courseid=> {
    println("RUNNING CLUSTERING FOR COURSE:" + courseid)
    identifyClustersInCourse(courseid)
  })
}
  def identifyClustersInCourse(courseId:Long): Unit ={
    val socialInteractionsData=readCourseData(courseId)
    val directedNetwork=new DirectedNetwork()
    socialInteractionsData.foreach {
      row =>
          val sourcenode=directedNetwork.getOrCreateUserNode(row._1)
          val targetnode=directedNetwork.getOrCreateUserNode(row._2)
          val link = new UserLink(row._3)
          directedNetwork.addLink(link,sourcenode,targetnode)
    }
    println("Users:"+directedNetwork.getNodes().size+" LINKS:"+directedNetwork.getLinks().size)
    if(directedNetwork.getLinks().size>0){
      val finalUserNodes:ArrayBuffer[UserNode]=directedNetwork.calculateEdgeBetweennessClustering(edgesToRemove)
    }

  }
  def readCourseData(courseId:Long):Array[Tuple3[Long,Long,Long]] ={
    val rows: java.util.List[Row] =dbManager.getSocialInteractions(courseId)
    val courseData:Array[Tuple3[Long,Long,Long]]=rows.asScala.toArray.map{row:Row=>new Tuple3(row.getLong("source"),row.getLong("target"),row.getLong("count"))}
    courseData
  }
  def storeUserNodesClustersForCourse(courseId:Long,userNodes:ArrayBuffer[UserNode]): Unit ={

  }
  //identifyClusters()



}
