package org.prosolo.bigdata.scala.twitter

import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl
import org.prosolo.bigdata.twitter.StreamListData
import org.prosolo.bigdata.dal.persistence.impl.TwitterStreamingDAOImpl
import twitter4j.{FilterQuery, TwitterStream}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer
import org.prosolo.bigdata.dal.persistence.HibernateUtil
import org.hibernate.Session

/**
  * @author zoran
  */
object TwitterHashtagsStreamsManager extends TwitterStreamsManager {

  val logger = LoggerFactory.getLogger(getClass)
  /** Keeps information about each hashtag and which users or learning goals are interested in it. Once nobody is interested in hashtag it can be removed   */
  val hashtagsAndReferences: collection.mutable.Map[String, StreamListData] = new collection.mutable.HashMap[String, StreamListData]
  /** Keeps reference to twitter stream based on stream id, and list of hashtags in filter  */
  val twitterStreamsAndHashtags: collection.mutable.Map[Int, (TwitterStream, ListBuffer[String])] = new collection.mutable.HashMap[Int, (TwitterStream, ListBuffer[String])]

  def getHashTags(): java.util.Set[String] = {
    logger.debug("getHashTags")
    val result: java.util.Set[String] = new java.util.HashSet[String]
    val session: Session = HibernateUtil.getSessionFactory().openSession()
    val twitterDAO = new TwitterStreamingDAOImpl()
    val hashtagsAndRefs: collection.mutable.Map[String, StreamListData] = twitterDAO.readAllHashtagsAndCredentialIds(session).asScala
    val hashTagsUserIds: collection.mutable.Map[String, java.util.List[java.lang.Long]] = twitterDAO.readAllUserPreferedHashtagsAndUserIds(session).asScala
    session.close()
    for ((hashtag, _) <- hashTagsUserIds) {
      logger.debug("User hashtag:" + hashtag)
      result.add(hashtag);
    }
    for ((hashtag, _) <- hashtagsAndRefs) {
      logger.debug("hashtag:" + hashtag)
      logger.debug("hashtag hashtag:" + hashtag)
      result.add(hashtag);
    }
    result
  }

  /**
    * At the applicaiton startup reads all hashtags from database and initialize required number of spark twitter streams to listen for it on Twitter
    */
  def initialize() {
    logger.info("INITIALIZE TWITTER STREAMING")
    val twitterDAO = new TwitterStreamingDAOImpl()
    val session: Session = HibernateUtil.getSessionFactory.openSession()
    val hashtagsAndRefs: collection.mutable.Map[String, StreamListData] = twitterDAO.readAllHashtagsAndCredentialIds(session).asScala
    hashtagsAndReferences ++= hashtagsAndRefs;
    val hashTagsUserIds: collection.mutable.Map[String, java.util.List[java.lang.Long]] = twitterDAO.readAllUserPreferedHashtagsAndUserIds(session).asScala
    session.close();
    for ((hashtag, userIds) <- hashTagsUserIds) {
      val listData: StreamListData = hashtagsAndReferences.getOrElse(hashtag, new StreamListData(hashtag))
      listData.addUsersIds(userIds)
      hashtagsAndReferences.put(hashtag, listData)
    }
    val disabled: Buffer[String] = TwitterHashtagStatisticsDBManagerImpl.getInstance.getDisabledTwitterHashtags.asScala
    disabled.foreach(disabledhashtag => {
      if (hashtagsAndReferences.contains("#" + disabledhashtag)) {
        hashtagsAndReferences.get("#" + disabledhashtag).get.setDissabled(true)
      }
    }
    )
    logger.info("INITIALIZE TWITTER STREAMING STARTING...")
    startStreamsForInitialSetOfData
    logger.info("INITIALIZE TWITTER STREAMING FINISHED")
  }

