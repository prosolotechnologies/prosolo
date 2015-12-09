package org.prosolo.bigdata.scala.clustering

import java.io.{File, InputStream, Reader, FileReader}
import java.net.URI
import java.text.SimpleDateFormat
import java.util
import java.util.Date

import be.ac.ulg.montefiore.run.jahmm.io.{ObservationVectorReader, ObservationSequencesReader}
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.SequenceFile
import org.apache.mahout.classifier.sequencelearning.hmm.HmmModel
import org.apache.mahout.clustering.Cluster
import org.apache.mahout.clustering.classify.WeightedPropertyVectorWritable
import org.prosolo.bigdata.clustering.QuartileName
import org.prosolo.bigdata.scala.clustering.EventsChecker._

import scala.collection.mutable.{HashMap, Map}

//import org.prosolo.bigdata.scala.statistics.QuartileName
import org.prosolo.bigdata.utils.DateUtil
import be.ac.ulg.montefiore.run.jahmm._
import java.util.List
import scala.collection.JavaConversions._

/**
  * Created by Zoran on 05/12/15.
  */
/**
  * Zoran 05/12/15
  */
object HmmClustering  extends App{
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
  val startDate: Date = dateFormat.parse("10/20/2014")
  val endDate: Date = dateFormat.parse("10/22/2014")
  val nStates=4
  val learntHmmModels:Map[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]=new HashMap[ClusterName.Value,Hmm[ObservationDiscrete[QuartileName]]]()
    //testHmmModel()
  initializeHmmModels()
    def testHmmModel(){
      val startDateSinceEpoch = DateUtil.getDaysSinceEpoch(startDate)
      val endDateSinceEpoch = DateUtil.getDaysSinceEpoch(endDate)
      println("testing hmm model")

      val initFactoryA:OpdfDiscreteFactory[QuartileName]  = new OpdfDiscreteFactory[QuartileName](classOf[QuartileName])
      //val hmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](nStates,initFactoryA)
     // val stream: InputStream = getClass.getClassLoader.getResourceAsStream("/home/zoran/git/prosolo-multimodule/features_quartiles_110_16363_16365.seq")
      val lines: Array[String] = scala.io.Source.fromFile(new File("/home/zoran/git/prosolo-multimodule/features_quartiles_573_16363_16365.seq")).getLines.toArray
      val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
         lines.foreach { line =>
           println("sequenceA:"+line)
           val sequence: java.util.List[ObservationDiscrete[QuartileName]] = line.split(";").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
           sequences.add(sequence)

      }
      val initAHmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](nStates,initFactoryA)
      val mABwl:BaumWelchLearner=new BaumWelchLearner
      val learntAHmm:Hmm[ObservationDiscrete[QuartileName]]=mABwl.learn(initAHmm, sequences)

      //val testSequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
      val testLine="H;H;L;H;L;H;L;L;L;H;M;L;";
      val testSequence: java.util.List[ObservationDiscrete[QuartileName]] = testLine.split(";").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
      val probability:Double=learntAHmm.probability(testSequence);
      println("PROBABILITY:"+probability+" LNprob:"+learntAHmm.lnProbability(testSequence))
     // testSequences.add(testSequence)

      //val hmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](3,initFactoryA)


    }


  def initializeHmmModels()={
    ClusterName.values.foreach{clusterName=>
      val initFactory:OpdfDiscreteFactory[QuartileName]  = new OpdfDiscreteFactory[QuartileName](classOf[QuartileName])
      val sequences:List[List[ObservationDiscrete[QuartileName]]]=getClusterSequences(clusterName)
      val initHmm:Hmm[ObservationDiscrete[QuartileName]]=new Hmm[ObservationDiscrete[QuartileName]](nStates,initFactory)
      val mBwl:BaumWelchLearner=new BaumWelchLearner
      val learntHmm:Hmm[ObservationDiscrete[QuartileName]]=mBwl.learn(initHmm, sequences)
      learntHmmModels.put(clusterName,learntHmm)
    }
  }
  def getClusterSequences(clusterName:ClusterName.Value):List[List[ObservationDiscrete[QuartileName]]]={
    val lines: Array[String] = scala.io.Source.fromFile(new File("/home/zoran/git/prosolo-multimodule/features_quartiles_"+clusterName.toString+"_16363_16365.seq")).getLines.toArray
    val sequences:List[List[ObservationDiscrete[QuartileName]]]=new util.ArrayList[List[ObservationDiscrete[QuartileName]]]()
    lines.foreach { line =>
      println("sequenceA:"+line)
      val sequence: java.util.List[ObservationDiscrete[QuartileName]] = line.split(";").map(_.trim).map{s=>QuartileName.valueOf(s)}.map{qn=>qn.observation()}.toList
      sequences.add(sequence)
    }
    sequences
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
