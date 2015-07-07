package org.prosolo.web.search;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.recommendation.SuggestedLearningService;
import org.prosolo.services.nodes.NodeRecommendationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.SuggestedLearningBean;
import org.prosolo.web.home.data.RecommendationData;
import org.prosolo.web.home.util.RecommendationConverter;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Zoran Jeremic"
 * @date 2013-04-18
 */
@ManagedBean(name = "searchSuggestedLearningBean")
@Component("searchSuggestedLearningBean")
@Scope("view")
public class SearchSuggestedLearningBean implements Serializable {

	private static final long serialVersionUID = -1752310276912171163L;
	
	private static Logger logger = Logger.getLogger(SearchSuggestedLearningBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private SuggestedLearningService suggestedLearningService;
	@Autowired private RecommendationConverter recommendationConverter;
	
	@Autowired private SuggestedLearningBean suggestedLearningBean;
	@Autowired private NodeRecommendationManager recommendationManager;
	//@Autowired private SuggestedLearningActionBean suggestedLearningActionBean;

	// private List<Recommendation> suggestedByColleagues;
	// private List<Node> suggestedBySystem;
	private List<RecommendationData> suggestedResources = new LinkedList<RecommendationData>();
	private List<RecommendationData> filteredResources = new LinkedList<RecommendationData>();
	
	private SuggestedLearningFilterType chosenFilter = SuggestedLearningFilterType.ALL;

	private int page = 0;
	private final int elementsPerPage = 7;

	public void init() {
		logger.debug("initializing");
		
		List<Recommendation> colleguesRecommendations = suggestedLearningService.findSuggestedLearningResourcesByCollegues(
					loggedUser.getUser(), 
					RecommendationType.USER,
					page, elementsPerPage);
		
		LinkedList<RecommendationData> colleguesRecData = recommendationConverter.convertRecommendationsToRecommendedData(colleguesRecommendations);
		suggestedResources.addAll(colleguesRecData);
		
		List<Node> nodesSuggestedBySystem = suggestedLearningService.findSuggestedLearningResourcesBySystem(loggedUser.getUser(), elementsPerPage);
		List<RecommendationData> systemRecommendation=recommendationConverter.convertNodesToRecommendedData(nodesSuggestedBySystem, loggedUser.getUser());
		suggestedResources.addAll(systemRecommendation);
		
		updateChosenFilter(chosenFilter);
	}
 
	public void updateChosenFilter(SuggestedLearningFilterType filter) {
		filteredResources.clear();
		
		if (chosenFilter.equals(SuggestedLearningFilterType.ALL)) {
			this.filteredResources.addAll(this.suggestedResources);
		} else if (chosenFilter.equals(SuggestedLearningFilterType.COLLEAGUES)) {
			for (RecommendationData recData : this.suggestedResources) {
				if (recData.getRecommendationType().equals(RecommendationType.USER)) {
					this.filteredResources.add(recData);
				}
			}
		} else if (chosenFilter.equals(SuggestedLearningFilterType.SYSTEM)) {
			for (RecommendationData recData : this.suggestedResources) {
				if (recData.getRecommendationType().equals(RecommendationType.SYSTEM)) {
					this.filteredResources.add(recData);
				}
			}
		} else if (chosenFilter.equals(SuggestedLearningFilterType.COURSES)) {
			for (RecommendationData recData : this.suggestedResources) {
				if (recData.getRecommendationType().equals(RecommendationType.COURSE)) {
					this.filteredResources.add(recData);
				}
			}
		}
	}
	
	public void updateBasedOnSelectedFilter(){
		updateChosenFilter(chosenFilter);
	}

	public void dismissRecommendation(RecommendationData recommendationData) {
		removeSuggestedRecommendation(recommendationData);
		suggestedLearningBean.removeSuggestedResource(recommendationData);
		recommendationManager.dismissRecommendation(recommendationData, loggedUser.getUser());
	}

	/*
	 * GETTERS / SETTERS
	 */
	public void setChosenFilter(SuggestedLearningFilterType filterType) {
		this.chosenFilter = filterType;
	}

	public SuggestedLearningFilterType getChosenFilter() {
		return chosenFilter;
	}

	public List<RecommendationData> getSuggestedResource() {
		return suggestedResources;
	}

	public void setSuggestedResource(List<RecommendationData> suggestedResource) {
		this.suggestedResources = suggestedResource;
	}
	
	public void removeSuggestedRecommendation(RecommendationData recommendationData) {
		if (this.suggestedResources.contains(recommendationData)) {
			Iterator<RecommendationData> iterator = suggestedResources.iterator();
			
			while (iterator.hasNext()) {
				RecommendationData recData = (RecommendationData) iterator.next();
				
				if (recData.equals(recommendationData)) {
					iterator.remove();
					break;
				}
			}
		}
		if (this.filteredResources.contains(recommendationData)) {
			Iterator<RecommendationData> iterator = filteredResources.iterator();
			
			while (iterator.hasNext()) {
				RecommendationData recData = (RecommendationData) iterator.next();
				
				if (recData.equals(recommendationData)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	public String getFilterString() {
		 return getFilterName(chosenFilter);
	}

	public String getFilterName(SuggestedLearningFilterType filter) {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		String filterString = "";
		try {
			filterString = ResourceBundleUtil.getMessage(
				"search.suggestedLearning.filter."+filter.toString().toLowerCase(), 
				locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		return filterString;
	}
	
	public void setFilterString(String filterString) {
		if (filterString != null && !filterString.equals("")) {
			try {
				chosenFilter = SuggestedLearningFilterType.valueOf(filterString.toUpperCase());
				return;
			} catch (IllegalArgumentException e) { }
		}
		chosenFilter = SuggestedLearningFilterType.ALL;
	}

	public List<RecommendationData> getFilteredResources() {
		return filteredResources;
	}

	public void setFilteredResources(List<RecommendationData> filteredResources) {
		this.filteredResources = filteredResources;
	}
}
