package org.prosolo.bigdata.scala.clustering

import scala.collection.mutable.{ Map, HashMap,ListBuffer }

class ClusterResults(val id: Int,val date: Long) {
  val featureValues:Map[Int,Tuple2[Double,Int]]=new HashMap[Int,Tuple2[Double,Int]]()
  val clustersMatching:Map[ClusterName.Value,Double]=new HashMap[ClusterName.Value,Double]()
  val sortedMatchingList:ListBuffer[(ClusterName.Value,Double)]=new ListBuffer[(ClusterName.Value,Double)]()
  
  def addFeatureValue(feature:Int,featureValue: Tuple2[Double,Int]){
    featureValues.put(feature, featureValue)
  }
  def getFeatureValues():Map[Int,Tuple2[Double,Int]]={
    featureValues
  }
  def addClusterMatching(cluster:ClusterName.Value, matchingValue:Double)={
    clustersMatching.put(cluster, matchingValue)
  }
  def addSortedListElement(elem: (ClusterName.Value,Double)){
    sortedMatchingList+=elem
  }
  
 
  

}