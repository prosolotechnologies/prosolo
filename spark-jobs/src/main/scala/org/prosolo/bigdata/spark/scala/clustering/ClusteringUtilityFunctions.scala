package org.prosolo.bigdata.spark.scala.clustering


import java.io.{BufferedWriter, FileWriter, PrintWriter}

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.SequenceFile
import org.apache.mahout.clustering.Cluster
import org.apache.mahout.clustering.canopy.CanopyDriver
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver
import org.apache.mahout.clustering.kmeans.{KMeansDriver, RandomSeedGenerator}
import org.apache.mahout.common.HadoopUtil
import org.apache.mahout.common.distance.{CosineDistanceMeasure, EuclideanDistanceMeasure}
import org.apache.mahout.math.NamedVector
import org.prosolo.bigdata.scala.clustering.userprofiling._
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Buffer, HashMap, Iterable, Map}

/**
  *
  * @author Zoran Jeremic
  * @date 2017-09-14
  * @since 1.0.0
  */
object ClusteringUtilityFunctions extends Serializable{
  val logger = LoggerFactory.getLogger(getClass)
  var featuresQuartiles: scala.collection.mutable.Map[Int, FeatureQuartiles] = new HashMap[Int, FeatureQuartiles]
  var matchedClusterProfiles: Map[Long, ClusterName.Value] = new HashMap[Long, ClusterName.Value]()
  /**
    * We are running clustering with selected algorithm
    */

