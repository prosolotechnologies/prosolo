package org.prosolo.bigdata.scala.twitter


import java.io.InputStream

import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.JavaConverters._


/**
 * @author zoran Jul 28, 2015
 */
trait ProfanityCensor {
  val logger = LoggerFactory.getLogger(getClass)
   val badWordFile = "files/badwords.sav"
   val instagramForbiddenHashtagsFile="files/instagrambannedtags.sav"
  val customForbiddenHashtagsFile="files/customforbiddentags.sav"
   def readBadWordsFromFile(file:String): mutable.Buffer[String]= {
     val stream : InputStream =getClass.getClassLoader.getResourceAsStream(file)
     val lines: mutable.Buffer[String] = scala.io.Source.fromInputStream( stream ).getLines.toBuffer
     lines
   }
  def readDisabledHashtags(): mutable.Buffer[String] ={
    TwitterHashtagStatisticsDBManagerImpl.getInstance.getDisabledTwitterHashtags.asScala.map(tag=>"#"+tag)

  }

   val badWords=readBadWordsFromFile(badWordFile)
   val forbiddenHashtags=readBadWordsFromFile(instagramForbiddenHashtagsFile)++readBadWordsFromFile(customForbiddenHashtagsFile)++readDisabledHashtags()
  logger.debug("FORBIDDEN HASHTAGS:"+forbiddenHashtags.mkString(" "))
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
           // logger.debug("FORBIDDEN:"+text+" BECAUSE WORD:"+checkword)
          } else if(forbiddenHashtags.contains(word)){
            polite=false
           // logger.debug("FORBIDDEN:"+text+" BECAUSE HASHTAG:"+checkword)
          }
       }  
          
       }
       polite
     }
     def addDisabledHashtag(hashtag:String): Unit ={
       forbiddenHashtags+="#"+hashtag
       logger.debug("Adding disabled hashtag:"+hashtag+" RESULTING:"+forbiddenHashtags.mkString(" "))
     }
     def enableDisabledHashtag(hashtag:String): Unit ={
       forbiddenHashtags-="#"+hashtag
      logger.debug("REMOVING HASHTAG:"+hashtag+" RESULTING:"+forbiddenHashtags.mkString(" "))
     }
   }
