package org.prosolo.bigdata.scala.twitter

import java.net.URL
import java.io.InputStream
/**
 * @author zoran Jul 28, 2015
 */
trait ProfanityCensor {
   val badWordFile = "files/badwords.sav"
   def readBadWordsFromFile(): Array[String]= {
     val stream : InputStream =getClass.getClassLoader.getResourceAsStream(badWordFile)
     val lines: Array[String] = scala.io.Source.fromInputStream( stream ).getLines.toArray
     lines.foreach(x=>println(x))
     lines
   }
   val badWords=readBadWordsFromFile
}
   class BadWordsCensor extends ProfanityCensor{
     def isPolite(text:String):Boolean={
       var polite=true
       val wordsList:List[String]=text.split(" ").toList
       wordsList.map { word =>  
         if(badWords.contains(word)) polite=false  
       }
       polite
     }
   }
