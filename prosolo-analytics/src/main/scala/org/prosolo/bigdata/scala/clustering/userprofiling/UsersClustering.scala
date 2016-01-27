package org.prosolo.bigdata.scala.clustering.userprofiling

import java.io.{BufferedWriter, FileWriter, PrintWriter}
import java.util.Date

import com.datastax.driver.core.Row
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{SequenceFile, Text}
import org.apache.mahout.clustering.Cluster
import org.apache.mahout.clustering.canopy.CanopyDriver
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver
import org.apache.mahout.clustering.iterator.ClusterWritable
import org.apache.mahout.clustering.kmeans.{KMeansDriver, RandomSeedGenerator}
import org.apache.mahout.common.HadoopUtil
import org.apache.mahout.common.distance.{CosineDistanceMeasure, EuclideanDistanceMeasure}
import org.apache.mahout.math.{DenseVector, NamedVector, VectorWritable}
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl
//import org.prosolo.bigdata.scala.clustering.
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles
import org.prosolo.bigdata.utils.DateUtil

import scala.IndexedSeq


import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable._

/**
  * Created by Zoran on 22/11/15.
  */
/**
  * Zoran 22/11/15
  */
class UsersClustering  {
 // val dbManager = new UserObservationsDBManagerImpl()

//val courseId:Long=0//we are still not taking account of this value. It should be fixed in Cassandra DAO

  //map feature id to FeatureQuartile
  val featuresQuartiles: mutable.Map[Int, FeatureQuartiles] = new HashMap[Int, FeatureQuartiles]
  val matchedClusterProfiles: Map[Long, ClusterName.Value] = new HashMap[Long, ClusterName.Value]()





  def getMatchedClusterProfile(clusterId:Long):String={
    matchedClusterProfiles.getOrElse(clusterId,"").toString
  }


  /**
    * Main function that executes clustering
    * @param startDate
    * @param endDate
    */
  def performKMeansClusteringForPeriod(startDate: Date, endDate: Date, courseId: Long) = {
    outputResults("ALGORITHM:"+ClusteringUtils.algorithmType.toString)


    val startDateSinceEpoch = DateUtil.getDaysSinceEpoch(startDate)
    val endDateSinceEpoch = DateUtil.getDaysSinceEpoch(endDate)
    outputResults("PERIOD start:"+startDate+"("+startDateSinceEpoch+")"+" endDate:"+endDate+"("+endDateSinceEpoch+")")
    val usersFeatures: Predef.Map[Long, Array[Double]] = startDateSinceEpoch.to(endDateSinceEpoch)
      .flatMap{date=>mapUserObservationsForDateToRows(date,courseId)}
      .groupBy { row: Row => row.getLong(1) }
      .transform((userid, userRows) => transformUserFeaturesForPeriod(userid, userRows))

    extractFeatureQuartilesValues(usersFeatures)
    evaluateFeaturesQuartiles()
    val usersQuartilesFeatures: Predef.Map[Long, Array[Double]] =  usersFeatures.transform((userid, userFeatures)=>transformUserFeaturesToFeatureQuartiles(userid, userFeatures))
    prepareSequenceFile(usersQuartilesFeatures)
    //runClustering()
  if(runClustering()){
    readAndProcessClusters(usersQuartilesFeatures, startDateSinceEpoch, endDateSinceEpoch, courseId)
    outputResults("******************************************************************************************")
  }else{
    println("there was no data in cluster")
  }

    // runClustering(date)
  }

  //////////////////////////////////////////
  //Functions to prepare data for clustering
  //////////////////////////////////////////
  /**
    * We are retrieving data about user activities for individual dates
    * @param date
    * @return
    */
  def mapUserObservationsForDateToRows(date: Long, courseId:Long) = {
    val rows: java.util.List[Row] = UserObservationsDBManagerImpl.getInstance().findAllUsersProfileObservationsForDate(date, courseId)
    rows.asScala.toList
  }

