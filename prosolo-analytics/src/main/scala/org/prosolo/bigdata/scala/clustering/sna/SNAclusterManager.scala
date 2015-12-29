package org.prosolo.bigdata.scala.clustering.sna

import java.io._
import java.net.URL

import edu.uci.ics.jung.graph.util.EdgeType

import scala.collection.mutable.ArrayBuffer

/**
  * Created by zoran on 21/12/15.
  */
/**
  * zoran 21/12/15
  */
object SNAclusterManager extends App{
val edgesToRemove=5
  val moocCourses:Array[Long]=Array(1,32768,32769,32770,65536,98304,98305,98306,131072,131073,131074)

def identifyClusters(): Unit ={
  moocCourses.foreach(courseid=> {
    println("RUNNING CLUSTERING FOR COURSE:" + courseid)
    identifyClustersInCourse(courseid)
  })
}
  def identifyClustersInCourse(courseId:Long): Unit ={
    val socialInteractionsData=readTestCourseData()
    val directedNetwork=new DirectedNetwork()
    socialInteractionsData.foreach {
      row =>
          val sourcenode=directedNetwork.getOrCreateUserNode(row._1)
          val targetnode=directedNetwork.getOrCreateUserNode(row._2)
          val link = new UserLink(row._3)
          directedNetwork.addLink(link,sourcenode,targetnode)
    }
    directedNetwork.calculateEdgeBetweennessClustering(edgesToRemove)
  }
  def readTestCourseData():ArrayBuffer[Tuple3[Int,Int,Int]] ={
    val testData:ArrayBuffer[Tuple3[Int,Int,Int]]=new ArrayBuffer[Tuple3[Int,Int,Int]]()
    val filePath: URL = Thread.currentThread.getContextClassLoader.getResource("files/users_interactions_test_data.csv")
    val testFile: File = new File(filePath.getPath)
    try {
      val br: BufferedReader = new BufferedReader(new FileReader(testFile))
      var line: String = null
      try {
        var first: Boolean = true
        while ((({
          line = br.readLine; line
        })) !=null ) {
          if (!first) {
            val parts: Array[String] = line.split("\\s*,\\s*")
            // System.out.println("SOURCE:" + parts(0) + " TARGET:" + parts(1) + " COUNT:" + parts(2))
            val row=new Tuple3(parts(0),parts(1),parts(2))
            testData += Tuple3(parts(0).toInt,parts(1).toInt,parts(2).toInt)
          }
          first = false
        }
      }
      catch {
        case e1: IOException => {
          e1.printStackTrace
        }
      }
    }
    catch {
      case e2: FileNotFoundException => {
        e2.printStackTrace
      }

    }
    testData
  }
  identifyClusters()

}
