package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.domainmodel.featuredNews.FeaturedNewsInbox;
import org.prosolo.domainmodel.featuredNews.FeaturedNewsType;
import org.prosolo.domainmodel.featuredNews.LearningGoalFeaturedNews;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.FeaturedNewsManager;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.interaction.FeaturedNewsManager")
public class FeaturedNewsManagerImpl extends AbstractManagerImpl implements	FeaturedNewsManager {

	private static final long serialVersionUID = 1687691247880121312L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FeaturedNewsManager.class);
	
	@Autowired private DefaultManager defaultManager;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public LearningGoalFeaturedNews createPublicFeaturedNewsForEvent(Event event, Session session) {
		//defaultManager.merge(event);
//		event=(Event) session.merge(event);
		LearningGoalFeaturedNews featuredNews = new LearningGoalFeaturedNews();
//		featuredNews.setEvent(event);
		
		featuredNews.setActor(event.getActor());
		featuredNews.setAction(event.getAction());
		
//		BaseEntity object = event.getObject();
//		if (object != null) { 
//			if (object instanceof Node) {
//				featuredNews.setObject((Node) object);
//			} else {
//				logger.error("Object of SocialActivity must be subclass of Node class");
//			}
//		}
		
		featuredNews.setDate(new Date());
		featuredNews.setResource((LearningGoal) event.getObject());
		featuredNews.setActor(event.getActor());
		featuredNews.setFeaturedNewsType(FeaturedNewsType.PUBLIC);
		session.save(featuredNews);
		return featuredNews;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<LearningGoalFeaturedNews> readPublicFeaturedNews(User user, int page, int limit) {
		String query = 
			"SELECT DISTINCT news " + 
			"FROM LearningGoalFeaturedNews news " +
			"WHERE news.featuredNewsType = :featuredNewsType " +
			"AND news.actor != :user "  +
			"ORDER BY news.date DESC ";
		//persistence.currentManager().clear();
	 	@SuppressWarnings("unchecked")
		List<LearningGoalFeaturedNews> fNews = persistence.currentManager().createQuery(query)
				.setString("featuredNewsType", FeaturedNewsType.PUBLIC.toString())
				.setEntity("user", user)
				.setFirstResult(page * limit)
				.setMaxResults(limit)
				.list();
			return fNews;
	}
	
	@Override
	@Transactional (readOnly = false)
	public FeaturedNewsInbox getUserFeaturedNewsInbox(User user) {
		String query = 
			"SELECT DISTINCT fnInbox " +
			"FROM FeaturedNewsInbox fnInbox " +
			"LEFT JOIN fnInbox.user user " +
			"WHERE user = :user ";
		
		FeaturedNewsInbox result = (FeaturedNewsInbox) persistence.currentManager().createQuery(query)
				.setEntity("user", user)
				.uniqueResult();
		
		if (result != null) {
			return result;
		} else {
			FeaturedNewsInbox fnInbox = new FeaturedNewsInbox();
			fnInbox.setUser(user);
			return saveEntity(fnInbox);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<LearningGoalFeaturedNews> getActiveFeaturedNews(User user, int limit) {
		String query =
			"SELECT DISTINCT news " +
			"FROM FeaturedNewsInbox fnInbox " +
			"LEFT JOIN fnInbox.user user " +
			"LEFT JOIN fnInbox.featuredNews featuredNews " +
			"LEFT JOIN featuredNews.news news " +
			"WHERE featuredNews.dismissed = :dismissed " +
				"AND user = :user " +
			"ORDER BY news.dateCreated DESC";
		
		Query q = persistence.currentManager().createQuery(query)
				.setEntity("user", user)
				.setBoolean("dismissed", false);
		
		if (limit > 0) {
			q.setMaxResults(limit);
		}
		
		@SuppressWarnings("unchecked")
		List<LearningGoalFeaturedNews> result = q.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return new ArrayList<LearningGoalFeaturedNews>();
	}

}
