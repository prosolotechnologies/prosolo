package org.prosolo.bigdata.scala.clustering.userprofiling

import java.math.RoundingMode
import java.util
import java.util.Date

import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner
import com.datastax.driver.core.Row
import org.prosolo.bigdata.clustering.QuartileName
import org.prosolo.bigdata.dal.cassandra.impl.UserObservationsDBManagerImpl
import org.prosolo.bigdata.dal.persistence.impl.ClusteringDAOImpl
import play.api.libs.json.Json


import scala.collection.mutable.{HashMap, Map}

//import org.prosolo.bigdata.scala.statistics.QuartileName
import java.util.List

import be.ac.ulg.montefiore.run.jahmm._
import org.prosolo.bigdata.utils.DateUtil

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by Zoran on 05/12/15.
  */
/**
  * Zoran 05/12/15
  */
class HmmClustering {
 // val dbManager = new UserObservationsDBManagerImpl()

  //val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
  //val startDate: Date = dateFormat.parse("10/20/2014")
 // val endDate: Date = dateFormat.parse("10/22/2014")
  val nStates=4
  val learntHmmModels:Map[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]=new HashMap[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]()
    //testHmmModel()

  def performHmmClusteringForPeriod(startDate: Date, endDate: Date, courseId: Long) = {
    initializeHmmModels(courseId)
    processCourseTestSequencesForPeriod(startDate, endDate, courseId)
  }

  def initializeHmmModels(courseId:Long)={
    ClusterName.values.foreach{clusterName=>
      val initFactory:OpdfDiscreteFactory[QuartileName]  = new OpdfDiscreteFactory[QuartileName](classOf[QuartileName])
      val sequences:List[List[ObservationDiscrete[QuartileName]]]=getClusterCourseSequences(clusterName, courseId)
      val initHmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](nStates,initFactory)
      val mBwl:BaumWelchLearner=new BaumWelchLearner
    if(sequences.size>0){
      val learntHmm:Hmm[ObservationDiscrete[QuartileName]]=mBwl.learn(initHmm, sequences)
      learntHmmModels.put(clusterName,learntHmm)
    }

    }

  }
  def getClusterCourseSequences(clusterName:ClusterName.Value, courseId:Long):List[List[ObservationDiscrete[QuartileName]]]={
    val  results:util.List[Row]=UserObservationsDBManagerImpl.getInstance().findAllUserQuartileFeaturesForCourseAndProfile(courseId, clusterName.toString)
    val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
    results.toList.foreach{
      row=>
        val sequence: java.util.List[ObservationDiscrete[QuartileName]] =row.getString("sequence").split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
        sequences.add(sequence)
    }
    sequences
  }
  def processCourseTestSequencesForPeriod(startDate: Date, endDate: Date, courseId: Long)={
    val startDateSinceEpoch = DateUtil.getDaysSinceEpoch(startDate)
    val endDateSinceEpoch = DateUtil.getDaysSinceEpoch(endDate)
    val clusteringDBManager=new ClusteringDAOImpl

    println("PROCESSING COURSE:"+courseId)
    val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
  //  ClusterName.values.foreach(clusterName=>
  //  {
      val  results:util.List[Row]=UserObservationsDBManagerImpl.getInstance().findAllUserQuartileFeaturesForCourseDate(courseId, endDateSinceEpoch)
      println("FOUND TEST SEQUENCES:"+courseId+" cluster:"+" end date:"+endDateSinceEpoch+" size:"+results.size())
      results.toList.foreach{
        row=>


          val testSequence: java.util.List[ObservationDiscrete[QuartileName]] =row.getString("sequence").split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
          println("TEST-Sequence:"+ testSequence.toString)
          val userid=row.getLong("userid");
          println("*****************************************")
          println("CHECKING ROW WITH TEST SEQUENCE:"+testSequence.toString)
       /*    val learntHmm= learntHmmModels.get(clusterName).get
          val probability:Double=learntHmm.probability(testSequence);
          val lnProbability:Double=learntHmm.lnProbability(testSequence);
          println("user "+userid+" has probability for cluster:"+clusterName.toString+" p:"+probability+" lnP:"+lnProbability)
*/
          var bestCluster=None:Option[ClusterName.Value]
          var bestClusterProbability:Double=0.0

          learntHmmModels.foreach{
           case(clusterName, learntHmm)=>
           {

             val probability:Double=learntHmm.probability(testSequence);
             val probability2:Double=BigDecimal(probability).setScale(3,BigDecimal.RoundingMode.HALF_UP).toDouble
            if(bestClusterProbability < probability){
               bestCluster=Some(clusterName)
               bestClusterProbability=probability


             }
             val lnProbability:Double=learntHmm.lnProbability(testSequence);
             println("---user "+userid+" has probability for cluster:"+clusterName.toString+" p:"+probability+" lnP:"+lnProbability)

           }

         }
          bestCluster match {
            case Some(cluster)=>{

              val clusterTemplate:ClusterTemplate=FeaturesToProfileMatcher.clusterProfiles.get(cluster).get
              println("BEST CLUSTER IDENTIFIED for course:"+courseId+" user:"+userid+" IS:"+cluster+" cluster full name:"+clusterTemplate.clusterFullName)
              clusteringDBManager.updateUserCourseProfile(courseId,userid, bestCluster.get.toString, clusterTemplate.clusterFullName)
              val tSeq=testSequence.zipWithIndex.map{case(sv,i)=>
                val jsonObject=Json.obj("featurename"->clusterTemplate.getFeatureName(i),"value"->clusterTemplate.getFeatureValue(i),"quartile"->sv.toString)
                Json.stringify(jsonObject)}.toList.asJava
              println("T--SEQ:"+tSeq)
              UserObservationsDBManagerImpl.getInstance().updateUserCurrentProfile(courseId,userid,bestCluster.get.toString,tSeq)
            }
            case None=>println("NO BEST CLUSTER FOUND")
          }
      }
   // })
    println("FINISHED PROCESSING COURSE:"+courseId)
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
