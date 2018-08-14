package org.prosolo.bigdata.scala

import org.junit.Test
/**
 * @author zoran
 */
class TestSparkHibernate {
  @Test def testHibernateSessionForPartitions() {
    //TODO reimplement or delete
//    val sc = SparkContextLoader.getSC
//    val scalaUsersIds = Seq[Long](5,27,41,50,51,56)
//    val usersRDD: RDD[Long] = sc.parallelize(scalaUsersIds)
//    usersRDD.foreachPartition {
//
//      nodes => {
//         val session: Session = HibernateUtil.getSessionFactory().openSession()
//        nodes.foreach {
//
//          nodeid =>
//            {
//
//              try {
//                val isActive: Boolean = session.getTransaction().isActive()
//                    if (!isActive) {
//                  session.beginTransaction()
//                }
//               val node:Competence= session.load(classOf[Competence], nodeid).asInstanceOf[Competence]
//                logger.debug("FOUND Node:"+node.getTitle)
//
//                val newTag: Tag = new Tag
//                newTag.setTitle("title" + nodeid)
//
//                logger.debug("Saving tag:"+newTag)
//                session.save(newTag)
//                logger.debug("Saved tag:"+newTag)
//
//                 node.getTags().add(newTag)
//                 session.save(node)
//                 logger.debug("Saved node")
//                 val created:java.util.Date=new java.util.Date
//                 val postLink="somelink"
//                 val text="sometext"
//               val twitterId=123432432
//               val creatorName="some creator"
//               val screenName="screen name"
//               val profileUrl="profile url"
//               val profileImage="profileImage"
//
//                  val post:TwitterPost=new TwitterPost
//    post.setDateCreated(created)
//    post.setLink(postLink)
//    post.setContent(text)
//    post.setVisibility(VisibilityType.PUBLIC)
//    post.setTweetId(twitterId);
//    post.setCreatorName(creatorName);
//    post.setScreenName(screenName);
//    post.setUserUrl(profileUrl);
//    post.setProfileImage(profileImage);
//    session.save(post)
//    logger.debug("SAVED POST HERE")
//
//                 session.getTransaction().commit()
//              } catch {
//                case ex: Exception => {
//                  if (session.getTransaction() != null) {
//                    session.getTransaction().rollback()
//                    ex.printStackTrace()
//                  }
//                }
//
//              }
//            }
//
//        }
//          session.close()
//      }
//
//    }
  }
}