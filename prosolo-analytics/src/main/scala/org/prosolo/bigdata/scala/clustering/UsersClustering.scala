package org.prosolo.bigdata.scala.clustering

import java.io.{FileWriter, BufferedWriter, PrintWriter}
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import com.datastax.driver.core.Row
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.hadoop.io.{Text, SequenceFile}
import org.apache.mahout.clustering.Cluster
import org.apache.mahout.clustering.canopy.CanopyDriver
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver
import org.apache.mahout.clustering.iterator.ClusterWritable
import org.apache.mahout.clustering.kmeans.{KMeansDriver, RandomSeedGenerator}
import org.apache.mahout.common.HadoopUtil
import org.apache.mahout.common.distance.{EuclideanDistanceMeasure, CosineDistanceMeasure}
import org.apache.mahout.math.{VectorWritable, NamedVector, DenseVector}
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles
import org.prosolo.bigdata.utils.DateUtil

import scala.IndexedSeq

//import scala.Predef.Map
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable._

/**
  * Created by Zoran on 22/11/15.
  */
/**
  * Zoran 22/11/15
  */
object UsersClustering extends App {
  val dbManager = new UserObservationsDBManagerImpl()
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

  val startDate: Date = dateFormat.parse("10/20/2014")
  val endDate: Date = dateFormat.parse("10/22/2014")
  val endDate2: Date = dateFormat.parse("12/20/2014")
  val numFeatures = 12

  val clustersDir = "clustersdir"
  val vectorsDir = clustersDir + "/users"
  val outputDir: String = clustersDir + "/output"
  val output = new Path(outputDir)
  val conf = new Configuration()
  val fs = FileSystem.get(conf)
  val datapath = new Path(vectorsDir + "/part-00000")
  val algorithmType: AlgorithmType.AlgorithmType = AlgorithmType.KMeans
  val numClusters = 4
  val periodDays=7

  //map feature id to FeatureQuartile
  val featuresQuartiles: mutable.Map[Int, FeatureQuartiles] = new HashMap[Int, FeatureQuartiles]

 calculateUsersClusteringForPeriod(startDate, endDate)
 // runPeriodicalClustering(startDate,endDate2)
  def addDaysToDate(date:Date, days:Int): Date ={
    val cal:Calendar=Calendar.getInstance()
    cal.setTime(date)
    cal.add(Calendar.DATE,days)
    val newDate:Date=cal.getTime
    newDate
  }

  /**
    * For the specific period of time e.g. course, runs clustering in specific intervals, e.g. week
    * @param startDate
    * @param endDate
    */
  def runPeriodicalClustering(startDate:Date, endDate:Date): Unit ={
    outputResults("******************************************************************************************")
    outputResults("******************************************************************************************")
    outputResults("******************************************************************************************")
    var tempDate=startDate

    while(endDate.compareTo(tempDate)>0){
      calculateUsersClusteringForPeriod(tempDate,addDaysToDate(tempDate,periodDays))
      tempDate=addDaysToDate(tempDate, periodDays+1)

    }
  }

