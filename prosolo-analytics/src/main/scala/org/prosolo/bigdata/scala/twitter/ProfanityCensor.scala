package org.prosolo.bigdata.scala.twitter

import java.net.URL
import java.io.InputStream
/**
 * @author zoran Jul 28, 2015
 */
trait ProfanityCensor {
   val badWordFile = "files/badwords.sav"
   val forbiddenHashtagsFile="files/instagrambannedtags.sav"
   def readBadWordsFromFile(file:String): Array[String]= {
     val stream : InputStream =getClass.getClassLoader.getResourceAsStream(file)
     val lines: Array[String] = scala.io.Source.fromInputStream( stream ).getLines.toArray
     
     
     lines
   }
   val badWords=readBadWordsFromFile(badWordFile)
   val forbiddenHashtags=readBadWordsFromFile(forbiddenHashtagsFile)
}
   class BadWordsCensor extends ProfanityCensor{
     def isPolite(text:String):Boolean={
       var polite=true
       val wordsList:List[String]=text.split(" ").toList
       wordsList.map { word =>{
        var anyword=word
         if(anyword.startsWith("#")){
           anyword=word.replace("#", "")
         }
          if(badWords.contains(anyword)){
            polite=false
           // println("FORBIDDEN:"+text+" BECAUSE WORD:"+checkword)
          } else if(forbiddenHashtags.contains(word)){
            polite=false
           // println("FORBIDDEN:"+text+" BECAUSE HASHTAG:"+checkword)
          }
       }  
          
       }
       polite
     }
   }
