package org.prosolo.bigdata.scala.clustering.sna

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer
import edu.uci.ics.jung.graph.DirectedSparseGraph
import edu.uci.ics.jung.graph.util.EdgeType

import scala.collection.JavaConverters._
import scala.collection.mutable.{ArrayBuffer, HashMap}

/**
  * Created by zoran on 20/12/15.
  */
/**
  * zoran 20/12/15
  */
class DirectedNetwork{
  val network:DirectedSparseGraph[UserNode,UserLink]=new DirectedSparseGraph[UserNode,UserLink]
  val addednodes=new HashMap[Long,UserNode]()
  def addLink (link:UserLink, source:UserNode, target: UserNode){
    network.addEdge(link,source, target,EdgeType.DIRECTED)

  }
  def addNode(node:UserNode): Unit ={
    network.addVertex(node)
  }
  def getOrCreateUserNode(id:Long): UserNode ={
    val node=addednodes.getOrElseUpdate(id,{
      val usernode=new UserNode(id)
      addNode(usernode)
      usernode
    })
    node
  }
  def getNodes():Iterable[UserNode]={
    network.getVertices.asScala
  }
  def getLinks():Iterable[UserLink]={
    network.getEdges().asScala
  }

  /**
    * Divides the network into clusters by edge-betweenness. Calculates the edge-betweenness-metric for each publication and assigns a
    * numerical cluster-number [0 .. n] to each node.<br />
    * <br />
    * The metric is stored using both the identifiers {@link MetricContainer#EDGE_BETWEENNESS} and {@link MetricContainer#CLUSTER}. All
    * previously calculated values for the cluster-metric are overwritten.
    *
    * @param edgesToRemove
   *          The parameter influences how the clusters are determined. A higher parameter-value creates smaller and more cohesive clusters.
    *          A value of about 5 to 10 should be good choice.
    */
  def calculateEdgeBetweennessClustering(edgesToRemove:Int):ArrayBuffer[UserNode] ={
      val clusterer:EdgeBetweennessClusterer[UserNode, UserLink]=new EdgeBetweennessClusterer[UserNode, UserLink](edgesToRemove)
  println("NUMBER OF EDGES:"+network.getEdges.size())
      val clusteredUsers:java.util.Set[java.util.Set[UserNode]] = clusterer.transform(network);
    val clustUsers=clusteredUsers.asScala
    var clusterNo=0
    val finalUserNodes:ArrayBuffer[UserNode]=new ArrayBuffer[UserNode]()
    clustUsers.foreach{
      userNodes=> {

        println("CLUSTER**********"+clusterNo)
        val uNodes = userNodes.asScala
        uNodes.foreach {
          userNode =>
            userNode.cluster=clusterNo
            finalUserNodes+=userNode
            println("USER NODE:" + userNode.id + " cluster:" + userNode.cluster)
        }

        clusterNo+=1
      }
      }
    println("FINISHED")
     finalUserNodes
  }
}
