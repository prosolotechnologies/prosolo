package org.prosolo.web.home;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.featuredNews.LearningGoalFeaturedNews;
import org.prosolo.services.interaction.FeaturedNewsManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.data.FeaturedNewsData;
import org.prosolo.web.home.util.FeaturedNewsConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "featuredNewsBean")
@Component("featuredNewsBean")
@Scope("session")
public class FeaturedNewsBean implements Serializable {

	private static final long serialVersionUID = -5400648943341522990L;

	private static Logger logger = Logger.getLogger(FeaturedNewsBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private FeaturedNewsManager featuredNewsManager;

	private LinkedList<FeaturedNewsData> featuredNewsList;
	private final int elementsPerPage = 2;
	private FeaturedNewsData feedFeaturedNewsData;

	public void init() {
		if (featuredNewsList == null) {
			logger.debug("initializing");
			
			featuredNewsList = new LinkedList<FeaturedNewsData>();
			
			List<LearningGoalFeaturedNews> fNewsList = featuredNewsManager.readPublicFeaturedNews(
					loggedUser.getUser(), 
					0,
					elementsPerPage);
			
			if (fNewsList != null) {
				featuredNewsList = FeaturedNewsConverter.convertFeaturedNewsToFeaturedNewsData(fNewsList);
			}
		}
	}

	public void addFeaturedNews(LearningGoalFeaturedNews featuredNews) {
		FeaturedNewsData fNewsData = FeaturedNewsConverter.convertFeaturedNewsToFeaturedNewsData(featuredNews);
		
		if (fNewsData != null) {
			featuredNewsList.push(fNewsData);
		}
	}

	public void addFeaturedNewsData(FeaturedNewsData featuredNewsData) {
		if (featuredNewsData != null) {
			featuredNewsList.push(featuredNewsData);
		}
	}

	public int getFeaturedNewsSize() {
		if (featuredNewsList != null) {
			if ((featuredNewsList.size()) <= elementsPerPage) {
				return featuredNewsList.size();
			} else {
				return elementsPerPage;
			}
		}
		return 0;
	}

	public boolean hasMoreFeaturedNews() {
		if (featuredNewsList != null) {
			return featuredNewsList.size() > elementsPerPage;
		}
		return false;
	}

	public int getRefreshRate() {
		return Settings.getInstance().config.application.featuredNewsRefreshRate;
	}

	/*
	 * GETTERS / SETTERS
	 */

	public LinkedList<FeaturedNewsData> getFeaturedNewsList() {

		return featuredNewsList;
	}

	public void setFeaturedNewsList(LinkedList<FeaturedNewsData> featuredNewsList) {
		this.featuredNewsList = featuredNewsList;
	}

	public FeaturedNewsData getFeedFeaturedNewsData() {
		return feedFeaturedNewsData;
	}

	public void setFeedFeaturedNewsData(FeaturedNewsData feedFeaturedNewsData) {
		this.feedFeaturedNewsData = feedFeaturedNewsData;
	}
}
