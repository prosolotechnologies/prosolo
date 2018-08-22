package org.prosolo.bigdata.spark.scala.clustering

import com.datastax.driver.core.Row
import org.apache.hadoop.io.{SequenceFile, Text}
import org.apache.mahout.clustering.iterator.ClusterWritable
import org.apache.mahout.math.{DenseVector, NamedVector, VectorWritable}
import org.prosolo.bigdata.dal.cassandra.impl.ProfilesDAO
import org.prosolo.bigdata.scala.clustering.userprofiling.{AlgorithmType, ClusteringUtils}
import org.prosolo.bigdata.scala.statistics.FeatureQuartiles

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import org.apache.hadoop.fs.Path


object KMeansClusteringUtility extends Serializable {
  def performKMeansClusteringForPeriod(
      daysSinceEpoch: IndexedSeq[Long],
      courseId: Long,
      keyspaceName: String,
      numFeatures: Int,
      numClusters: Int): Iterable[(Long, String, Long, Long, String)] = {
    val featuresQuartiles: scala.collection.mutable.Map[Int, FeatureQuartiles] = new mutable.HashMap[Int, FeatureQuartiles]()

    val profilesDAO = new ProfilesDAO(keyspaceName)
    //   logger.debug("perform kmeans clustering for:" + courseId)
    val clustersDir = "clustersdir/" + courseId
    val vectorsDir = clustersDir + "/users"
    val outputDir: String = clustersDir + "/output"
    val output = new Path(outputDir)
    val datapath = new Path(vectorsDir + "/part-00000")
    val courseClusterConfiguration: CourseClusterConfiguration = new CourseClusterConfiguration(courseId, clustersDir, vectorsDir, outputDir, output, datapath)


    val usersFeatures: Predef.Map[Long, Array[Double]] = daysSinceEpoch
        .flatMap { date => mapUserObservationsForDateToRows(date, courseId, profilesDAO) }
        .groupBy { row: Row => row.getLong(1) }
        .transform((userid, userRows) => transformUserFeaturesForPeriod(userid, userRows, numFeatures))
    extractFeatureQuartilesValues(usersFeatures, featuresQuartiles)
    evaluateFeaturesQuartiles(numFeatures, featuresQuartiles)
    val usersQuartilesFeatures: Predef.Map[Long, Array[Double]] = usersFeatures.transform(
      (userid, userFeatures) => transformUserFeaturesToFeatureQuartiles(
        userid,
        userFeatures,
        numFeatures,
        featuresQuartiles
      )
    )
    prepareSequenceFile(usersQuartilesFeatures, datapath)
    if (ClusteringUtilityFunctions.runClustering(courseClusterConfiguration, numClusters)) {

      val usercourseprofiles: mutable.Iterable[(Long, String, Long, Long, String)] = ClusteringUtilityFunctions.readAndProcessClusters(
        usersQuartilesFeatures,
        daysSinceEpoch,
        courseId,
        output,
        outputDir,
        numFeatures,
        numClusters
      )
      usercourseprofiles
    } else {
      val usercourseprofiles: mutable.Iterable[(Long, String, Long, Long, String)] = new scala.collection.mutable.ListBuffer[(Long, String, Long, Long, String)]()
      usercourseprofiles

    }
  }

  /**
    * We are axtracting values for individual features in order to resolve quartiles later
    *
    * @param usersFeatures
    */
  private def extractFeatureQuartilesValues(usersFeatures: collection.Map[Long, Array[Double]], featuresQuartiles: scala.collection.mutable.Map[Int, FeatureQuartiles]): Unit = {
    //logger.debug("EXTRACT FEATURE:" + usersFeatures.size)
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
  private def evaluateFeaturesQuartiles(numFeatures: Int, featuresQuartiles: scala.collection.mutable.Map[Int, FeatureQuartiles]) {
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
  def transformUserFeaturesToFeatureQuartiles(
      userid: Long,
      userFeatures: Array[Double],
      numFeatures: Int,
      featuresQuartiles: scala.collection.mutable.Map[Int, FeatureQuartiles]): Array[Double] = {
    val quartilesFeaturesArray: Array[Double] = new Array[Double](numFeatures)
    for (i <- 0 to numFeatures - 1) {
      val quartile: FeatureQuartiles = featuresQuartiles.getOrElseUpdate(i, new FeatureQuartiles())
      quartilesFeaturesArray(i) = quartile.getQuartileForFeatureValue(userFeatures(i))
    }
    quartilesFeaturesArray
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
    //   logger.debug("TRANSFORM USER FEATURES:" + userid)
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
    * We are writing user features vectors to the Sequence files
    *
    * @param usersFeatures
    */
  def prepareSequenceFile(usersFeatures: collection.Map[Long, Array[Double]], datapath: Path): Unit = {
    val vectors = new ListBuffer[NamedVector]
    usersFeatures.foreach {
      case (userid: Long, featuresArray: Array[Double]) =>
        //   logger.debug("ADDING VECTOR FOR USER:" + userid + " features:" + featuresArray.mkString(","))
        val dv = new DenseVector(featuresArray)
        vectors += (new NamedVector(dv, userid.toString()))
    }
    val valClass = if (ClusteringUtils.algorithmType == AlgorithmType.Canopy) classOf[ClusterWritable] else classOf[VectorWritable]
    val writer = new SequenceFile.Writer(ClusteringUtils.fs, ClusteringUtils.conf, datapath, classOf[Text], valClass)
    val vec = new VectorWritable()
    vectors.foreach { vector =>
      vec.set(vector)
      writer.append(new Text(vector.getName), vec)
    }
    writer.close()
  }

}
