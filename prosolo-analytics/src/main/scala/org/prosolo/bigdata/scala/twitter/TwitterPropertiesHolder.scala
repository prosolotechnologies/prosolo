package org.prosolo.bigdata.scala.twitter

import twitter4j.conf.ConfigurationBuilder
import org.prosolo.bigdata.twitter.StreamListData
import org.prosolo.common.twitter.{PropertiesFacade, TwitterSiteProperties}
import org.slf4j.LoggerFactory

/**
  * @author zoran Aug 8, 2015
  */
object TwitterPropertiesHolder {
  val propFacade = new PropertiesFacade()
  val logger = LoggerFactory.getLogger(getClass)
  /** Credentials used to connect with Twitter user streams. */
  val twitterProperties: java.util.Queue[TwitterSiteProperties] = propFacade.getAllTwitterSiteProperties

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
    logger.debug("Twitter Configuration builder: accessToken:" + siteProperties.getAccessToken + " accessTokenSecret:" + siteProperties.getAccessTokenSecret
      + " consumerKey:" + siteProperties.getConsumerKey + " consumerSecret:" + siteProperties.getConsumerSecret)
    builder
  }
}