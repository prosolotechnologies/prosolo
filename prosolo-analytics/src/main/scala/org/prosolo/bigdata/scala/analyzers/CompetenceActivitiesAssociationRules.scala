package org.prosolo.bigdata.scala.analyzers

import org.prosolo.bigdata.dal.cassandra.impl.{AnalyzedResultsDBmanagerImpl, AnalyticalEventDBManagerImpl}
import org.prosolo.bigdata.es.AssociationRulesIndexerImpl
import org.prosolo.bigdata.scala.spark.SparkContextLoader
import org.prosolo.bigdata.spark.CompetenceActivitiesAssociationRulesAnalyzer
import scala.collection.JavaConverters._

/**
  * Created by zoran on 09/01/16.
  */
/**
  * zoran 09/01/16
  */
class CompetenceActivitiesAssociationRules {
  def analyzeCompetenceActivitesAssociationRules(): Unit ={
    val sc=SparkContextLoader.getSC
    //val eventDBManager = new AnalyticalEventDBManagerImpl
    //val dbManager = new AnalyzedResultsDBmanagerImpl
   // val indexer = new AssociationRulesIndexerImpl
    val associationRulesAnalyzer=new CompetenceActivitiesAssociationRulesAnalyzer
    val competences = AnalyticalEventDBManagerImpl.getInstance().findAllCompetences
    val competencesRDD=sc.parallelize(competences.asScala)
    competencesRDD.foreach{
      competence=>
        associationRulesAnalyzer.analyseCompetence(competence)
    }
  }
}
