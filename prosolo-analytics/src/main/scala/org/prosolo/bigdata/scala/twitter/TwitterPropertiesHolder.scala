package org.prosolo.bigdata.scala.twitter
import twitter4j.conf.ConfigurationBuilder
import org.prosolo.bigdata.twitter.StreamListData
import org.prosolo.common.twitter.{PropertiesFacade, TwitterSiteProperties}
/**
 * @author zoran Aug 8, 2015
 */
object TwitterPropertiesHolder {
   val propFacade = new PropertiesFacade()
   /** Credentials used to connect with Twitter user streams.*/
  val twitterProperties:java.util.Queue[TwitterSiteProperties] = propFacade.getAllTwitterSiteProperties
    /**
  * Returns next available Twitter configuration that can be used for new stream. 
  */
  def getTwitterConfigurationBuilder(): ConfigurationBuilder = {
    val builder = new ConfigurationBuilder
    val siteProperties = twitterProperties.poll
    builder.setOAuthAccessToken(siteProperties.getAccessToken)
    builder.setOAuthAccessTokenSecret(siteProperties.getAccessTokenSecret)
    builder.setOAuthConsumerKey(siteProperties.getConsumerKey)
    builder.setOAuthConsumerSecret(siteProperties.getConsumerSecret)
    builder
  }
}