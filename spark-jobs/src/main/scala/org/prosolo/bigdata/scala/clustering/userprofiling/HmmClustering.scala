package org.prosolo.bigdata.scala.clustering.userprofiling

import java.util
import java.util.Date

import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner
import be.ac.ulg.montefiore.run.jahmm.{Hmm, ObservationDiscrete, OpdfDiscreteFactory}
import com.datastax.driver.core.Row
import org.prosolo.bigdata.clustering.QuartileName
import org.prosolo.bigdata.dal.cassandra.impl.{DBManager, ProfilesDAO, TablesNames}
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.bigdata.spark.scala.clustering.UserProfileInteractionsSparkJob.sc
import org.prosolo.bigdata.utils.DateUtil
import play.api.libs.json.Json

import scala.collection.mutable.{HashMap, Map}
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.joda.time.DateTime
//import org.prosolo.bigdata.scala.statistics.QuartileName
import java.util.List

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by Zoran on 05/12/15.
  */
/**
  * Zoran 05/12/15
  */
class HmmClustering (val dbName:String) extends Serializable {
 // val dbManager = new UserObservationsDBManagerImpl()

  //val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
  //val startDate: Date = dateFormat.parse("10/20/2014")
 // val endDate: Date = dateFormat.parse("10/22/2014")
  val nStates=4
  val learntHmmModels:Map[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]=new HashMap[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]()
  val sc=SparkContextLoader.getSC
  val sparkConf=SparkContextLoader.sparkConf
  val profilesDAO=new ProfilesDAO(dbName)
 // val dbManager=new DBManager(sparkConf)
  //val connector=CassandraConnector(sparkConf)
    //testHmmModel()

  def performHmmClusteringForPeriod(days:IndexedSeq[DateTime], courseId: Long):Unit = {
    println("PERFORM HMM CLUSTERING FOR PERIOD FOR 1:"+courseId)
    initializeHmmModels(courseId)
    println("PERFORM HMM CLUSTERING FOR PERIOD FOR 2:"+courseId)
    processCourseTestSequencesForPeriod(days, courseId)
    println("PERFORM HMM CLUSTERING FOR PERIOD FOR 3:"+courseId)
  }

