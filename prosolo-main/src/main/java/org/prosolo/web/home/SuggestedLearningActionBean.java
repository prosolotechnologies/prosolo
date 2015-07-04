package org.prosolo.web.home;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.NodeRecommendationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.search.SearchSuggestedLearningBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
@author "Zoran Jeremic"
@date 2013-06-08
 */

@ManagedBean(name = "suggestedLearningActionBean")
@Component("suggestedLearningActionBean")
@Scope("view")
@Deprecated
public class SuggestedLearningActionBean  implements Serializable {

	private static final long serialVersionUID = 4433005572166326551L;

	private static Logger logger = Logger.getLogger(SuggestedLearningActionBean.class);
	
	@Autowired private SuggestedLearningBean suggestedLearningBean;
	@Autowired private SearchSuggestedLearningBean searchSuggestedLearningBean;
	@Autowired private NodeRecommendationManager recommendationManager;
	@Autowired private LoggedUserBean loggedUser;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	// TODO: move to SearchSuggestedLearningBean
//	public void updateSelectedFilter(String hiddenFilter){
//		if (hiddenFilter.equals(searchSuggestedLearningBean.getFilterName(SuggestedLearningFilterType.ALL))) {
//			searchSuggestedLearningBean.updateChosenFilter(SuggestedLearningFilterType.ALL);
//		} else if (hiddenFilter.equals(searchSuggestedLearningBean.getFilterName(SuggestedLearningFilterType.COLLEAGUES))) {
//			searchSuggestedLearningBean.updateChosenFilter(SuggestedLearningFilterType.COLLEAGUES);
//		} else if (hiddenFilter.equals(searchSuggestedLearningBean.getFilterName(SuggestedLearningFilterType.SYSTEM))) {
//			searchSuggestedLearningBean.updateChosenFilter(SuggestedLearningFilterType.SYSTEM);
//		}
//	}

	// TODO: move to SearchSuggestedLearningBean
//	public void dismissRecommendation(RecommendationData recommendationData) {
//		searchSuggestedLearningBean.removeSuggestedRecommendation(recommendationData);
//		suggestedLearningBean.removeSuggestedResource(recommendationData);
//		recommendationManager.dismissRecommendation(recommendationData, loggedUser.getUser());
//	}

}
