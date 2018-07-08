package org.prosolo.bigdata.scala.clustering

import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl
import org.prosolo.bigdata.scala.clustering.userprofiling.{ClusterHelper, ClusterName, ClusterResults, FeaturesToProfileMatcher}
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles
import com.datastax.driver.core.Row
import org.apache.mahout.math.{DenseVector, NamedVector, VectorWritable}
import org.apache.hadoop.io.{SequenceFile, Text}
import org.apache.mahout.common.HadoopUtil
import org.apache.mahout.common.distance.CosineDistanceMeasure
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.apache.mahout.clustering.kmeans.{KMeansDriver, Kluster, RandomSeedGenerator}
import org.apache.mahout.clustering.Cluster
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.SequenceFile
import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
import java.util.{Calendar, Date}
import java.text.SimpleDateFormat

import org.prosolo.common.util.date.DateEpochUtil
import org.slf4j.LoggerFactory

import scala.collection.mutable.{ArrayBuffer, Buffer, HashMap, ListBuffer, Map}
import scala.collection.JavaConverters._

/**
 * @author zoran October 24, 2015
 */
@deprecated
object UsersKMeansClustering {
  val logger = LoggerFactory.getLogger(getClass)
  val clustersDir = "clustersdir"
  val vectorsDir = clustersDir + "/users"
  val outputDir: String = clustersDir + "/output"

  val conf = new Configuration()
  val fs = FileSystem.get(conf)
  val datapath = new Path(vectorsDir + "/part-00000")
  val numClusters = 4
  val numFeatures = 3
  val prepareTrainingSetData = true

  val maxFeaturesValues: Map[Int, Double] = new HashMap[Int, Double]()
  //map feature id to FeatureQuartile
  val featuresQuartiles: Map[Int, FeatureQuartiles] = new HashMap[Int, FeatureQuartiles]

  def initData(date: Long) = {
    for (i <- 1 to numFeatures) {
      maxFeaturesValues.put(i, 0)
    }
    val vectors = new ListBuffer[NamedVector]

    val rows: java.util.List[Row] = UserObservationsDBManagerImpl.getInstance().findAllUsersObservationsForDate(date)

    rows.asScala.foreach { row =>
      //creating dense vector for each user observation
      val userid = row.getLong(1)
      val featuresArray: Array[Double] = new Array[Double](numFeatures)

      for (i <- 0 to numFeatures - 1) {
        val featureValue = row.getLong(i + 2).toDouble
        featuresArray(i) = featureValue
        featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).addValueToSet(featureValue)
      }

      val dv = new DenseVector(featuresArray)
      //creating named vector by user id
      vectors += (new NamedVector(dv, userid.toString()))
    }
    //storing input vectors to hdfs

    val writer = new SequenceFile.Writer(fs, conf, datapath, classOf[Text], classOf[VectorWritable])
    val vec = new VectorWritable()
    vectors.foreach { vector =>
      vec.set(vector)
      writer.append(new Text(vector.getName), vec)
    }
    writer.close()
    //reading for test only. should remove this
    val reader = new SequenceFile.Reader(fs, datapath, conf)