  /**
    * Start streams for all hashtags pulled from database
    */
  def startStreamsForInitialSetOfData() {
    val currentFilterList: ListBuffer[String] = new ListBuffer[String]()
    for ((tag, streamListData) <- hashtagsAndReferences) {
      if (!streamListData.isDissabled) {
        currentFilterList += (tag)
        currentFilterList.size match {
          case x if x > STREAMLIMIT => initializeNewCurrentListAndStream(currentFilterList)
          case _ =>
        }
        streamListData.setStreamId(streamsCounter)
      } else {
        logger.debug("NOT FOLLOWING THIS BECAUSE DISABLED:" + tag)
      }

    }
    initializeNewCurrentListAndStream(currentFilterList)
  }

  def getLatestStreamAndList(): (TwitterStream, ListBuffer[String]) = {
    twitterStreamsAndHashtags.get(streamsCounter - 1) match {
      case None => null
      case x: Option[(TwitterStream, ListBuffer[String])] => x.get

    }
  }

  def getLatestStreamList(): ListBuffer[String] = {
    twitterStreamsAndHashtags.get(streamsCounter - 1) match {
      case None => new ListBuffer[String]
      case x: Option[(TwitterStream, ListBuffer[String])] => x.get._2

    }
  }

  def initializeNewCurrentListAndStream(newCurrentFilterList: ListBuffer[String]) {
    if (newCurrentFilterList.size > 0) {
      val (stream, streamId): (TwitterStream, Int) = initializeNewStream(newCurrentFilterList)
      twitterStreamsAndHashtags.put(streamId, (stream, newCurrentFilterList))
    }

  }

  def addNewHashTags(hashtags: ListBuffer[String], userId: Int, goalId: Int): Boolean = {
    var changed = false
    val currentFilterList: ListBuffer[String] = getLatestStreamList()
    for (hashtag <- hashtags) {
      if (currentFilterList.size > STREAMLIMIT) {
        restartStream(getLatestStreamAndList._1, getLatestStreamAndList._2)
        initializeNewCurrentListAndStream(currentFilterList)
        changed = false
      }
      hashtagsAndReferences.contains(hashtag) match {
        case true =>
        case _ => if (!currentFilterList.contains(hashtag)) {
          currentFilterList += (hashtag);
          changed = true;
        }
      }
      val listData: StreamListData = hashtagsAndReferences.getOrElseUpdate(hashtag, new StreamListData(hashtag, userId.toLong, goalId.toLong))
      val streamid = if (streamsCounter == 0) 0 else streamsCounter - 1
      listData.setStreamId(streamid)
    }
    if (streamsCounter == 0) {
      initializeNewCurrentListAndStream(currentFilterList)
    }
    changed
  }

  def enableHashtagByListData(listData: StreamListData): Unit = {
    logger.debug("Enable hashtag by list data:" + listData.getHashtag)
    val currentFilterList: ListBuffer[String] = getLatestStreamList
    if (currentFilterList.size > STREAMLIMIT) {
      initializeNewCurrentListAndStream(currentFilterList)
    }


    val streamid = if (streamsCounter == 0) 0 else streamsCounter - 1
    listData.setStreamId(streamid)
    val latestStreamAndList = getLatestStreamAndList()
    val streamList = getLatestStreamList()
    streamList += listData.getHashtag
    val stream = getLatestStreamAndList()._1

    twitterStreamsAndHashtags.put(streamid, (stream, streamList))
    restartStream(getLatestStreamAndList._1, getLatestStreamAndList._2)
  }

  def removeHashTags(hashtags: ListBuffer[String], userId: Int, goalId: Int): ListBuffer[Int] = {

    val changedIds: ListBuffer[Int] = new ListBuffer[Int]()
    for (hashtag <- hashtags) {
      val listData: StreamListData = hashtagsAndReferences.get(hashtag).get
      if (goalId > 0) listData.removeLearningGoalId(goalId.toLong)
      if (userId > 0) listData.removeUserId(userId.toLong)
      if (listData.isFreeToRemove()) {
        val changedId = listData.getStreamId
        if (!changedIds.contains(changedId)) {
          changedIds += changedId
        }
        removeHashTagByStreamListData(hashtag, listData)
      }
    }
    changedIds
  }

