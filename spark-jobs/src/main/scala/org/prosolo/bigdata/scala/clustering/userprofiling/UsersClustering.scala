package org.prosolo.bigdata.scala.clustering.userprofiling

import java.io.{BufferedWriter, FileWriter, PrintWriter}

import org.apache.mahout.clustering.Cluster
import org.apache.mahout.clustering.canopy.CanopyDriver
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver
import org.apache.mahout.clustering.iterator.ClusterWritable
import org.apache.mahout.clustering.kmeans.{KMeansDriver, RandomSeedGenerator}
import org.apache.mahout.common.HadoopUtil
import org.apache.mahout.common.distance.{CosineDistanceMeasure, EuclideanDistanceMeasure}
import org.apache.mahout.math.{DenseVector, NamedVector, VectorWritable}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{SequenceFile, Text}
import org.joda.time.DateTime
import org.prosolo.bigdata.dal.cassandra.impl.{ProfilesDAO, TablesNames}
import org.prosolo.bigdata.scala.spark.SparkJob
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles
import org.prosolo.bigdata.utils.DateUtil

import scala.collection.mutable.Iterable
import scala.IndexedSeq
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable._
import com.datastax.spark.connector._
import com.datastax.driver.core.Row
import org.slf4j.LoggerFactory



/**
  * Created by Zoran on 22/11/15.
  */
/**
  * Zoran 22/11/15
  */
@deprecated
class UsersClustering (val sparkJob:SparkJob, val dbName:String, val numClusters:Int, val numFeatures:Int)  {
  val logger = LoggerFactory.getLogger(getClass)
  //val sc=SparkContextLoader.getSC
  val featuresQuartiles: mutable.Map[Int, FeatureQuartiles] = new HashMap[Int, FeatureQuartiles]
  val matchedClusterProfiles: Map[Long, ClusterName.Value] = new HashMap[Long, ClusterName.Value]()
  val profilesDAO=new ProfilesDAO(dbName)


  def getMatchedClusterProfile(clusterId:Long):String={
    matchedClusterProfiles.getOrElse(clusterId,"").toString
  }
  /*case class CourseClusterConfiguration(courseId: Long,
                                        clustersDir:String,
                                        vectorsDir:String,
                                        outputDir:String,
                                        output:Path,
                                        datapath:Path)

*/
  /**
    * Main function that executes clustering
    *
    * @param startDate
    * @param endDate
    */
  //def performKMeansClusteringForPeriod(days:IndexedSeq[DateTime], courseId: Long):Iterable[Tuple5[Long,String,Long,Long,String]] = {
    def performKMeansClusteringForPeriod(days:IndexedSeq[DateTime], courseId: Long) = {
    logger.debug("perform kmeans clustering for:"+courseId)
    val clustersDir = "clustersdir/"+courseId
    val vectorsDir = clustersDir + "/users"
    val outputDir: String = clustersDir + "/output"
    val output = new Path(outputDir)
    val datapath = new Path(vectorsDir + "/part-00000")

    //val courseClusterConfiguration:Tuple6[Long, String, String, String, Path, Path]=new Tuple6(courseId, clustersDir,vectorsDir, outputDir, output, datapath)
   // val courseClusterConfiguration:CourseClusterConfiguration=new CourseClusterConfiguration(courseId, clustersDir,vectorsDir, outputDir, output, datapath)
    val courseClusterConfiguration=""
    val daysSinceEpoch:IndexedSeq[Long]=days.map{
      day=>
        DateUtil.getDaysSinceEpoch(day)
    }
    val usersFeatures: Predef.Map[Long, Array[Double]] = daysSinceEpoch
      .flatMap{date=>mapUserObservationsForDateToRows(date,courseId)}
      .groupBy { row: Row => row.getLong(1) }
      .transform((userid, userRows) => transformUserFeaturesForPeriod(userid, userRows))
    extractFeatureQuartilesValues(usersFeatures)
    evaluateFeaturesQuartiles()
    val usersQuartilesFeatures: Predef.Map[Long, Array[Double]] =  usersFeatures.transform((userid, userFeatures)=>transformUserFeaturesToFeatureQuartiles(userid, userFeatures))
    prepareSequenceFile(usersQuartilesFeatures, datapath)
  /*  if(runClustering(courseClusterConfiguration)){
      val usercourseprofiles:Iterable[Tuple5[Long,String,Long,Long,String]] =readAndProcessClusters(usersQuartilesFeatures, daysSinceEpoch,  courseId,output, outputDir)
      usercourseprofiles
    }else {
      val usercourseprofiles:Iterable[Tuple5[Long,String,Long,Long,String]] =new  mutable.ListBuffer[Tuple5[Long,String,Long,Long,String]]()
      usercourseprofiles
    }*/
  }

