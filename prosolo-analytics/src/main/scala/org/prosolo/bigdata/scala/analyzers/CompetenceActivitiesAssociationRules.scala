package org.prosolo.bigdata.scala.analyzers

import org.prosolo.bigdata.dal.cassandra.impl.AnalyticalEventDBManagerImpl
import org.prosolo.bigdata.scala.messaging.BroadcastDistributer.getClass
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.bigdata.spark.CompetenceActivitiesAssociationRulesAnalyzer

import scala.collection.JavaConverters._
import org.prosolo.bigdata.scala.spark.{SparkContextLoader, SparkManager}
import org.slf4j.LoggerFactory
/**
  * Created by zoran on 09/01/16.
  */
/**
  * zoran 09/01/16
  */
class CompetenceActivitiesAssociationRules {
  val logger = LoggerFactory.getLogger(getClass)
  def analyzeCompetenceActivitesAssociationRules(): Unit ={
    val sc=SparkManager.sparkContextLoader.getSC
    val associationRulesAnalyzer=new CompetenceActivitiesAssociationRulesAnalyzer
    val competences = AnalyticalEventDBManagerImpl.getInstance().findAllCompetences
    val competencesRDD=sc.parallelize(competences.asScala)
    competencesRDD.foreach{
      competence=>
        logger.debug("Analyze competence:"+competence)
        associationRulesAnalyzer.analyseCompetence(competence)
    }
  }
}