  def initializeHmmModels(courseId:Long)={
    println("INITIALIZE HMM MODELS:"+courseId)
    ClusterName.values.foreach{clusterName=>
      println("CLUSTER NAME:"+clusterName)
      val initFactory:OpdfDiscreteFactory[QuartileName]  = new OpdfDiscreteFactory[QuartileName](classOf[QuartileName])
      val sequences:List[List[ObservationDiscrete[QuartileName]]]=getClusterCourseSequences(clusterName, courseId)
      println("KT-1:"+courseId+" sequences:"+sequences.size)
      val initHmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](nStates,initFactory)
      val mBwl:BaumWelchLearner=new BaumWelchLearner
      println("KT-2:"+courseId+" sequences size:"+sequences.size())
    if(sequences.size>0){
      println("KT-3:"+courseId+" clusterName:"+clusterName+" sequences:"+sequences.size)
      val learntHmm:Hmm[ObservationDiscrete[QuartileName]]=mBwl.learn(initHmm, sequences)
      learntHmmModels.put(clusterName,learntHmm)
    }
      println("INITIALIZED MODEL FOR:"+courseId)

    }

  }
  def getClusterCourseSequences(clusterName:ClusterName.Value, courseId:Long):List[List[ObservationDiscrete[QuartileName]]]={
    println("GET CLUSTER COURSE SEQUENCES:"+courseId+" clusterName:"+clusterName.toString+" dbName:"+dbName)
    //val  results:util.List[Row]=UserObservationsDBManagerImpl.getInstance().findAllUserQuartileFeaturesForCourseAndProfile(courseId, clusterName.toString)
    val results=sc.cassandraTable(dbName, TablesNames.PROFILE_USERQUARTILE_FEATURES_BYPROFILE).where("course=?",courseId).where("profile=?",clusterName.toString)
    println("RECORDS:"+results.count())
    results.collect().foreach(x=>println(x.toString()))
   // "SELECT * FROM " + TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE + " WHERE course=? and profile=? ALLOW FILTERING;
   // val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
   //results.foreach{
     val seq:List[List[ObservationDiscrete[QuartileName]]]= results.map{
      row=>
        println("ROW SEQUENCE:"+row.getString("sequence"))
        val sequence: java.util.List[ObservationDiscrete[QuartileName]] =row.getString("sequence").split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
       // sequences.add(sequence)
         sequence
    }.collect().toList
    println("RETURNING SEQUENCES: seq size:"+seq.size())
    //sequences
    seq
  }
  def processCourseTestSequencesForPeriod(days:IndexedSeq[DateTime], courseId: Long)={
    //val startDateSinceEpoch = DateUtil.getDaysSinceEpoch(startDate)

    val endDateSinceEpoch = DateUtil.getDaysSinceEpoch(days.last)
   // val clusteringDBManager=new ClusteringDAOImpl

    println("PROCESSING COURSE:"+courseId+" date:"+endDateSinceEpoch)
    val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
  //  ClusterName.values.foreach(clusterName=>
  //  {
    //  val  results:util.List[Row]=UserObservationsDBManagerImpl.getInstance().findAllUserQuartileFeaturesForCourseDate(courseId, endDateSinceEpoch)
    //val results=sc.cassandraTable(dbName, TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE).where("course=?",courseId).where("date=?",endDateSinceEpoch)
    val results=DBManager.connector.withSessionDo{
      session=>
        val rs=session.execute(s"SELECT * FROM "+dbName+"."+TablesNames.PROFILE_USERQUARTILE_FEATURES_BYDATE+" WHERE course="+courseId+" AND date="+endDateSinceEpoch)
        Option(rs.one()) match {
          case Some(row) if !row.isNull(0) ⇒ {
            println("ROW TEST SEQUENCE:"+row.getString("sequence"))
            Some(row.getString("sequence"),row.getLong("userid"))
          }
          case _ ⇒ None
        }
    }

      println("FOUND TEST SEQUENCES:"+courseId+" cluster:"+" end date:"+endDateSinceEpoch+" size:"+results.size)
      val bestClusters=results.map{
        row=>
          /*println("ROW TEST SEQUENCE:"+row.getString("sequence"))
          val testSequence: java.util.List[ObservationDiscrete[QuartileName]] =row.getString("sequence").split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
          println("TEST-Sequence:"+ testSequence.toString)
          val userid=row.getLong("userid");*/
          println("ROW TEST SEQUENCE:"+row._1)
          val testSequence: java.util.List[ObservationDiscrete[QuartileName]] =row._1.split(",").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
          println("TEST-Sequence:"+ testSequence.toString)
          val userid=row._2;
          println("*****************************************")
          println("CHECKING ROW WITH TEST SEQUENCE:"+testSequence.toString)

          var bestCluster=None:Option[ClusterName.Value]
          var bestClusterProbability:Double=0.0

          learntHmmModels.foreach{
           case(clusterName, learntHmm)=>
           {

             val probability:Double=learntHmm.probability(testSequence);
             val probability2:Double=BigDecimal(probability).setScale(3,BigDecimal.RoundingMode.HALF_UP).toDouble
             println("PROBABILITY:"+probability+" prob2:"+probability2+" best cluster prob:"+bestClusterProbability)
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
              println("THIS IS DISABLED TEMPORARY. WE ARE NOT UPDATING HIBERNATE DB HERE")
             // clusteringDBManager.updateUserCourseProfile(courseId,userid, bestCluster.get.toString, clusterTemplate.clusterFullName)
              val tSeq=testSequence.zipWithIndex.map{case(sv,i)=>
                val jsonObject=Json.obj("featurename"->clusterTemplate.getFeatureName(i),"value"->clusterTemplate.getFeatureValue(i),"quartile"->sv.toString)
                Json.stringify(jsonObject)}.toList.asJava
              println("T--SEQ:"+tSeq)
            //  println("THIS IS DISABLED TEMPORARY")
              //UserObservationsDBManagerImpl.getInstance().updateUserCurrentProfile(courseId,userid,bestCluster.get.toString,clusterTemplate.clusterFullName,tSeq)
             // val query="UPDATE " + TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE + " SET profile=?, profilefullname=?, sequence=? WHERE course=? AND userid=?;"
              profilesDAO.updateStudentProfileInCourse(bestCluster.get.toString,clusterTemplate.clusterFullName,tSeq, courseId, userid)
              (courseId,userid,bestCluster.get.toString,clusterTemplate.clusterFullName,tSeq)
            }
            case None=>println("NO BEST CLUSTER FOUND")
              (courseId,userid,"NO_CLUSTER","NO_CLUSTER","")
          }
      }
    //println("TEMPORARY DISABLED")
    //bestClusters.saveToCassandra(dbName,TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE)

  //  "UPDATE " + TablesNames.PROFILE_USER_CURRENT_PROFILE_INCOURSE + " SET profile=?, profilefullname=?, sequence=? WHERE course=? AND userid=?;"
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
