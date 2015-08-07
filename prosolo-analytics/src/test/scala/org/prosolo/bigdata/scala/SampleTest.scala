package org.prosolo.bigdata.scala

import java.util.ArrayList
import org.junit.Test
import org.junit.Assert._
/**
 * @author zoran Aug 6, 2015
 */
class SampleTest {
  @Test def listAdd(){
    val list=new ArrayList[String]
    list.add("one")
    list.add("two")
    assertEquals(2,list.size())
  }
}