  def runClustering(courseClusterConfiguration:CourseClusterConfiguration,numClusters:Int):Boolean= {
    var success=true;
    featuresQuartiles=new mutable.HashMap[Int, FeatureQuartiles]()
    matchedClusterProfiles=new mutable.HashMap[Long,ClusterName.Value]()
    HadoopUtil.delete(ClusteringUtils.conf, courseClusterConfiguration.output)
    val measure = new CosineDistanceMeasure()
    val clustersIn = new Path(courseClusterConfiguration.output, "random-seeds")

    RandomSeedGenerator.buildRandom(ClusteringUtils.conf, courseClusterConfiguration.datapath, clustersIn, numClusters, measure)
    logger.debug("BUILD RANDOM FINISHED for course:"+courseClusterConfiguration.courseId)
    ////end
    val convergenceDelta = 0.01
    val maxIterations = 50
    val clusterClassificationThreshold = 0.0
    val m: Float = 0.01f
    if (ClusteringUtils.algorithmType == AlgorithmType.KMeans) {
      try{
        KMeansDriver.run(ClusteringUtils.conf, courseClusterConfiguration.datapath, clustersIn, courseClusterConfiguration.output, convergenceDelta, maxIterations, true, clusterClassificationThreshold, true)
      }catch{
        case ise: IllegalStateException=>
          logger.debug("ERROR in KMeansDriver.run:"+ise.getMessage)
          //sparkJob.submitTaskProblem(ise.getMessage,courseClusterConfiguration.courseId,"KMeansClustering",ProblemSeverity.MAJOR)
          success=false;
      }

    } else if (ClusteringUtils.algorithmType == AlgorithmType.Canopy) {
      CanopyDriver.run(ClusteringUtils.conf, clustersIn, courseClusterConfiguration.output, new EuclideanDistanceMeasure(), 20, 5, false, clusterClassificationThreshold, false)
    } else if (ClusteringUtils.algorithmType == AlgorithmType.FuzzyKMeans) {
      FuzzyKMeansDriver.run(ClusteringUtils.conf, courseClusterConfiguration.datapath, clustersIn, courseClusterConfiguration.output, convergenceDelta, maxIterations, m, true, true, clusterClassificationThreshold, false)
    }

    // CanopyDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, 0.0, true)
    success
  }
  def readAndProcessClusters(usersQuartilesFeatures: Predef.Map[Long, Array[Double]],daysSinceEpoch:IndexedSeq[Long], courseId:Long, output:Path, outputDir:String,numFeatures:Int, numClusters:Int) : Iterable[Tuple5[Long,String,Long,Long,String]]={

    val clusters=ClusterHelper.readClusters(ClusteringUtils.conf, output)

    val clusterResults: Buffer[ClusterResults] = evaluateClustersFeaturesQuartiles(clusters,numFeatures)
    evaluateClustersResultsForProfiles(clusterResults)
    val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("features_outputs_"+ClusteringUtils.algorithmType.toString+".csv", true)))
    csvfilewriter.append("*********")
    csvfilewriter.append("\r\n")
    clusterResults.foreach(results=>
    {
      logger.debug(" ")
      logger.debug("CLUSTER:"+results.id)
      logger.debug("FEATURES:"+results.featureValues)

      val line="FEATURE QUARTILES:"+ results.featureValues.map {
        case (fid, triple) => {
          (fid, triple._2)
        }
      }
      logger.debug(line)
      logger.debug("MATCHING LIST:"+results.sortedMatchingList)
      csvfilewriter.append(results.id+",")
      results.featureValues.foreach{
        case(featureKey:Int, featureValue:Tuple2[Double,Int])=>
          csvfilewriter.append(featureKey+","+featureValue._1+","+featureValue._2+",")
      }
      csvfilewriter.append("\r\n")
    })
    csvfilewriter.close()
    findClustersAffiliation(clusterResults,numClusters)
    val usersQuartilesFeaturesAndClusters: scala.collection.mutable.HashMap[Long, (Int, Array[Double])]=findClustersMembers(usersQuartilesFeatures,outputDir)
    val usercourseprofiles:Iterable[Tuple5[Long,String,Long,Long,String]]=storeUserQuartilesFeaturesToClusterFiles(usersQuartilesFeaturesAndClusters,daysSinceEpoch, courseId)

    usercourseprofiles

  }
  def findClustersAffiliation(clusterResults: Buffer[ClusterResults],numClusters:Int) {
    val matchedElements: Map[ClusterName.Value, ClusterResults] = new HashMap[ClusterName.Value, ClusterResults]()
    val matchedIds:ArrayBuffer[Int]=new ArrayBuffer[Int]()
    for {index <- 0 to numClusters-1
         if (matchedIds.size<numClusters)
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
    logger.debug("***********************************************************************")
    logger.debug("CLUSTERS ASSOCIATIONS")
    matchedElements.foreach{
      case(a,b)=>{
        val line:String="cluster:" +b.id+" matches: "+a+" cluster matching:"+b.sortedMatchingList
        matchedClusterProfiles.put(b.id,a)
        logger.debug(line)
      }
    }
  }
  def findClustersMembers(usersQuartilesFeatures: Predef.Map[Long, Array[Double]], outputDir:String): scala.collection.mutable.HashMap[Long, (Int, Array[Double])] ={

    val reader = new SequenceFile.Reader(ClusteringUtils.fs, new Path(outputDir + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-0"), ClusteringUtils.conf)
    val key = new org.apache.hadoop.io.IntWritable()
    val value = new WeightedPropertyVectorWritable()
    val usersQuartilesFeaturesAndClusters: scala.collection.mutable.HashMap[Long, (Int, Array[Double])]=new HashMap[Long,Tuple2[Int,Array[Double]]]()
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
  def storeUserQuartilesFeaturesToClusterFiles(usersQuartilesFeaturesAndClusters: scala.collection.mutable.HashMap[Long, (Int, Array[Double])],
                                               days:IndexedSeq[Long], courseId:Long):Iterable[Tuple5[Long,String,Long,Long,String]] ={
    // val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("features_outputs_"+algorithmType.toString+".csv", true)))
    val clusterswriters:HashMap[Int,PrintWriter]=new HashMap[Int,PrintWriter]()
    //val dates=startDateSinceEpoch.toString+"_"+endDateSinceEpoch.toString
    val dates=days(0)+"_"+days(days.length-1)
    val endDateSinceEpoch=days(days.length-1)
    val usercourseSequences:Iterable[Tuple5[Long,String,Long,Long,String]]=usersQuartilesFeaturesAndClusters.map{
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
        logger.debug("course:" + courseId + " cluster-name:" + clusterProfile + " userid:" + userid + " date:" + endDateSinceEpoch + " sequence:" + userQuartilesSequence.map {
          feature => FeatureQuartiles.matchQuartileValueToQuartileName(feature)
        }.mkString(","))
        (courseId, clusterProfile, endDateSinceEpoch, userid, sequence)

      }

    }

    //usercourseSequences.saveToCassandra(dbName,TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE)

    clusterswriters.foreach{
      case(clusterid, clusterwriter)=>{
        clusterwriter.close
      }
    }
    usercourseSequences
  }
   def evaluateClustersFeaturesQuartiles(clusters: java.util.List[java.util.List[Cluster]],numFeatures:Int): Buffer[ClusterResults] = {
    val clusterResults: Buffer[ClusterResults] = clusters.get(clusters.size - 1).asScala
      .map { cluster =>
        extractClusterResultFeatureQuartileForCluster(cluster,numFeatures)
      }
    clusterResults
  }
  /**
    * We are extracting Quartile from the feature in order to match it against the template
    *
    * @param cluster
    * @return
    */
  def extractClusterResultFeatureQuartileForCluster(cluster: Cluster,numFeatures:Int): ClusterResults = {
    val cid = cluster.getId
    val clusterResult = new ClusterResults(cid)
    for (i <- 0 to numFeatures - 1) {
      val featureVal = cluster.getCenter.get(i)
      val (featureQuartileMean: Array[Double], featureQuartile) = featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).checkQuartileForFeatureValue(featureVal)
      clusterResult.addFeatureValue(i, (featureVal, featureQuartile))
    }
    clusterResult
  }
  def getMatchedClusterProfile(clusterId:Long):String={
    matchedClusterProfiles.getOrElse(clusterId,"").toString
  }
   /**
    * We are comparing cluster results with each cluster template in order to identify
    * how much it matches and sort these values
    *
    * @param clusterResults
    */
  def evaluateClustersResultsForProfiles(clusterResults: Buffer[ClusterResults]) {
    clusterResults.foreach { clusterResult =>
      FeaturesToProfileMatcher.checkClustersMatching(clusterResult)
     FeaturesToProfileMatcher.sortClustersMatchingByValues(clusterResult)
    }

  }

}
