package org.prosolo.bigdata.scala.twitter

import java.util.{Timer, TimerTask}

import scala.collection.mutable.ListBuffer
import org.prosolo.bigdata.scala.spark.SparkManager
import org.prosolo.bigdata.scala.messaging.BroadcastDistributer
import org.prosolo.common.messaging.data.{ServiceType => MServiceType}
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType
import org.prosolo.common.domainmodel.user.{AnonUser, User, UserType}
import org.prosolo.common.domainmodel.organization.VisibilityType
import twitter4j.Status
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import org.prosolo.bigdata.dal.persistence.TwitterStreamingDAO
import org.prosolo.bigdata.dal.persistence.HibernateUtil
import org.hibernate.Session
import org.prosolo.common.domainmodel.annotation.Tag

import scala.collection.JavaConversions._
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl
import org.prosolo.common.util.date.DateUtil
import org.prosolo.bigdata.common.dal.pojo.TwitterHashtagDailyCount
import org.prosolo.bigdata.scala.twitter.StatusListener.getClass
import org.prosolo.common.util.date.DateEpochUtil
import org.slf4j.LoggerFactory


/**
  * @author zoran Jul 28, 2015
  */
object TwitterStatusBuffer {
  val logger = LoggerFactory.getLogger(getClass)
  val buffer: ListBuffer[Status] = ListBuffer()
  val profanityFilter: BadWordsCensor = new BadWordsCensor

  /** heartbeat scheduler timer. */
  private[this] val timer = new Timer("Statuses Updates Monitor", true)
  timer.scheduleAtFixedRate(new TimerTask {
    def run() {
      processBufferStatuses
    }
  }, 1000, 10000)

  def addStatus(status: Status) {
    buffer += (status)
  }

  def pullStatuses(): ListBuffer[Status] = {
    var statuses: ListBuffer[Status] = new ListBuffer[Status]()
    statuses = statuses ++ buffer
    buffer.clear()
    statuses
  }

  def disableHashtagInFilter(hashtag: String): Unit = {
    profanityFilter.addDisabledHashtag(hashtag)
  }

  def enableHashtagInFilter(hashtag: String): Unit = {
    profanityFilter.enableDisabledHashtag(hashtag)
  }

  def processBufferStatuses() {

    val statuses = pullStatuses
    val sc = SparkManager.sparkContextLoader.getSC
    val statusesRDD = sc.parallelize(statuses)
    val filteredStatusesRDD = statusesRDD.filter {
      isAllowed
    }
    filteredStatusesRDD.foreachPartition { statuses => {
      statuses.foreach { status: Status => {
        processStatus(status)

      }
      }
    }


    }
  }

  def isAllowed(status: Status): Boolean = {
    val isPolite: Boolean = profanityFilter.isPolite(status.getText)
    isPolite
  }

  def processStatus(status: Status) {
    logger.debug("process status")

    val twitterUser = status.getUser
    val twitterHashtags: java.util.List[String] = new java.util.ArrayList[String]()
    status.getHashtagEntities.map { htent => twitterHashtags.add(htent.getText.toLowerCase) }
    val (twitterId, cName, screenName, profileImage) = (twitterUser.getId, twitterUser.getName, twitterUser.getScreenName, twitterUser.getProfileImageURL)
    val creatorName = cName.replaceAll("[^\\x00-\\x7f-\\x80-\\xad]", "")
    val profileUrl = "https://twitter.com/" + screenName


    val twitterStreamingDao: TwitterStreamingDAO = new TwitterStreamingDAOImpl
    val session: Session = HibernateUtil.getSessionFactory().openSession()
    val isActive: Boolean = session.getTransaction().isActive()
    if (!isActive) {
      session.beginTransaction()
    }
    var poster: User = twitterStreamingDao.getUserByTwitterUserId(twitterId, session);


    val (text, created, postLink) = (status.getText, status.getCreatedAt, "https://twitter.com/" + twitterUser.getScreenName + "/status/" + status.getId)
    val statusText = text.replaceAll("[^\\x00-\\x7f-\\x80-\\xad]", "")
    printTweet("current", creatorName, profileUrl, screenName, profileImage, statusText)
    logger.debug("is retweet:" + status.isRetweet + " is retweeted:" + status.isRetweeted)
    logger.debug("retweeted status " + (if (status.getRetweetedStatus == null) "NULL" else status.getRetweetedStatus.getText));
    val twitterPostSocialActivity = if (status.isRetweet) {
      logger.debug("this is retweet")

      val reStatus = status.getRetweetedStatus
      val reTwitterUser = reStatus.getUser
      val (reText, reCreated, rePostLink) = (reStatus.getText, status.getCreatedAt, "https://twitter.com/" + reTwitterUser.getScreenName + "/status/" + reStatus.getId)
      val reStatusText = reText.replaceAll("[^\\x00-\\x7f-\\x80-\\xad]", "")
      val (reTwitterId, reCName, reScreenName, reProfileImage) = (reTwitterUser.getId, reTwitterUser.getName, reTwitterUser.getScreenName, reTwitterUser.getProfileImageURL)
      val reCreatorName = reCName.replaceAll("[^\\x00-\\x7f-\\x80-\\xad]", "")
      val reProfileUrl = "https://twitter.com/" + reScreenName
      printTweet("RE-TWEET:", reCreatorName, reProfileUrl, reScreenName, reProfileImage, reStatusText)
      twitterStreamingDao.createTwitterPostSocialActivity(
        poster, reCreated, rePostLink, twitterId, true, reText, reCreatorName, reScreenName, reProfileUrl, reProfileImage,
        reStatusText, twitterHashtags, session);
    } else {
      logger.debug("create twitter post")
      twitterStreamingDao.createTwitterPostSocialActivity(
        poster, created, postLink, twitterId, false, "", creatorName, screenName, profileUrl, profileImage,
        statusText, twitterHashtags, session);
    }
    session.getTransaction().commit()
    session.close();
    val day = DateEpochUtil.getDaysSinceEpoch;
    twitterHashtags.map { hashtag => TwitterHashtagStatisticsDBManagerImpl.getInstance().updateTwitterHashtagDailyCount(hashtag, day) };
    if (twitterPostSocialActivity != null) {
      logger.debug("broadcasting tweet")
      val parameters: java.util.Map[String, String] = new java.util.HashMap[String, String]()
      parameters.put("socialActivityId", twitterPostSocialActivity.getId.toString())
      BroadcastDistributer.distributeMessage(MServiceType.BROADCAST_SOCIAL_ACTIVITY, parameters)
    } else {
      logger.debug("ERROR: TwitterPostSocialActivity was not initialized")
    }


  }

  def printTweet(statusType: String, creatorName: String, profileUrl: String, screenName: String, profileImage: String, text: String): Unit = {
    logger.debug("statusType:" + statusType + " creatorName:" + creatorName + " profileUrl:" + profileUrl + " screenName:" + screenName + " profileImage:" + profileImage + " text:" + text);
  }

  def initAnonUser(creatorName: String, profileUrl: String, screenName: String, profileImage: String): AnonUser = {
    val anonUser: AnonUser = new AnonUser
    anonUser.setName(creatorName)
    anonUser.setProfileUrl(profileUrl)
    anonUser.setNickname(screenName)
    anonUser.setAvatarUrl(profileImage)
    anonUser.setServiceType(ServiceType.TWITTER)
    anonUser.setUserType(UserType.TWITTER_USER)
    anonUser.setDateCreated(new java.util.Date)
    anonUser
  }

}