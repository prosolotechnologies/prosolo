package org.prosolo.web.search;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.featuredNews.LearningGoalFeaturedNews;
import org.prosolo.services.interaction.FeaturedNewsManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.data.FeaturedNewsData;
import org.prosolo.web.home.util.FeaturedNewsConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
@author "Zoran Jeremic"
@date 2013-04-10
 */
@ManagedBean(name = "searchFeaturedNewsBean")
@Component("searchFeaturedNewsBean")
@Scope("view")
public class SearchFeaturedNewsBean implements Serializable{

	private static final long serialVersionUID = 7425398428736202443L;
	
	private static Logger logger = Logger.getLogger(SearchFeaturedNewsBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private FeaturedNewsManager featuredNewsManager;
	
	private LinkedList<FeaturedNewsData> featuredNewsList;
	private int limit = 7;
	private int page = 0;
	
	public void init() {
		logger.debug("initializing");
		
		List<LearningGoalFeaturedNews> fNewsList = featuredNewsManager.readPublicFeaturedNews(loggedUser.getUser(), page, limit);
		featuredNewsList = FeaturedNewsConverter.convertFeaturedNewsToFeaturedNewsData(fNewsList);
	}
 
	public void addFeaturedNewsData(FeaturedNewsData featuredNewsData){
		if (featuredNewsData != null) {
			featuredNewsList.push(featuredNewsData);
		}
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
 
}
