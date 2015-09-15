package org.prosolo.bigdata.scala

import org.junit.Test
import org.prosolo.bigdata.twitter.TestJava8Paralelizm
/**
 * @author zoran
 */
class TestJavaParalelismHibernate {
   @Test def testJavaParalelismHibernate() {
     val userids = new java.util.ArrayList[java.lang.Long]
    userids.add(5) 
    userids.add(27) 
    userids.add(41) 
    userids.add(50) 
    userids.add(51) 
    userids.add(56) 
 
     TestJava8Paralelizm.getInstance.runInParalelForUsers(userids)
     Thread.sleep(100000)
   }
}