    val key = new Text()
    val value = new VectorWritable()
    while (reader.next(key, value)) {
      //  logger.debug("INPUT:" + s"$key--- ${value.get().asFormatString()}")
    }
    reader.close()
    logger.debug("Finished data initialization")
  }
  def runClustering(date: Long) = {
    val output = new Path(outputDir)
    HadoopUtil.delete(conf, output)
    val measure = new CosineDistanceMeasure()
    val clustersIn = new Path(output, "random-seeds")
    RandomSeedGenerator.buildRandom(conf, datapath, clustersIn, numClusters, measure)
    val convergenceDelta = 0.01
    val maxIterations = 50
    KMeansDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, 0.0, true)

    val clusters = ClusterHelper.readClusters(conf, output)
    evaluateFeaturesQuartiles()
    val clusterResults: Buffer[ClusterResults] = evaluateClustersFeaturesQuartiles(clusters, date)
    evaluateClustersResultsForProfiles(clusterResults)
    findClustersAffiliation(clusterResults)
    val reader = new SequenceFile.Reader(fs, new Path(outputDir + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-0"), conf)
    val key = new org.apache.hadoop.io.IntWritable()
    val value = new WeightedPropertyVectorWritable()

    //if(reader==null){logger.debug("reader is null")}
    val finalClusters = new ListBuffer[String]
    while (reader.next(key, value)) {
       logger.debug("KEY:"+value)
       logger.debug(s"$value belongs to cluster $key")
      if (!finalClusters.contains(key.toString))
        finalClusters += key.toString()
    }
    reader.close()
    logger.debug("CLUSTERS:" + finalClusters)

  }
  /**
   * For each feature finds quartiles based on the maximum identified value
   */
  def evaluateFeaturesQuartiles() {
    for (i <- 0 to numFeatures - 1) {
      featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).findQuartiles()
    }
  }
  def extractClusterResultFeatureQuartileForCluster(cluster: Cluster, date: Long): ClusterResults = {
    logger.debug(s"Cluster id:${cluster.getId} center:${cluster.getCenter.asFormatString()}")
    val cid = cluster.getId
    val clusterResult = new ClusterResults(cid)
    for (i <- 0 to numFeatures - 1) {
      val featureVal = cluster.getCenter.get(i)
      val (featureQuartileMean: Array[Double], featureQuartile) = featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).checkQuartileForFeatureValue(featureVal)
      logger.debug("Result for cluster:" + cid + " feature:" + i + " has value:" + featureVal + " Q:" + featureQuartile)
      clusterResult.addFeatureValue(i, (featureVal, featureQuartile))
    }
    clusterResult
  }
  def findClusterResultsMatching(clusterResult: ClusterResults): ClusterResults = {
    logger.debug("Trying to find cluster results matching")
    clusterResult.getFeatureValues().foreach {
      value: (Int, (Double, Int)) =>
        logger.debug("feature:" + value._1, "Quadrile:" + value._2._2)
    }
    clusterResult
  }
  def evaluateClustersFeaturesQuartiles(clusters: java.util.List[java.util.List[Cluster]], date: Long): Buffer[ClusterResults] = {
    logger.debug("EVALUATING DAY::::::" + date);
    val clusterResults: Buffer[ClusterResults] = clusters.get(clusters.size - 1).asScala
      .map { cluster =>
        extractClusterResultFeatureQuartileForCluster(cluster, date)
      }
      .map { clusterResults =>
        findClusterResultsMatching(clusterResults)
      }

    logger.debug("EVALUATED DAY::::::" + date)
    clusterResults
  }
  def evaluateClustersResultsForProfiles(clusterResults: Buffer[ClusterResults]) {
    clusterResults.foreach { clusterResult =>
      FeaturesToProfileMatcher.checkClustersMatching(clusterResult)
      FeaturesToProfileMatcher.sortClustersMatchingByValues(clusterResult)
    }
  }
  def findClustersAffiliation(clusterResults: Buffer[ClusterResults]) {
    logger.debug("FIND CLUSTERS AFFILIATION")
    val matchedElements: Map[ClusterName.Value, ClusterResults] = new HashMap[ClusterName.Value, ClusterResults]()
    val matchedIds:ArrayBuffer[Int]=new ArrayBuffer[Int]()
    for (index <- 0 to numClusters-1) {
      val elementsToCheck: Map[ClusterName.Value, ArrayBuffer[(ClusterResults,Double)]] = new HashMap[ClusterName.Value, ArrayBuffer[(ClusterResults,Double)]]()
      clusterResults.foreach { clusterResult =>
        val matchElem = clusterResult.sortedMatchingList(index)
        logger.debug(index+".MATCH ELEM FOR :"+clusterResult.id+":"+matchElem)
        //checking if cluster was not already resolved
         if(!matchedElements.contains(matchElem._1)){
              val tempList = elementsToCheck.getOrElse(matchElem._1,new ArrayBuffer[(ClusterResults,Double)]())
        tempList +=Tuple2(clusterResult, matchElem._2)
        elementsToCheck.put(matchElem._1, tempList)
         }
    
      }
      logger.debug("ELEMENTS TO CHECK:"+elementsToCheck.map(el=>(el._1,el._2.map(cr=>(cr._1.id,cr._2)))))
      elementsToCheck.foreach{
       
        case (clusterName:ClusterName.Value, matchingResults:ArrayBuffer[(ClusterResults,Double)])=> 
          logger.debug("************MATCHING CLUSTER:"+clusterName)
           logger.debug("MATCHED IDS:"+matchedIds.toString()+" mathing results:"+matchingResults.map(p=>p._1.id))
        if(!matchedElements.contains(clusterName)){
          if(matchingResults.size==1 && !matchedIds.contains(matchingResults(0)._1.id)){
          logger.debug("FOUND SINGLE RESULT FOR:"+clusterName+" id:"+matchingResults(0)._1.id)
             matchedElements.put(clusterName, matchingResults(0)._1)
             matchedIds+=matchingResults(0)._1.id
       
        }else{
          logger.debug("CONFLICT:"+clusterName+" has:"+matchingResults.size+" Searching for best match")
         val sorted:ArrayBuffer[(ClusterResults,Double)]= matchingResults.sortBy(_._2).reverse 
         
         logger.debug("SORTED MATCHES:"+sorted.map(v=>(v._2,v._1.id)))
          val notAssignedSorted=sorted.filterNot{p:(ClusterResults,Double) => matchedIds.contains(p._1.id)}
           logger.debug("NOT ASSIGNED AND SORTED MATCHES:"+notAssignedSorted.map(v=>v._2))
          val highest=notAssignedSorted.head._2
          val filtered=notAssignedSorted.filter{_._2==highest}
          if(filtered.size==1){
            logger.debug("ONLY ONE MATCH:"+filtered(0)._1.id)
             matchedElements.put(clusterName, filtered(0)._1)
             matchedIds+=filtered(0)._1.id
          }else{
            logger.debug("SELECTING FIRST MATCH:"+filtered(0)._1.id)
            matchedElements.put(clusterName, filtered(0)._1)
             matchedIds+=filtered(0)._1.id
          }
        }
        }
      
      }
    }
    val printoutput=matchedElements.map{
      case (el:ClusterName.Value,el2:ClusterResults)=>
        (el,el2.id,el2.featureValues)
        outputResults(el,el2.id)
        outputResults("features:",el2.featureValues)
         outputResults("clusters matching:",el2.clustersMatching)
        
    }
    outputResults("")
    //outputResults(printoutput)
    //logger.debug("******MATCHED ELEMENTS:"+matchedElements.map{case (el:ClusterName.Value,el2:ClusterResults)=>(el,el2.featureValues)})
  }
    def outputResults(line:Any){
      println(line)
      val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("my_test_outputfile.txt", true)))
   
      csvfilewriter.append(s"$line")
       csvfilewriter.append("\r\n")
 
         csvfilewriter.close()
    }
  /**
   *
   */
  @deprecated
  def evaluateAndExportClustersCentroids(clusters: java.util.List[java.util.List[Cluster]], date: Long) = {

    val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("my_test_file3.csv", true)))
    clusters.get(clusters.size - 1).asScala.foreach { cluster =>
      logger.debug(s"Cluster id:${cluster.getId} center:${cluster.getCenter.asFormatString()}")
      val cid = cluster.getId
      csvfilewriter.append(s"$cid,")
      csvfilewriter.append(s"$date,")
      for (i <- 0 to numFeatures - 1) {

        val featureVal = cluster.getCenter.get(i)
        val (featureQuartileMean: Array[Double], featureQuartile) = featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).checkQuartileForFeatureValue(featureVal)

        for (i <- 0 to featureQuartileMean.length - 1) {
          val median = featureQuartileMean(i)
          csvfilewriter.append(s"$median,")
        }
        csvfilewriter.append(s"$featureVal,")
        csvfilewriter.append(s"$featureQuartile,")
        logger.debug("cluster:" + cid + " feature:" + i + " has value:" + featureVal + " Q:" + featureQuartile)

      }
      csvfilewriter.append("\r\n")

    }
    csvfilewriter.close()
  }


  def calculateUsersClasteringForPeriod(startDate: Date, endDate: Date) = {

    val startDateSinceEpoch = DateEpochUtil.getDaysSinceEpoch(startDate)
    logger.debug("DAYS SINCE EPOCH FOR:" + startDate + " is:" + startDateSinceEpoch)
    val endDateSinceEpoch = DateEpochUtil.getDaysSinceEpoch(endDate)
    for (date <- startDateSinceEpoch to endDateSinceEpoch) {
      calculateUsersClasteringForDate(date)
    }

  }
  def calculateUsersClasteringForDate(date: Long) = {
    logger.debug("CLUSTERING FOR:" + date)
    initData(date)
    runClustering(date)
  }
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

  val startDate: Date = dateFormat.parse("10/20/2014")
  val endDate: Date = dateFormat.parse("10/22/2014")
  val endDate2: Date = dateFormat.parse("12/20/2014")
  // calculateUsersClasteringForDate(16370)
  calculateUsersClasteringForPeriod(startDate, endDate2)
}