  //////////////////////////////////////////
  //Functions to prepare data for clustering
  //////////////////////////////////////////
  /**
    * We are retrieving data about user activities for individual dates
    *
    * @param date
    * @return
    */
  def mapUserObservationsForDateToRows(date: Long, courseId:Long):List[Row] = {
    /* val rows=sc.cassandraTable(dbName, TablesNames.PROFILE_USERPROFILE_ACTIONS_OBSERVATIONS_BYDATE)
      .select("date","userid","attach","progress","comment","creating","evaluation","join","like","login" ,"posting","content_access","message","search")
      .where("date="+date+" and course="+courseId)
    logger.debug("kmeans-2.1:"+rows.collect().size)
     rows.collect().toList*/

    val rows=profilesDAO.findUserProfileObservationsByDate(date,courseId)
    rows

  }

  /**
    * For each user feature value finds appropriate quartile
    *
    * @param userid
    * @param userFeatures
    * @return
    */
  def transformUserFeaturesToFeatureQuartiles(userid:Long, userFeatures: Array[Double]): Array[Double]={
    val quartilesFeaturesArray: Array[Double] = new Array[Double](numFeatures)
    for(i<-0 to numFeatures - 1){
      val quartile:FeatureQuartiles=featuresQuartiles.getOrElseUpdate(i,new FeatureQuartiles())
      quartilesFeaturesArray(i)=quartile.getQuartileForFeatureValue(userFeatures(i))
    }
    quartilesFeaturesArray
  }
  /**
    * We are tranforming data for different dates for one user and collect it in single Array of features
    *
    * @param userid
    * @param userRows
    * @return
    */
  def transformUserFeaturesForPeriod(userid: Long, userRows: IndexedSeq[Row]) = {
    logger.debug("TRANSFORM USER FEATURES:"+userid)
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
    *
    * @param usersFeatures
    */
  def extractFeatureQuartilesValues(usersFeatures: collection.Map[Long, Array[Double]]): Unit = {
    logger.debug("EXTRACT FEATURE:"+usersFeatures.size)
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
    *
    * @param usersFeatures
    */
  def prepareSequenceFile(usersFeatures: collection.Map[Long, Array[Double]], datapath:Path): Unit = {
    val vectors = new ListBuffer[NamedVector]
    usersFeatures.foreach {
      case (userid: Long, featuresArray: Array[Double]) =>
        logger.debug("ADDING VECTOR FOR USER:"+userid+" features:"+featuresArray.mkString(","))
        val dv = new DenseVector(featuresArray)
        vectors += (new NamedVector(dv, userid.toString()))
    }
    val valClass=if(ClusteringUtils.algorithmType==AlgorithmType.Canopy) classOf[ClusterWritable] else classOf[VectorWritable]
    val writer = new SequenceFile.Writer(ClusteringUtils.fs, ClusteringUtils.conf, datapath, classOf[Text],valClass)
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

/*  def runClustering(courseClusterConfiguration:CourseClusterConfiguration):Boolean= {

    HadoopUtil.delete(ClusteringUtils.conf, courseClusterConfiguration.output)
    val measure = new CosineDistanceMeasure()
    val clustersIn = new Path(courseClusterConfiguration.output, "random-seeds")
    var success=true;
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
          sparkJob.submitTaskProblem(ise.getMessage,courseClusterConfiguration.courseId,"KMeansClustering",ProblemSeverity.MAJOR)
          success=false;
      }

    } else if (ClusteringUtils.algorithmType == AlgorithmType.Canopy) {
      CanopyDriver.run(ClusteringUtils.conf, clustersIn, courseClusterConfiguration.output, new EuclideanDistanceMeasure(), 20, 5, false, clusterClassificationThreshold, false)
    } else if (ClusteringUtils.algorithmType == AlgorithmType.FuzzyKMeans) {
      FuzzyKMeansDriver.run(ClusteringUtils.conf, courseClusterConfiguration.datapath, clustersIn, courseClusterConfiguration.output, convergenceDelta, maxIterations, m, true, true, clusterClassificationThreshold, false)
    }

    // CanopyDriver.run(conf, datapath, clustersIn, output, convergenceDelta, maxIterations, true, 0.0, true)
    success
  }*/

  /**
    * W
    */
  def readAndProcessClusters(usersQuartilesFeatures: Predef.Map[Long, Array[Double]],daysSinceEpoch:IndexedSeq[Long], courseId:Long, output:Path, outputDir:String) : Iterable[Tuple5[Long,String,Long,Long,String]]={

    val clusters=ClusterHelper.readClusters(ClusteringUtils.conf, output)

    val clusterResults: Buffer[ClusterResults] = evaluateClustersFeaturesQuartiles(clusters)
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
    findClustersAffiliation(clusterResults)
    val usersQuartilesFeaturesAndClusters: mutable.HashMap[Long, (Int, Array[Double])]=findClustersMembers(usersQuartilesFeatures,outputDir)
    val usercourseprofiles:Iterable[Tuple5[Long,String,Long,Long,String]]=storeUserQuartilesFeaturesToClusterFiles(usersQuartilesFeaturesAndClusters,daysSinceEpoch, courseId)

    usercourseprofiles

  }

  def storeUserQuartilesFeaturesToClusterFiles(usersQuartilesFeaturesAndClusters: mutable.HashMap[Long, (Int, Array[Double])],
                                               days:IndexedSeq[Long], courseId:Long):Iterable[(Long, String, Long, Long, String)] ={
    // val csvfilewriter = new PrintWriter(new BufferedWriter(new FileWriter("features_outputs_"+algorithmType.toString+".csv", true)))
    val clusterswriters:HashMap[Int,PrintWriter]=new HashMap[Int,PrintWriter]()
    //val dates=startDateSinceEpoch.toString+"_"+endDateSinceEpoch.toString
    val dates=days(0)+"_"+days(days.length-1)
    val endDateSinceEpoch=days(days.length-1)
    val usercourseSequences:Iterable[(Long, String, Long, Long, String)]=usersQuartilesFeaturesAndClusters.map{
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
    *
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
    *
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
    *
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
          tempList +=Tuple2(clusterResult,matchElem._2)
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
  def findClustersMembers(usersQuartilesFeatures: Predef.Map[Long, Array[Double]], outputDir:String): mutable.HashMap[Long, (Int, Array[Double])] ={

    val reader = new SequenceFile.Reader(ClusteringUtils.fs, new Path(outputDir + "/" + Cluster.CLUSTERED_POINTS_DIR + "/part-m-0"), ClusteringUtils.conf)
    val key = new org.apache.hadoop.io.IntWritable()
    val value = new WeightedPropertyVectorWritable()
    val usersQuartilesFeaturesAndClusters: mutable.HashMap[Long, (Int, Array[Double])]=new HashMap[Long,(Int, Array[Double])]()
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