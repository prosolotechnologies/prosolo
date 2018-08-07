package org.prosolo.bigdata.scala.recommendations

import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer}
import org.apache.spark.sql.DataFrame
import org.slf4j.LoggerFactory

/**
  * Created by zoran on 19/07/16.
  */
/**
  * zoran 19/07/16
  */
object FeaturesBuilder {
  val logger = LoggerFactory.getLogger(getClass)
  private def buildOneHotPipeLine(colName:String):Array[PipelineStage] = {
    val stringIndexer = new StringIndexer()
      .setInputCol(s"$colName")
      .setOutputCol(s"${colName}_index")

    val oneHotEncoder = new OneHotEncoder()
      .setInputCol(s"${colName}_index")
      .setOutputCol(s"${colName}_onehotindex")

    Array(stringIndexer,oneHotEncoder)
  }

  def buildPipeLineForFeaturePreparation():Array[PipelineStage]={
    val credentialPipelineStages=buildOneHotPipeLine("credential")
    //val someotherPipelineStages=buildOneHotPipeLine("somethingelse");
    Array.concat(credentialPipelineStages)
  }
  def buildAndTransformPipelineModel(trainingData:DataFrame):DataFrame={
    logger.debug("TRAINING DATA")
    trainingData.show(10)
    val pipelineStagesForFeatures=buildPipeLineForFeaturePreparation()
    val pipeline=new Pipeline().setStages(pipelineStagesForFeatures)
    val pipelineModel=pipeline.fit(trainingData)
    val resultsDF:DataFrame=pipelineModel.transform(trainingData)
    logger.debug("BUILD AND TRANSFORM PIPELINE MODEL")
    println(resultsDF.select("credential_index", "credential_onehotindex"))
    println(resultsDF.select("*"))
    resultsDF.show(40)
    resultsDF
  }

}
