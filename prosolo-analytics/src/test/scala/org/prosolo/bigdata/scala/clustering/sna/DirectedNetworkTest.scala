package org.prosolo.bigdata.scala.clustering.sna

import java.io._
import java.net.URL
import java.util

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer
import edu.uci.ics.jung.graph.util.EdgeType
import edu.uci.ics.jung.graph.{SparseMultigraph, DirectedSparseGraph}
import org.junit.Test
import org.scalatest.FunSuite

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by zoran on 21/12/15.
  */
/**
  * zoran 21/12/15
  */
class DirectedNetworkTest {

  @Test def testCalculateEdgeBetweennessClustering(){
    println("testCalculateEdgeBetweennessClustering")
      println("TEST CALCULATE")
    val edgesToRemove=5
    val addednodes=new HashMap[Int,UserNode]()


    val clusterer:EdgeBetweennessClusterer[UserNode,UserLink]=new EdgeBetweennessClusterer[UserNode,UserLink](edgesToRemove)
   // val clusterer2:EdgeBetweennessClusterer[Int,Int]=new EdgeBetweennessClusterer[Int,Int](edgesToRemove)
    val directednetwork:DirectedNetwork=new DirectedNetwork()
   val network:DirectedSparseGraph[UserNode,UserLink]=new DirectedSparseGraph[UserNode,UserLink]()
   // val network:SparseMultigraph[Int,Int]=new SparseMultigraph[Int,Int]()
    val testData=readTestData()
    var linkid=0
    testData.foreach {
      row =>
      //  val sourcenode = new UserNode(row._1)
        val sourcenode=addednodes.getOrElseUpdate(row._1,{
          val node=new UserNode(row._1)
          network.addVertex(node)
          node
          })
        /*if (!addednodes.containsKey(row._1)) {
          sourcenode=new UserNode(row._1);
          addednodes.put(row._1,sourcenode )
          network.addVertex()
        //  directednetwork.addNode(sourcenode)
        }*/
       // val targetnode = new UserNode(row._2)
        val targetnode=addednodes.getOrElseUpdate(row._2,{
          val node=new UserNode(row._2)
          network.addVertex(node)
          node
        })
        /*if (addednodes.contains(row._2)) {
          addednodes += row._2
          network.addVertex(new UserNode(row._2))
         // directednetwork.addNode(targetnode)
        }*/
        linkid=linkid+1

        val link = new UserLink(row._3)
        //for(i<-0 to row._3){

          network.addEdge(link,sourcenode, targetnode, EdgeType.DIRECTED)
        //}

       // directednetwork.addLink(link, sourcenode, targetnode)
    }
    println("vertex count:"+network.getVertexCount)
    println("edge count:"+network.getEdgeCount())
    val clusteredUsers:java.util.Set[java.util.Set[UserNode]] = clusterer.transform(network);
    val clustUsers=clusteredUsers.asScala
    println("HAS CLUSTERS:"+clusteredUsers.size()+" ..."+clustUsers.size)
     clustUsers.foreach{
      userNodes=>{
        println("CLUSTER**********")
        val uNodes=userNodes.asScala
        uNodes.foreach{
          userNode=>
         println("USER NODE:"+userNode.id+" cluster:"+userNode.cluster)
        }
      }

    }

  }
  def readTestData():ArrayBuffer[Tuple3[Int,Int,Int]] ={
    val testData:ArrayBuffer[Tuple3[Int,Int,Int]]=new ArrayBuffer[Tuple3[Int,Int,Int]]()
    val filePath: URL = Thread.currentThread.getContextClassLoader.getResource("files/users_interactions_test_data2.csv")
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
 @Test def anotherTest(): Unit ={
   val graph:SparseMultigraph[Int,Int]=new SparseMultigraph[Int,Int]()
   for(i<-0 to 10){
     graph.addVertex(i+1)
   }
   var j:Int=0
   j+=1
   graph.addEdge(j,1,2);
   j+=1
   graph.addEdge(j,1,3);
   j+=1
   graph.addEdge(j,2,3);
   j+=1
   graph.addEdge(j,5,6);
   j+=1
   graph.addEdge(j,5,7);
   j+=1
   graph.addEdge(j,6,7);
   j+=1
   graph.addEdge(j,8,10);
   j+=1
   graph.addEdge(j,7,8);
   j+=1
   graph.addEdge(j,7,10);
   j+=1
   graph.addEdge(j,3,4);
   j+=1
   graph.addEdge(j,4,6);
   j+=1
   graph.addEdge(j,4,8);

   println("vertex count:"+graph.getVertexCount)
   println("edge count:"+graph.getEdgeCount())
   val clusterer:EdgeBetweennessClusterer[Int,Int]=new EdgeBetweennessClusterer[Int,Int](3)
   val clusteredUsers:java.util.Set[java.util.Set[Int]] = clusterer.transform(graph);
   println("NUMBER OF CLUSTERS:"+clusteredUsers.size())

  }


}
