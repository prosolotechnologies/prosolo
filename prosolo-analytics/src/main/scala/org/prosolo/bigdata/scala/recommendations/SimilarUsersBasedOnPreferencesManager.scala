package org.prosolo.bigdata.scala.recommendations

import org.prosolo.bigdata.config.Settings
import org.prosolo.bigdata.dal.cassandra.impl.{CassandraDDLManagerImpl, UserObservationsDBManagerImpl}
import org.prosolo.bigdata.jobs.SimilarUsersBasedOnPreferencesJob

/**
  * Created by zoran on 29/04/17.
  */
object SimilarUsersBasedOnPreferencesManager extends App{
  println("FIND SIMILAR USERS BASED ON PREFERENCES")
  val jobProperties=Settings.getInstance().config.schedulerConfig.jobs.getJobConfig(classOf[SimilarUsersBasedOnPreferencesJob].getName)
  val clusterAproxSize:Int=jobProperties.jobProperties.getProperty("clusterAproximateSize").toInt;
  val maxIt1=jobProperties.jobProperties.getProperty("possibleKmeansMaxIteration1").toInt;
  val maxIt2=jobProperties.jobProperties.getProperty("possibleKmeansMaxIteration2").toInt;
  val possibleMaxIterations=Seq(maxIt1, maxIt2)
  //val keyspaceName=CassandraDDLManagerImpl.getInstance().getSchemaName
  val keyspaceName="prosolo_logs_uta"
  println("USING KEYSPACE:"+keyspaceName)
  runJob()
  def runJob() ={
    val totalNumberOfUsers:Long=UserObservationsDBManagerImpl.getInstance().findTotalNumberOfUsers()
    println("TOTAL NUMBER OF USERS:"+totalNumberOfUsers)

    if (totalNumberOfUsers>clusterAproxSize*1.5) SimilarUsersBasedOnPreferencesJob.runKmeans(totalNumberOfUsers,keyspaceName,possibleMaxIterations,clusterAproxSize)
    else SimilarUsersBasedOnPreferencesJob.createOneCluster(keyspaceName)

    SimilarUsersBasedOnPreferencesJob.runALSUserRecommender(clusterAproxSize,keyspaceName)
    println("SIMILAR USERS BASED ON PREFERENCES runJob finished")
  }

}
