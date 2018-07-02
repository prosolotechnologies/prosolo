package org.prosolo.bigdata.spark.scala.clustering

import com.datastax.driver.core.Row
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{SequenceFile, Text}
import org.apache.mahout.clustering.iterator.ClusterWritable
import org.apache.mahout.math.{DenseVector, NamedVector, VectorWritable}
import org.joda.time.DateTime
import org.prosolo.bigdata.dal.cassandra.impl.ProfilesDAO
import org.prosolo.bigdata.scala.clustering.userprofiling.{AlgorithmType, ClusteringUtils}
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles
import org.prosolo.bigdata.utils.DateUtil
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.{HashMap, ListBuffer}

/**
  *
  * @author Zoran Jeremic
  * @date 2017-09-14
  * @since 1.0.0
  */
object KMeansClusteringUtility extends Serializable {
  val logger = LoggerFactory.getLogger(getClass)
  var featuresQuartiles: scala.collection.mutable.Map[Int, FeatureQuartiles] = new HashMap[Int, FeatureQuartiles]

  def performKMeansClusteringForPeriod( days:IndexedSeq[DateTime], courseId: Long,keyspaceName:String,numFeatures: Int, numClusters: Int):mutable.Iterable[Tuple5[Long,String,Long,Long,String]] = {
  //def performKMeansClusteringForPeriod( days: IndexedSeq[DateTime], courseId: Long, keyspaceName: String, numFeatures: Int, numClusters: Int): Unit = {
    featuresQuartiles = new HashMap[Int, FeatureQuartiles]()

    val profilesDAO = new ProfilesDAO(keyspaceName)
    logger.debug("perform kmeans clustering for:" + courseId)
    val clustersDir = "clustersdir/" + courseId
    val vectorsDir = clustersDir + "/users"
    val outputDir: String = clustersDir + "/output"
    val output = new Path(outputDir)
    val datapath = new Path(vectorsDir + "/part-00000")

    //val courseClusterConfiguration:Tuple6[Long, String, String, String, Path, Path]=new Tuple6(courseId, clustersDir,vectorsDir, outputDir, output, datapath)
    val courseClusterConfiguration: CourseClusterConfiguration = new CourseClusterConfiguration(courseId, clustersDir, vectorsDir, outputDir, output, datapath)
    val daysSinceEpoch: IndexedSeq[Long] = days.map {
      day =>
        DateUtil.getDaysSinceEpoch(day)
    }

    //logger.debug("COMMENTED HERE.1")
    val usersFeatures: Predef.Map[Long, Array[Double]] = daysSinceEpoch
      .flatMap { date => mapUserObservationsForDateToRows(date, courseId, profilesDAO) }
      .groupBy { row: Row => row.getLong(1) }
      .transform((userid, userRows) => transformUserFeaturesForPeriod(userid, userRows, numFeatures))
    extractFeatureQuartilesValues(usersFeatures)
       evaluateFeaturesQuartiles(numFeatures)
           val usersQuartilesFeatures: Predef.Map[Long, Array[Double]] =  usersFeatures.transform((userid, userFeatures)=>transformUserFeaturesToFeatureQuartiles(userid, userFeatures,numFeatures))
        prepareSequenceFile(usersQuartilesFeatures, datapath)
        if(ClusteringUtilityFunctions.runClustering( courseClusterConfiguration,numClusters)){

         val usercourseprofiles:mutable.Iterable[Tuple5[Long,String,Long,Long,String]] =ClusteringUtilityFunctions.readAndProcessClusters(usersQuartilesFeatures, daysSinceEpoch,  courseId,output, outputDir,numFeatures,numClusters)
         usercourseprofiles
       }else {
         val usercourseprofiles:mutable.Iterable[Tuple5[Long,String,Long,Long,String]] =new  scala.collection.mutable.ListBuffer[Tuple5[Long,String,Long,Long,String]]()
         usercourseprofiles

       }
    // ""
  }

  def mapUserObservationsForDateToRows(date: Long, courseId: Long, profilesDAO: ProfilesDAO): List[Row] = {
    val rows = profilesDAO.findUserProfileObservationsByDate(date, courseId)
    rows
  }

  /**
    * We are tranforming data for different dates for one user and collect it in single Array of features
    *
    * @param userid
    * @param userRows
    * @return
    */
  def transformUserFeaturesForPeriod(userid: Long, userRows: IndexedSeq[Row], numFeatures: Int) = {
    logger.debug("TRANSFORM USER FEATURES:" + userid)
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
    logger.debug("EXTRACT FEATURE:" + usersFeatures.size)
    usersFeatures.foreach {
      case (userid: Long, userFeatures: Array[Double]) =>
        for (i <- 0 to (userFeatures.length - 1)) {
          val featureValue = userFeatures(i)
          featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).addValueToSet(featureValue)
        }
    }

  }
  /**
    * For each feature finds quartiles based on the maximum identified value
    */
  def evaluateFeaturesQuartiles(numFeatures:Int) {
    for (i <- 0 to numFeatures - 1) {
      featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles()).findQuartiles()
    }
  }
  /**
    * For each user feature value finds appropriate quartile
    *
    * @param userid
    * @param userFeatures
    * @return
    */
  def transformUserFeaturesToFeatureQuartiles(userid:Long, userFeatures: Array[Double],numFeatures:Int): Array[Double]={
    val quartilesFeaturesArray: Array[Double] = new Array[Double](numFeatures)
    for(i<-0 to numFeatures - 1){
      val quartile:FeatureQuartiles=featuresQuartiles.getOrElseUpdate(i,new FeatureQuartiles())
      quartilesFeaturesArray(i)=quartile.getQuartileForFeatureValue(userFeatures(i))
    }
    quartilesFeaturesArray
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


}