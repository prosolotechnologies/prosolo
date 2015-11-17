package org.prosolo.bigdata.scala.statistics

import scala.collection.mutable.{ Buffer, ListBuffer, ArrayBuffer, Map, HashMap }

class FeatureQuartiles {
  val quartiles=new Array[Double](3)
  val set:ArrayBuffer[Double]=ArrayBuffer[Double]()
  
  def getQuartiles():Tuple2[Double,Double]={
     (quartiles(0),quartiles(2))
  }
  /**
   * Checks which quartile value belongs to
   */
  def checkQuartileForFeatureValue(featureValue:Double):Tuple2[Array[Double],Int]={
   val value= if(featureValue<1){
      Math.round(featureValue)
    }else{
       featureValue
    }
    
    if(value<=quartiles(0)){
      (quartiles,1)
    }else if(value>=quartiles(2)){
     (quartiles,3)
    }else{
      (quartiles,2)
    }
  }
  
  def addValueToSet(value:Double)={

    set+=value
  }
  /**
	 * 
	 * @return Returns an array of three doubles representing the first,
	 * second (median) and third quartiles in elements 0, 1, and 2,
	 * respectively. The calculation of quartiles uses the Moore and McCabe
	 * method (aka M and M) as described by http://www.mathsisfun.com/data/quartiles.html.
	 */
  def findQuartiles():Tuple2[Double,Double]={
   // val set:ArrayBuffer[Double]=ArrayBuffer[Double](3.1,2.7,8.11,19.47,3.34,8.901,14.1,15.8,13.2,121.17)
    val sortedSet:ArrayBuffer[Double]=set.sorted
    println("SORTED:"+sortedSet)
    val middle:Integer=set.length/2
    println("MIDDLE:"+middle)
    if(sortedSet.length<1){
      println("DATA SET NOT INITIALIZED")
    }
    if(sortedSet.length==1){
      quartiles(0)=sortedSet(0)
      quartiles(1)=sortedSet(0)
      quartiles(2)=sortedSet(0)
    }else{
      //val cof:Array[Double]=Array[Double]()
      // val x=sortedSet.slice(0,middle)
      quartiles(0)= median(sortedSet.slice(0,middle))
      quartiles(1)=median(sortedSet)
   
      quartiles(2)=median(sortedSet.slice(middle+evenSetLength(sortedSet),sortedSet.length))
      println("Q1:"+quartiles(0)+" Q2:"+quartiles(1)+" Q3:"+quartiles(2))
 
    }
    (quartiles(0),quartiles(2))
  }
  def evenSetLength(set:ArrayBuffer[Double]):Integer={
    set.length % 2 
  }
  def median(set:ArrayBuffer[Double]):Double={
    val workingSet=set.clone()
    println("Working set:"+workingSet)
   // var median:Double=0
    if(set.length==0){
      println("ProvidedSet lenght is 0")
      0.0
      
    }else if(evenSetLength(set)==0){
      //taking average value of two median elements
      (set(set.length/2)+set(set.length/2-1))/2
    }else{
      set(set.length/2)
    }
    
   // median
    
  }
 
}