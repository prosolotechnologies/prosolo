package org.prosolo.bigdata.scala.recommendations

import org.prosolo.bigdata.common.enums.ESIndexTypes
import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{CassandraDDLManagerImpl, UserObservationsDBManagerImpl}
import org.prosolo.bigdata.jobs.SimilarUsersBasedOnPreferencesJob
import org.slf4j.LoggerFactory
import org.prosolo.common.ESIndexNames

/**
  * Created by zoran on 29/04/17.
  */
object SimilarUsersBasedOnPreferencesManager {
  val logger = LoggerFactory.getLogger(getClass)
  logger.debug("FIND SIMILAR USERS BASED ON PREFERENCES")
  val jobProperties = Settings.getInstance().config.schedulerConfig.jobs.getJobConfig(classOf[SimilarUsersBasedOnPreferencesJob].getName)
  //val clusterAproxSize:Int=jobProperties.jobProperties.getProperty("clusterAproximateSize").toInt;
  val clusterAproxSize: Int = 5
  logger.debug("TEMPORARY CLUSTER SIZE:" + clusterAproxSize)
  val maxIt1 = jobProperties.jobProperties.getProperty("possibleKmeansMaxIteration1").toInt;
  val maxIt2 = jobProperties.jobProperties.getProperty("possibleKmeansMaxIteration2").toInt;
  val possibleMaxIterations = Seq(maxIt1, maxIt2)
  val keyspaceName = CassandraDDLManagerImpl.getInstance().getSchemaName
  logger.debug("USING KEYSPACE:" + keyspaceName)
  runJob()

  def runJob() = {
    val totalNumberOfUsers: Long = UserObservationsDBManagerImpl.getInstance().findTotalNumberOfUsers()
    logger.debug("TOTAL NUMBER OF USERS:" + totalNumberOfUsers)
    val similarUsersBasedOnPreferencesSparkJob = new SimilarUsersBasedOnPreferencesSparkJob(keyspaceName)
    if (totalNumberOfUsers > clusterAproxSize * 1.5) similarUsersBasedOnPreferencesSparkJob.runKmeans(totalNumberOfUsers, keyspaceName, possibleMaxIterations, clusterAproxSize)
    else similarUsersBasedOnPreferencesSparkJob.createOneCluster(keyspaceName)

    similarUsersBasedOnPreferencesSparkJob.runALSUserRecommender(clusterAproxSize, keyspaceName, ESIndexNames.INDEX_RECOMMENDATION_DATA, ESIndexTypes.SIMILAR_USERS)
    logger.debug("SIMILAR USERS BASED ON PREFERENCES runJob finished")
    similarUsersBasedOnPreferencesSparkJob.finishJob()
  }

}