  def removeHashTagByStreamListData(hashtag: String, listData: StreamListData) = {
    logger.debug("REMOVE HASH TAG:" + hashtag)
    val changedId = listData.getStreamId
    val streamHashtagsTuple: (TwitterStream, ListBuffer[String]) = twitterStreamsAndHashtags.get(changedId).get
    val newhashtags: ListBuffer[String] = streamHashtagsTuple._2.filterNot(p => p == hashtag)
    twitterStreamsAndHashtags.put(changedId, (streamHashtagsTuple._1, newhashtags))
  }

  /**
    * Receives an array of events from buffer and add it to stream, followed by stream restart
    */
  def updateHashTagsFromBufferAndRestartStream(eventsTuples: ListBuffer[(ListBuffer[String], ListBuffer[String], Int, Int)]) {
    var currentStreamChanged = false
    logger.debug("updateHashTagsFromBufferAndRestartStream called. Current streamsCounter:" + streamsCounter + " streams size:" + twitterStreamsAndHashtags.size)
    val changedIds: ListBuffer[Int] = new ListBuffer[Int]()
    for (eventTuple <- eventsTuples) {
      val addedHashtags: ListBuffer[String] = eventTuple._1
      val removedHashtags: ListBuffer[String] = eventTuple._2
      val userid: Int = eventTuple._3
      val goalid: Int = eventTuple._4
      logger.debug("ADDED :" + addedHashtags + " removed:" + removedHashtags) + "..."
      if (addedHashtags.nonEmpty) {
        if (addNewHashTags(addedHashtags, userid, goalid)) {
          currentStreamChanged = true
        }
      }
      if (removedHashtags.nonEmpty) {
        val changedStreamsIds = removeHashTags(removedHashtags, userid, goalid)
        changedIds ++= changedStreamsIds
      }

    }
    if (currentStreamChanged && streamsCounter > 0) {
      if (changedIds.contains(streamsCounter - 1)) {
        changedIds -= streamsCounter - 1
      }
      restartStream(getLatestStreamAndList._1, getLatestStreamAndList._2)
    }
    if (!changedIds.isEmpty) {
      for (streamid <- changedIds) {
        restartStreamById(streamid)

      }
    }
  }

  def restartStreamById(streamid: Integer): Unit = {
    logger.debug("RESTART STREAM BY ID:" + streamid)
    val streamTagsTuple = twitterStreamsAndHashtags.get(streamid).get
    if (streamTagsTuple._2.size > 0) {
      restartStream(streamTagsTuple._1, streamTagsTuple._2)
    } else {
      terminateStream(streamTagsTuple._1)
      twitterStreamsAndHashtags.remove(streamid)
    }
  }

  /**
    * Initialize new stream for an array of hashtags
    */
  def initializeNewStream(filters: Buffer[String]): (TwitterStream, Int) = {
    super.initializeNewStream(new FilterQuery().track(filters: _*))
  }

  def restartStream(twitterStream: TwitterStream, filters: ListBuffer[String]) {
    //twitterStream:TwitterStream, streamId:Int){
    super.restartStream(twitterStream, new FilterQuery().track(filters: _*))
    logger.debug("STREAM FILTER:" + filters.mkString(" "))
  }

  def terminateStream(twitterStream: TwitterStream) {
    twitterStream.shutdown()

  }

  def adminDisableHashtag(hashtag: String): Unit = {
    logger.debug("Admin disable hashtag:" + hashtag)
    if (hashtagsAndReferences.contains("#" + hashtag)) {
      val streamListData = hashtagsAndReferences.get("#" + hashtag).get
      streamListData.setDissabled(true)
      removeHashTagByStreamListData("#" + hashtag, streamListData)
      restartStreamById(streamListData.getStreamId)
    }
  }

  def adminEnableHashtag(hashtag: String): Unit = {
    logger.debug("Admin enable hashtag:" + hashtag)
    if (hashtagsAndReferences.contains("#" + hashtag)) {
      logger.debug("contains it")
      val streamListData = hashtagsAndReferences.get("#" + hashtag).get
      streamListData.setDissabled(false)
      enableHashtagByListData(streamListData)
    }
  }


}