  /**
    * For each user feature value finds appropriate quartile
    * @param userid
    * @param userFeatures
    * @return
    */
  def transformUserFeaturesToFeatureQuartiles(userid:Long, userFeatures: Array[Double]): Array[Double]={
  val quartilesFeaturesArray: Array[Double] = new Array[Double](ClusteringUtils.numFeatures)
  for(i<-0 to ClusteringUtils.numFeatures - 1){
    val quartile:FeatureQuartiles=featuresQuartiles.getOrElseUpdate(i,new FeatureQuartiles())
 //   println("Transforming feature:"+i+" featureValue:"+userFeatures(i)+" quartile:"+quartile.getQuartileForFeatureValue(userFeatures(i)))

    quartilesFeaturesArray(i)=quartile.getQuartileForFeatureValue(userFeatures(i))
  }
  quartilesFeaturesArray
}
  /**
    * We are tranforming data for different dates for one user and collect it in single Array of features
    * @param userid
    * @param userRows
    * @return
    */
  def transformUserFeaturesForPeriod(userid: Long, userRows: IndexedSeq[Row]) = {
    val featuresArray: Array[Double] = new Array[Double](ClusteringUtils.numFeatures)
    for (userRow <- userRows) {
      for (i <- 0 to ClusteringUtils.numFeatures - 1) {
        val featureValue = userRow.getLong(i + 2).toDouble
        featuresArray(i) = featuresArray(i).+(featureValue)
        featuresArray(i) = featuresArray(i).+(featureValue)
      }
    }
    featuresArray
  }

