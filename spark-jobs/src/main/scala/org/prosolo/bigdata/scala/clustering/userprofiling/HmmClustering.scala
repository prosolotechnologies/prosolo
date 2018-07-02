package org.prosolo.bigdata.scala.clustering.userprofiling

import java.util

import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner
import be.ac.ulg.montefiore.run.jahmm.{Hmm, ObservationDiscrete, OpdfDiscreteFactory}
import org.prosolo.bigdata.clustering.QuartileName
import org.prosolo.bigdata.dal.cassandra.impl.ProfilesDAO
import org.prosolo.bigdata.scala.spark.SparkManager
import org.prosolo.bigdata.utils.DateUtil
import play.api.libs.json.Json

import scala.collection.mutable.{HashMap, Map}
import org.joda.time.DateTime
import java.util.List

import com.datastax.driver.core.Row
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by Zoran on 05/12/15.
  */
/**
  * Zoran 05/12/15
  */
class HmmClustering (val dbName:String) extends Serializable {
  val logger = LoggerFactory.getLogger(getClass)
  val nStates=4
  val learntHmmModels:Map[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]=new HashMap[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]()
  val sc=SparkManager.sparkContextLoader.getSC
  val sparkConf=SparkManager.sparkContextLoader.sparkConf
  val profilesDAO=new ProfilesDAO(dbName)


  def performHmmClusteringForPeriod(days:IndexedSeq[DateTime], courseId: Long):Unit = {
    initializeHmmModels(courseId)
    processCourseTestSequencesForPeriod(days, courseId)
  }

  /**
    * For each course we are training HMM model which will be used later to find clusters
    * @param courseId
    */
  def initializeHmmModels(courseId:Long)={
    ClusterName.values.foreach{clusterName=>
      val initFactory:OpdfDiscreteFactory[QuartileName]  = new OpdfDiscreteFactory[QuartileName](classOf[QuartileName])
      val sequences:List[List[ObservationDiscrete[QuartileName]]]=getClusterCourseSequences(clusterName, courseId)
      logger.debug("InitializeHMM model for:"+courseId)
      val initHmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](nStates,initFactory)
      val mBwl:BaumWelchLearner=new BaumWelchLearner
      if(sequences.size>0){
        logger.debug("Learning for:"+courseId)
        val learntHmm:Hmm[ObservationDiscrete[QuartileName]]=mBwl.learn(initHmm, sequences)
        learntHmmModels.put(clusterName,learntHmm)
      }
      logger.debug("INITIALIZED MODEL FOR:"+courseId)
    }
  }

  def getClusterCourseSequences(clusterName:ClusterName.Value, courseId:Long):List[List[ObservationDiscrete[QuartileName]]]={
    logger.debug("GET CLUSTER COURSE SEQUENCES:"+courseId+" clusterName:"+clusterName.toString+" dbName:"+dbName)
    val results:List[Row]=profilesDAO.findUserQuartileFeaturesByProfile(courseId,clusterName)
    val seq:List[List[ObservationDiscrete[QuartileName]]]= results.map{
      row=>
        val sequence: java.util.List[ObservationDiscrete[QuartileName]] =row.getString("sequence").split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
        sequence
    }.toList
    seq
  }

  /**
    * We are finding best cluster for each students by matching learnt models with their observed sequences
    * @param days
    * @param courseId
    */
  def processCourseTestSequencesForPeriod(days:IndexedSeq[DateTime], courseId: Long)={
    val endDateSinceEpoch = DateUtil.getDaysSinceEpoch(days.last)
    logger.debug("PROCESSING COURSE:"+courseId+" date:"+endDateSinceEpoch)
    val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
    val results=profilesDAO.findUserQuartileFeaturesByDate(courseId, endDateSinceEpoch)

    logger.debug("FOUND TEST SEQUENCES:"+courseId+" cluster:"+" end date:"+endDateSinceEpoch+" size:"+results.size)
    val bestClusters=results.map{
      row=>
        val userid=row._2;
        logger.debug("ROW TEST SEQUENCE:"+row._1+" user:"+userid)
        val testSequence: java.util.List[ObservationDiscrete[QuartileName]] =row._1.split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList

        logger.debug("*****************************************")
        logger.debug("CHECKING ROW WITH TEST SEQUENCE:"+testSequence.toString)

        var bestCluster=None:Option[ClusterName.Value]
        var bestClusterProbability:Double=0.0

        learntHmmModels.foreach{
          case(clusterName, learntHmm)=>
          {

            val probability:Double=learntHmm.probability(testSequence);
            val probability2:Double=BigDecimal(probability).setScale(3,BigDecimal.RoundingMode.HALF_UP).toDouble
            logger.debug("PROBABILITY:"+probability+" prob2:"+probability2+" best cluster prob:"+bestClusterProbability)
            if(bestClusterProbability < probability){
              bestCluster=Some(clusterName)
              bestClusterProbability=probability


            }
            val lnProbability:Double=learntHmm.lnProbability(testSequence);
            logger.debug("---user "+userid+" has probability for cluster:"+clusterName.toString+" p:"+probability+" lnP:"+lnProbability)

          }

        }
        bestCluster match {
          case Some(cluster)=>{

            val clusterTemplate:ClusterTemplate=FeaturesToProfileMatcher.clusterProfiles.get(cluster).get
            logger.debug("BEST CLUSTER IDENTIFIED for course:"+courseId+" user:"+userid+" IS:"+cluster+" cluster full name:"+clusterTemplate.clusterFullName)
            logger.debug("THIS IS DISABLED TEMPORARY. WE ARE NOT UPDATING HIBERNATE DB HERE")
            // clusteringDBManager.updateUserCourseProfile(courseId,userid, bestCluster.get.toString, clusterTemplate.clusterFullName)
            val tSeq=testSequence.zipWithIndex.map{case(sv,i)=>
              val jsonObject=Json.obj("featurename"->clusterTemplate.getFeatureName(i),"value"->clusterTemplate.getFeatureValue(i),"quartile"->sv.toString)
              Json.stringify(jsonObject)}.toList.asJava
            logger.debug("T--SEQ:"+tSeq)
            profilesDAO.updateStudentProfileInCourse(bestCluster.get.toString,clusterTemplate.clusterFullName,tSeq, courseId, userid)
            (courseId,userid,bestCluster.get.toString,clusterTemplate.clusterFullName,tSeq)
          }
          case None=>logger.debug("NO BEST CLUSTER FOUND")
            (courseId,userid,"NO_CLUSTER","NO_CLUSTER","")
        }
    }
    logger.debug("FINISHED PROCESSING COURSE:"+courseId)
  }

  def extractSequenceProbabilities(testSequence: java.util.List[ObservationDiscrete[QuartileName]]):Map[ClusterName.Value,Tuple2[Double,Double]]={
    val probabilities:Map[ClusterName.Value,Tuple2[Double,Double]]=new HashMap[ClusterName.Value,Tuple2[Double,Double]]()
    learntHmmModels.foreach{
      case(clusterName,hmmModel)=>{
        val prob:Double=hmmModel.probability(testSequence)
        val lnProb:Double=hmmModel.lnProbability(testSequence)
        probabilities.put(clusterName,(prob,lnProb))
      }
    }
    probabilities
  }
}