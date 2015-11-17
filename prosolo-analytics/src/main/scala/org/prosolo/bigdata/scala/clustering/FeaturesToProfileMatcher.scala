package org.prosolo.bigdata.scala.clustering

import scala.collection.mutable.{ Buffer,  Map, HashMap }
 

object FeaturesToProfileMatcher {
  
  val clusterProfiles:Map[ClusterName.Value,ClusterTemplate]={
    println("init profiles")
    
    val profiles:Map[ClusterName.Value,ClusterTemplate]=new HashMap[ClusterName.Value,ClusterTemplate]()   
    //cluster 1 template - Cluster A
    val c1:ClusterTemplate=new ClusterTemplate
    c1.addFeatureValue(0, Array(3))//discussion
    c1.addFeatureValue(1, Array(1))//lmsuse
    //  c1.addFeatureValue(2, Array(0))//login
    c1.addFeatureValue(2, Array(1))//resourceview
    profiles.put(ClusterName.A, c1)
    
    //cluster 2-3 template - Cluster B
    val c2:ClusterTemplate=new ClusterTemplate
    c2.addFeatureValue(0, Array(1))//discussion
    c2.addFeatureValue(1, Array(1,2))//lmsuse
    //  c2.addFeatureValue(2, Array(0))//login
    c2.addFeatureValue(2, Array(1,2))//resourceview
    profiles.put(ClusterName.B, c2)
    
    
    //cluster 4 template - Cluster C
    val c3:ClusterTemplate=new ClusterTemplate
    c3.addFeatureValue(0, Array(3))//discussion
    c3.addFeatureValue(1, Array(3))//lmsuse
     // c3.addFeatureValue(2, Array(0))//login
    c3.addFeatureValue(2, Array(3))//resourceview
    profiles.put(ClusterName.C, c3)
    
    //cluster 5-6 template - Cluster D
    val c4:ClusterTemplate=new ClusterTemplate
    c4.addFeatureValue(0, Array(2))//discussion
    c4.addFeatureValue(1, Array(2,3))//lmsuse
     // c4.addFeatureValue(2, Array(0))//login
    c4.addFeatureValue(2, Array(2))//resourceview
    profiles.put(ClusterName.D, c4)    
    
    profiles
  }
  
  def checkClustersMatching(clusterResults:ClusterResults){
    println("*-*-*-*-*-*-*-*-*-*-*-checkClustersMatching for:"+clusterResults.id)
    val featureValues:Map[Int,Tuple2[Double,Int]]= clusterResults.getFeatureValues()
  // val clustersMatching:Map[ClusterName.Value,Double]=
    println("FEATURES:"+featureValues.toString())
     clusterProfiles.foreach{
      case (clusterName:ClusterName.Value,template:ClusterTemplate)=>
       val clusterMatching:Double= featureValues.foldLeft(0.0){
          case(acc:Double, (fvKey:Int, fvValue:Tuple2[Double,Int]))=>
            val templValue:Array[Int]= template.getFeatureValue(fvKey)
            val featureMatching=findFeatureMatching(fvValue._2,templValue)
           // println("FEATURE:"+fvKey+" has value:"+fvValue._2+" TEMPLATE:"+templValue.mkString(" ")+" MATCHING:"+featureMatching)
            acc+featureMatching
        }
       println("***CLUSTER:"+clusterName.toString()+" matching:"+clusterMatching)
       // (clusterName,clusterMatching)
        clusterResults.addClusterMatching(clusterName,clusterMatching)
    }
  }
 def sortClustersMatchingByValues(clusterResult:ClusterResults){
    println("Sort cluster matching")
   val clustersMathingList:List[(ClusterName.Value,Double)]= clusterResult.clustersMatching.toList
    println("UNSORTED LIST:"+clustersMathingList)
   val sorted=clustersMathingList.sortBy(_._2).reverse 
      println("SORTED LIST:"+sorted)
   sorted.foreach{elem=>
     clusterResult.addSortedListElement(elem)
     
    }
 
    
  }
  
  def findFeatureMatching(fValue:Int,tValues:Array[Int]):Double={
    val templAverage:Double=tValues.foldLeft(0)(_+_)/(tValues.size).toDouble
    if(templAverage>0) 2-Math.abs(fValue-templAverage) else 0.0
  }
 
   
}
class ClusterTemplate{
  val featureValues:Map[Int,Array[Int]]=new HashMap[Int,Array[Int]]()
  def addFeatureValue(featureId:Int,values:Array[Int]){
    featureValues.put(featureId, values)
  }
  def getFeatureValue(featureId:Int):Array[Int]={
    featureValues.getOrElse(featureId,Array(0))
  }
  
}


 