package org.prosolo.web.home;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.recommendation.SuggestedLearningService;
import org.prosolo.services.nodes.NodeRecommendationManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.data.RecommendationData;
import org.prosolo.web.home.util.RecommendationConverter;
import org.prosolo.web.search.SearchSuggestedLearningBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "suggestedLearningBean")
@Component("suggestedLearningBean")
@Scope("session")
public class SuggestedLearningBean implements Serializable {

	private static final long serialVersionUID = -6719661304377654493L;
	private static Logger logger = Logger.getLogger(SuggestedLearningBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private SuggestedLearningService suggestedLearningService;
	@Autowired private RecommendationConverter recommendationConverter;
	@Autowired private NodeRecommendationManager recommendationManager;
	@Autowired private ApplicationBean applicationBean;

	private LinkedList<RecommendationData> suggestedByColleagues;
	private List<RecommendationData> suggestedBySystem;
	private List<RecommendationData> suggestedByCourse;
	private int page = 0;
	private int elementsPerPage = 5;
	private int numberOfSuggestedByColleagues = 0;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public void initializeSuggestedByColleagues(){
		if (suggestedByColleagues == null) {
	 		logger.debug("Initializing suggested by colleagues");
			numberOfSuggestedByColleagues = suggestedLearningService.findNumberOfSuggestedLearningResourcesByCollegues(loggedUser.getUser(), RecommendationType.USER);
			
			List<Recommendation> suggestedByColleaguesRecommendations = suggestedLearningService
					.findSuggestedLearningResourcesByCollegues(
							loggedUser.getUser(), 
							RecommendationType.USER, 
							page, 
							elementsPerPage);
			
			suggestedByColleagues = recommendationConverter.convertRecommendationsToRecommendedData(suggestedByColleaguesRecommendations);
		}
	}
	
	public void initializeSuggestedBySystem(){
		if (suggestedBySystem == null) {
	 		logger.debug("Initializing suggested by system");
			try{
			List<Node> systemRecommendations = suggestedLearningService
					.findSuggestedLearningResourcesBySystem(
							loggedUser.getUser(), 
							Settings.getInstance().config.application.defaultLikeThisItemsNumber);

			logger.debug("Converting suggested by system");
		
			suggestedBySystem = recommendationConverter.convertNodesToRecommendedData(systemRecommendations,loggedUser.getUser());
			}catch(Exception ex){
				ex.getStackTrace();
			}
			logger.debug("Suggested by system finished");
		}
	}
	
	public List<RecommendationData> getSuggestedByCourse() {
		return suggestedByCourse;
	}

	public void setSuggestedByCourse(List<RecommendationData> suggestedByCourse) {
		this.suggestedByCourse = suggestedByCourse;
	}
	
	public void loadSuggestedByCourse(){
		List<Node> courseCompetences = suggestedLearningService
				.findSuggestedLearningResourcesByCourse(
						loggedUser.getUser(), 
						Settings.getInstance().config.application.defaultLikeThisItemsNumber);
		suggestedByCourse = recommendationConverter.convertNodesToRecommendedData(courseCompetences,
				loggedUser.getUser());
	}
	
	public void initializeSuggestedByCourse(){
		 if (suggestedByCourse == null) {
	 		logger.debug("Initializing suggested by course");
			 this.loadSuggestedByCourse();

	 	}
	}
	
	public void addSuggestedByColleagues(RecommendationData recommendation) {
		if (!this.suggestedByColleagues.contains(recommendation)) {
			this.suggestedByColleagues.push(recommendation);
		}
	}
	
	public void removeSuggestedResource(RecommendationData recommendationData) {
		RecommendationType rType = recommendationData.getRecommendationType();
		Node node = (Node) recommendationData.getResource();
		
		if (rType.equals(RecommendationType.USER)) {
			ListIterator<RecommendationData> suggestedByColleaguesIterator = this.suggestedByColleagues.listIterator();
			
			while (suggestedByColleaguesIterator.hasNext()) {
				RecommendationData rData = suggestedByColleaguesIterator.next();
				
				if (rData.getResource().equals(node)) {
					suggestedByColleaguesIterator.remove();
				}
			}
		} else if (rType.equals(RecommendationType.SYSTEM)) {
			ListIterator<RecommendationData> suggestedBySystemIterator = this.suggestedBySystem.listIterator();
			
			while (suggestedBySystemIterator.hasNext()) {
				RecommendationData rData = suggestedBySystemIterator.next();
				
				if (rData.getResource().equals(node)) {
					suggestedBySystemIterator.remove();
				}
			}
		}
	}
	
	public boolean hasMoreSuggestedByColleagues(){
		return numberOfSuggestedByColleagues>elementsPerPage;
	}
	
	public void dismissRecommendation(RecommendationData recommendationData) {
		SearchSuggestedLearningBean searchSuggestedLearningBean = (SearchSuggestedLearningBean) applicationBean.getUserSession(loggedUser.getUser().getId()).getAttribute("searchSuggestedLearningBean");
		
		if (searchSuggestedLearningBean != null) {
			searchSuggestedLearningBean.removeSuggestedRecommendation(recommendationData);
		}
		
		removeSuggestedResource(recommendationData);
		recommendationManager.dismissRecommendation(recommendationData, loggedUser.getUser());
	}

	/*
	 * GETTERS / SETTERS
	 */
	public int getRefreshRate() {
		return Settings.getInstance().config.application.suggestedLearningRefreshRate;
	}

	public LinkedList<RecommendationData> getSuggestedByColleagues() {
		return suggestedByColleagues;
	}
	
	public List<RecommendationData> getSuggestedBySystem() {
		return suggestedBySystem;
	}
	
}