  /**
    * We are axtracting values for individual features in order to resolve quartiles later
    * @param usersFeatures
    */
  def extractFeatureQuartilesValues(usersFeatures: collection.Map[Long, Array[Double]]): Unit = {
    usersFeatures.foreach {
      case (userid: Long, userFeatures: Array[Double]) =>
        for (i <- 0 to (userFeatures.length - 1)) {
          val featureValue = userFeatures(i)
          featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).addValueToSet(featureValue)
        }
    }
  }

  /**
    * We are writing user features vectors to the Sequence files
    * @param usersFeatures
    */
  def prepareSequenceFile(usersFeatures: collection.Map[Long, Array[Double]]): Unit = {

    val vectors = new ListBuffer[NamedVector]
    usersFeatures.foreach {
      case (userid: Long, featuresArray: Array[Double]) =>
        val dv = new DenseVector(featuresArray)
        vectors += (new NamedVector(dv, userid.toString()))
    }
    val valClass=if(ClusteringUtils.algorithmType==AlgorithmType.Canopy) classOf[ClusterWritable] else classOf[VectorWritable]
    val writer = new SequenceFile.Writer(ClusteringUtils.fs, ClusteringUtils.conf, ClusteringUtils.datapath, classOf[Text],valClass)
    val vec =  new VectorWritable()
    vectors.foreach { vector =>
      vec.set(vector)
      writer.append(new Text(vector.getName), vec)
    }
    writer.close()
    }

  /**
    * We are running clustering with selected algorithm
    */

  def runClustering():Boolean= {

    HadoopUtil.delete(ClusteringUtils.conf, ClusteringUtils.output)
    val measure = new CosineDistanceMeasure()
    val clustersIn = new Path(ClusteringUtils.output, "random-seeds")
    var success=true;
    RandomSeedGenerator.buildRandom(ClusteringUtils.conf, ClusteringUtils.datapath, clustersIn, ClusteringUtils.numClusters, measure)
    val convergenceDelta = 0.01
    val maxIterations = 50
    val clusterClassificationThreshold = 0.0
    val m: Float = 0.01f
    if (ClusteringUtils.algorithmType == AlgorithmType.KMeans) {
      try{
        KMeansDriver.run(ClusteringUtils.conf, ClusteringUtils.datapath, clustersIn, ClusteringUtils.output, convergenceDelta, maxIterations, true, clusterClassificationThreshold, true)
      }catch{
        case ise: IllegalStateException=>
          success=false;
      }

    } else if (ClusteringUtils.algorithmType == AlgorithmType.Canopy) {
      CanopyDriver.run(ClusteringUtils.conf, clustersIn, ClusteringUtils.output, new EuclideanDistanceMeasure(), 20, 5, false, clusterClassificationThreshold, false)
    } else if (ClusteringUtils.algorithmType == AlgorithmType.FuzzyKMeans) {
      FuzzyKMeansDriver.run(ClusteringUtils.conf, ClusteringUtils.datapath, clustersIn, ClusteringUtils.output, convergenceDelta, maxIterations, m, true, true, clusterClassificationThreshold, false)
    }

    // CanopyDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, 0.0, true)
    success
  }

  /**
    * W
    */
  def readAndProcessClusters(usersQuartilesFeatures: Predef.Map[Long, Array[Double]],startDateSinceEpoch:Long, endDateSinceEpoch:Long, courseId:Long) {
    val clusters = ClusterHelper.readClusters(ClusteringUtils.conf, ClusteringUtils.output)

     val clusterResults: Buffer[ClusterResults] = evaluateClustersFeaturesQuartiles(clusters)
    evaluateClustersResultsForProfiles(clusterResults)
    val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("features_outputs_"+ClusteringUtils.algorithmType.toString+".csv", true)))
    csvfilewriter.append("*********")
    csvfilewriter.append("\r\n")
    clusterResults.foreach(results=>
    {
      outputResults(" ")
      outputResults("CLUSTER:"+results.id)
      outputResults("FEATURES:"+results.featureValues)

      val line="FEATURE QUARTILES:"+ results.featureValues.map {
        case (fid, triple) => {
          (fid, triple._2)
        }
      }
      outputResults(line)
       outputResults("MATCHING LIST:"+results.sortedMatchingList)
      csvfilewriter.append(results.id+",")
       results.featureValues.foreach{
         case(featureKey:Int, featureValue:Tuple2[Double,Int])=>
           csvfilewriter.append(featureKey+","+featureValue._1+","+featureValue._2+",")
       }
      csvfilewriter.append("\r\n")
    })
    csvfilewriter.close()
    findClustersAffiliation(clusterResults)
    val usersQuartilesFeaturesAndClusters: mutable.HashMap[Long, (Int, Array[Double])]=findClustersMembers(usersQuartilesFeatures)
    storeUserQuartilesFeaturesToClusterFiles(usersQuartilesFeaturesAndClusters,startDateSinceEpoch, endDateSinceEpoch, courseId)

  }

  def storeUserQuartilesFeaturesToClusterFiles(usersQuartilesFeaturesAndClusters: mutable.HashMap[Long, (Int, Array[Double])],
                                               startDateSinceEpoch:Long, endDateSinceEpoch:Long, courseId:Long): Unit ={
   // val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("features_outputs_"+algorithmType.toString+".csv", true)))
    val clusterswriters:HashMap[Int,PrintWriter]=new HashMap[Int,PrintWriter]()
    val dates=startDateSinceEpoch.toString+"_"+endDateSinceEpoch.toString
    usersQuartilesFeaturesAndClusters.foreach{
      case(userid, valueTuple)=> {
        val clusterId = valueTuple._1
        val clusterProfile = getMatchedClusterProfile(clusterId)
        val key = clusterId.toString + "_" + dates.toString

        val clusterWriter = clusterswriters.getOrElseUpdate(clusterId,
          new PrintWriter(new BufferedWriter(new FileWriter("features_quartiles_" + key + ".seq", true))))
        val userQuartilesSequence: Array[Double] = valueTuple._2
        userQuartilesSequence.foreach { feature =>
          clusterWriter.append(FeatureQuartiles.matchQuartileValueToQuartileName(feature) + ";")
        }
        clusterWriter.append("\r\n")
        val sequence = userQuartilesSequence.map {
          feature => FeatureQuartiles.matchQuartileValueToQuartileName(feature)
        }.mkString(",")

        UserObservationsDBManagerImpl.getInstance().insertUserQuartileFeaturesByProfile(courseId, clusterProfile, endDateSinceEpoch, userid, sequence);
        UserObservationsDBManagerImpl.getInstance().insertUserQuartileFeaturesByDate(courseId, endDateSinceEpoch, userid, clusterProfile, sequence);
        println("course:" + courseId + " cluster-name:" + clusterProfile + " userid:" + userid + " date:" + endDateSinceEpoch + " sequence:" + userQuartilesSequence.map {
          feature => FeatureQuartiles.matchQuartileValueToQuartileName(feature)
        }.mkString(","))

      }
    }
    clusterswriters.foreach{
      case(clusterid, clusterwriter)=>{
           clusterwriter.close
      }
    }
  }

  /**
    * For each feature finds quartiles based on the maximum identified value
    */
  def evaluateFeaturesQuartiles() {
    for (i <- 0 to ClusteringUtils.numFeatures - 1) {
      featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).findQuartiles()
    }
  }



  def evaluateClustersFeaturesQuartiles(clusters: java.util.List[java.util.List[Cluster]]): Buffer[ClusterResults] = {
    val clusterResults: Buffer[ClusterResults] = clusters.get(clusters.size - 1).asScala
      .map { cluster =>
        extractClusterResultFeatureQuartileForCluster(cluster)
      }
    clusterResults
  }

  /**
    * We are extracting Quartile from the feature in order to match it against the template
    * @param cluster
    * @return
    */
  def extractClusterResultFeatureQuartileForCluster(cluster: Cluster): ClusterResults = {
    val cid = cluster.getId
    val clusterResult = new ClusterResults(cid)
    for (i <- 0 to ClusteringUtils.numFeatures - 1) {
      val featureVal = cluster.getCenter.get(i)
      val (featureQuartileMean: Array[Double], featureQuartile) = featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).checkQuartileForFeatureValue(featureVal)
      clusterResult.addFeatureValue(i, (featureVal, featureQuartile))
    }
    clusterResult
  }

  /**
    * We are comparing cluster results with each cluster template in order to identify
    * how much it matches and sort these values
    * @param clusterResults
    */
  def evaluateClustersResultsForProfiles(clusterResults: Buffer[ClusterResults]) {
    clusterResults.foreach { clusterResult =>
      FeaturesToProfileMatcher.checkClustersMatching(clusterResult)
      FeaturesToProfileMatcher.sortClustersMatchingByValues(clusterResult)
    }
  }

  /**
    * We are trying to find which cluster is best match for defined user profile
    * @param clusterResults
    */
  def findClustersAffiliation(clusterResults: Buffer[ClusterResults]) {
    val matchedElements: Map[ClusterName.Value, ClusterResults] = new HashMap[ClusterName.Value, ClusterResults]()
    val matchedIds:ArrayBuffer[Int]=new ArrayBuffer[Int]()
    for {index <- 0 to ClusteringUtils.numClusters-1
      if (matchedIds.size<ClusteringUtils.numClusters)
    } {
      val elementsToCheck: Map[ClusterName.Value, ArrayBuffer[(ClusterResults,Double)]] = new HashMap[ClusterName.Value, ArrayBuffer[(ClusterResults,Double)]]()
      clusterResults.foreach { clusterResult =>
        val matchElem = clusterResult.sortedMatchingList(index)
        //checking if cluster was not already resolved
        if(!matchedElements.contains(matchElem._1)){
          val tempList = elementsToCheck.getOrElse(matchElem._1,new ArrayBuffer[(ClusterResults,Double)]())
          //  val elem=(clusterResult,matchElem._2)
          tempList +=new Tuple2(clusterResult,matchElem._2)
          elementsToCheck.put(matchElem._1, tempList)
        }

      }
      elementsToCheck.foreach {

        case (clusterName: ClusterName.Value, matchingResults: ArrayBuffer[(ClusterResults, Double)]) =>
          if (!matchedElements.contains(clusterName)) {
            if (matchingResults.size == 1 && !matchedIds.contains(matchingResults(0)._1.id)) {
              matchedElements.put(clusterName, matchingResults(0)._1)
              matchedIds += matchingResults(0)._1.id
            //} else
            } else if (matchingResults.size > 1) {
              val sorted: ArrayBuffer[(ClusterResults, Double)] = matchingResults.sortBy(_._2).reverse
              val notAssignedSorted = sorted.filterNot { p: (ClusterResults, Double) => matchedIds.contains(p._1.id) }
                     if(notAssignedSorted.size>0){
                val highest = notAssignedSorted.head._2
                val filtered = notAssignedSorted.filter {
                  _._2 == highest
                }
                if (filtered.size == 1) {
                  matchedElements.put(clusterName, filtered(0)._1)
                  matchedIds += filtered(0)._1.id
                } else {
                  matchedElements.put(clusterName, filtered(0)._1)
                  matchedIds += filtered(0)._1.id
                }
              }

            }

          }
      }
      }
    outputResults("***********************************************************************")
    outputResults("CLUSTERS ASSOCIATIONS")
    matchedElements.foreach{
      case(a,b)=>{
        val line:String="cluster:" +b.id+" matches: "+a+" cluster matching:"+b.sortedMatchingList
        matchedClusterProfiles.put(b.id,a)
        outputResults(line)
    }
    }
  }
  def findClustersMembers(usersQuartilesFeatures: Predef.Map[Long, Array[Double]]): mutable.HashMap[Long, (Int, Array[Double])] ={

    val reader = new SequenceFile.Reader(ClusteringUtils.fs, new Path(ClusteringUtils.outputDir + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-0"), ClusteringUtils.conf)
    val key = new org.apache.hadoop.io.IntWritable()
    val value = new WeightedPropertyVectorWritable()
    val usersQuartilesFeaturesAndClusters: mutable.HashMap[Long, (Int, Array[Double])]=new HashMap[Long,Tuple2[Int,Array[Double]]]()
    while (reader.next(key, value)) {
      val namedVector:NamedVector=value.getVector().asInstanceOf[NamedVector]

      val userid:Long=namedVector.getName.toLong
      val clusterid:Int=key.toString.toInt

      val newValue=(clusterid,usersQuartilesFeatures.getOrElse(userid,new Array[Double](ClusteringUtils.numFeatures)))
        usersQuartilesFeaturesAndClusters.put(userid,newValue)
    }
    reader.close()
    usersQuartilesFeaturesAndClusters
  }

  def outputResults(line:Any){
    val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("clusters_outputs_"+ClusteringUtils.algorithmType.toString+".txt", true)))

    csvfilewriter.append(s"$line")
    csvfilewriter.append("\r\n")

    csvfilewriter.close()
  }

}
