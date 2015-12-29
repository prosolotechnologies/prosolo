package org.prosolo.bigdata.scala.clustering.userprofiling

import java.io.InputStream

import scala.collection.mutable.{HashMap, Map}
 

object FeaturesToProfileMatcher {
  
  val clusterProfiles:Map[ClusterName.Value,ClusterTemplate]={
     val profileFeaturesTemplatesFile = "files/profilesfeaturestemplates.csv"
    val profiles:Map[ClusterName.Value,ClusterTemplate]=new HashMap[ClusterName.Value,ClusterTemplate]()
    initializeProfileTemplates()

    /**
      * We initialize profile templates from CSV file and use it for resolving which student profile cluster belongs
      */
    def initializeProfileTemplates(): Unit ={
      val profileLines=readLinesFromFile(profileFeaturesTemplatesFile)
      val header=profileLines(0)
      val hCols: Array[String] = header.split(",").map(_.trim)
      val headerCols:Array[String]=new Array[String](hCols.length-1)
      for( index<-headerCols.indices){
        if(index>0){
          headerCols(index-1)=hCols(index)
        }
      }
      for(i<- 1 to profileLines.length-1){
        val line=profileLines(i)
        val cols: Array[String] = line.split(",").map(_.trim)
        val featuresCols:Array[Double]=new Array[Double](cols.length-1)
        for(i<-1 to cols.length-1){
          featuresCols(i-1)=cols(i).toDouble
        }
        val cluster:ClusterTemplate=new ClusterTemplate(ClusterName.withName(cols(0)),headerCols, featuresCols)
          profiles.put(cluster.clusterName,cluster)


      }
    }
    def readLinesFromFile(file: String): Array[String] = {
      val stream: InputStream = getClass.getClassLoader.getResourceAsStream(file)
      val lines: Array[String] = scala.io.Source.fromInputStream(stream).getLines.toArray
      lines
    }
    profiles
  }

  /**
    * We are comparing cluster results with each cluster template in order to identify
    * how much it matches
    * @param clusterResults
    */
  def checkClustersMatching(clusterResults:ClusterResults){
    val featureValues:Map[Int,Tuple2[Double,Int]]= clusterResults.getFeatureValues()
     clusterProfiles.foreach{
      case (clusterName:ClusterName.Value,template:ClusterTemplate)=>
       val clusterMatching:Double= featureValues.foldLeft(0.0){
          case(acc:Double, (fvKey:Int, fvValue:Tuple2[Double,Int]))=>
            val templValue:Double= template.getFeatureValue(fvKey)
            val featureMatching=findFeatureMatching(fvValue._2,templValue)
            acc+featureMatching
        }
        clusterResults.addClusterMatching(clusterName,clusterMatching)
    }
  }

  /**
    * We are converting list of cluster matches based on their scores
    * @param clusterResult
    */
 def sortClustersMatchingByValues(clusterResult:ClusterResults){
   val clustersMathingList:List[(ClusterName.Value,Double)]= clusterResult.clustersMatching.toList
   val sorted=clustersMathingList.sortBy(_._2).reverse
   sorted.foreach{elem=>
     clusterResult.addSortedListElement(elem)
     
    }
 
    
  }

  /**
    * We are comparing template feature value with real cluster value to get the value of their matching
    * @param fValue
    * @param templAverage
    * @return
    */
  def findFeatureMatching(fValue:Int,templAverage:Double):Double={
    if(templAverage>0) 2-Math.abs(fValue-templAverage) else 0.0
  }
 
   
}

/**
  * Cluster template that keeps prototype of user profile based on the values specified in CSV file
  * @param clusterName
  * @param featureNames
  * @param featureValues
  */
class ClusterTemplate(val clusterName:ClusterName.Value, val featureNames: Array[String],val featureValues:Array[Double]){
  def getFeatureValue(featureId:Int):Double={
    featureValues(featureId)
  }
  
}


 