package org.prosolo.bigdata.scala.statistics

import org.junit.Test
import org.junit.Assert._

class QuartilesTest {
   @Test def testQuartiles(){
     val quartiles:FeatureQuartiles=new FeatureQuartiles
     quartiles.findQuartiles
   }
}