  /**
    * Main function that executes clustering
    * @param startDate
    * @param endDate
    */
  def calculateUsersClusteringForPeriod(startDate: Date, endDate: Date) = {
    outputResults("ALGORITHM:"+algorithmType.toString)


    val startDateSinceEpoch = DateUtil.getDaysSinceEpoch(startDate)
    val endDateSinceEpoch = DateUtil.getDaysSinceEpoch(endDate)
    outputResults("PERIOD start:"+startDate+"("+startDateSinceEpoch+")"+" endDate:"+endDate+"("+endDateSinceEpoch+")")
    val usersFeatures: Predef.Map[Long, Array[Double]] = startDateSinceEpoch.to(endDateSinceEpoch)
      .flatMap(mapDateToRows)
      .groupBy { row: Row => row.getLong(1) }
      .transform((userid, userRows) => transformUserFeaturesForPeriod(userid, userRows))

    extractFeatureQuartilesValues(usersFeatures)
    prepareSequenceFile(usersFeatures)
    runClustering()

    readAndProcessClusters()
    outputResults("******************************************************************************************")
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
  def mapDateToRows(date: Long) = {
    val rows: java.util.List[Row] = dbManager.findAllUsersProfileObservationsForDate(date)
    rows.asScala.toList
  }

  /**
    * We are tranforming data for different dates for one user and collect it in single Array of features
    * @param userid
    * @param userRows
    * @return
    */
  def transformUserFeaturesForPeriod(userid: Long, userRows: IndexedSeq[Row]) = {
    val featuresArray: Array[Double] = new Array[Double](numFeatures)
    for (userRow <- userRows) {
      for (i <- 0 to numFeatures - 1) {
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
        println("USER:"+userid+" featuresArray:"+dv)
        vectors += (new NamedVector(dv, userid.toString()))
    }
    val valClass=if(algorithmType==AlgorithmType.Canopy) classOf[ClusterWritable] else classOf[VectorWritable]
    val writer = new SequenceFile.Writer(fs, conf, datapath, classOf[Text],valClass)
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

  def runClustering() {

    HadoopUtil.delete(conf, output)
    val measure = new CosineDistanceMeasure()
    val clustersIn = new Path(output, "random-seeds")
    RandomSeedGenerator.buildRandom(conf, datapath, clustersIn, numClusters, measure)
    val convergenceDelta = 0.01
    val maxIterations = 50
    val clusterClassificationThreshold = 0.0
    val m: Float = 0.01f
    if (algorithmType == AlgorithmType.KMeans) {
      KMeansDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, clusterClassificationThreshold, true)
    } else if (algorithmType == AlgorithmType.Canopy) {
      CanopyDriver.run(conf, clustersIn, output, new EuclideanDistanceMeasure(), 20, 5, false, clusterClassificationThreshold, false)
    } else if (algorithmType == AlgorithmType.FuzzyKMeans) {
      FuzzyKMeansDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, m, true, true, clusterClassificationThreshold, false)
    }

    // CanopyDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, 0.0, true)

  }

  /**
    * W
    */
  def readAndProcessClusters() {
    val clusters = ClusterHelper.readClusters(conf, output)
    evaluateFeaturesQuartiles()
     val clusterResults: Buffer[ClusterResults] = evaluateClustersFeaturesQuartiles(clusters)
    evaluateClustersResultsForProfiles(clusterResults)
    val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("features_outputs_"+algorithmType.toString+".csv", true)))
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
      println("LINE:"+line)
      outputResults(line)
       outputResults("MATCHING LIST:"+results.sortedMatchingList)
      csvfilewriter.append(results.id+",")

     // csvfilewriter.append(s"$line")



       results.featureValues.foreach{
         case(featureKey:Int, featureValue:Tuple2[Double,Int])=>
           csvfilewriter.append(featureKey+","+featureValue._1+","+featureValue._2+",")
       }
      csvfilewriter.append("\r\n")
    })
    csvfilewriter.close()
    findClustersAffiliation(clusterResults)
    findClustersMembers()

  }

  /**
    * For each feature finds quartiles based on the maximum identified value
    */
  def evaluateFeaturesQuartiles() {
    for (i <- 0 to numFeatures - 1) {
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
    for (i <- 0 to numFeatures - 1) {
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
          /*  else if (matchingResults.size == 1) {
              //case we have only one remaining cluster to check
              matchingResults(0)
              matchedElements.put(clusterName, matchingResults(0)._1)
              matchedIds += matchingResults(0)._1.id
            }
           */

          }
      }
      }
    outputResults("***********************************************************************")
    outputResults("CLUSTERS ASSOCIATIONS")
    matchedElements.foreach{
      case(a,b)=>{
        val line:String="cluster:" +b.id+" matches: "+a+" cluster matching:"+b.sortedMatchingList
        outputResults(line)
    }
    }
  }
  def findClustersMembers(): Unit ={
    val reader = new SequenceFile.Reader(fs, new Path(outputDir + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-0"), conf)
    val key = new org.apache.hadoop.io.IntWritable()
    val value = new WeightedPropertyVectorWritable()

    //if(reader==null){println("reader is null")}
    val finalClusters = new ListBuffer[String]
    while (reader.next(key, value)) {
      val namedVector:NamedVector=value.getVector().asInstanceOf[NamedVector]
      println("USERID:"+namedVector.getName+" belongs to cluster:"+key)
      if (!finalClusters.contains(key.toString))
        finalClusters += key.toString()
    }
    reader.close()
  }

  def outputResults(line:Any){
    val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("clusters_outputs_"+algorithmType.toString+".txt", true)))

    csvfilewriter.append(s"$line")
    csvfilewriter.append("\r\n")

    csvfilewriter.close()
  }

}
