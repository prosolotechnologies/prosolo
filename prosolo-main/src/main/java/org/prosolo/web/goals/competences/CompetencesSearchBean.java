package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.ResourceUtilBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "competencesSearchBean")
@Component("competencesSearchBean")
@Scope("view")
public class CompetencesSearchBean implements Serializable {

	private static final long serialVersionUID = 5631214388913695909L;

	protected static Logger logger = Logger.getLogger(CompetencesSearchBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private TextSearch textSearch;
	@Autowired private CompetenceManager compManager;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private LearningGoalsBean goalBean;

	private Collection<Competence> foundCompetences;
	private int foundCompetencesSize;
	private String lastSearch = "";

	public CompetencesSearchBean() {
		foundCompetences = new ArrayList<Competence>();
	}
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	public void searchCompetencesListener(ValueChangeEvent event) {
		lastSearch = event.getNewValue().toString();
		foundCompetences.clear();

		if (!lastSearch.isEmpty() && (lastSearch != null)) {
//			searchCompetences(lastSearch, getCompetencesForSelectedGoal());
			searchCompetences(lastSearch, null);
		}
	}

//	private List<Competence> getCompetencesForSelectedGoal() {
//		List<Competence> competences = new ArrayList<Competence>();
//
//		if (goalBean != null && goalBean.getSelectedGoalData() != null && goalBean.getSelectedGoalData().getData() != null) {
//			Collection<TargetCompetence> tCompetences = compManager.merge(goalBean.getSelectedGoalData().getGoal()).getTargetCompetences();
//	
//			for (TargetCompetence tc : tCompetences) {
//				if (!competences.contains(tc.getCompetence())) {
//					competences.add(tc.getCompetence());
//				}
//			}
//		}
//		return competences;
//	}

	@SuppressWarnings("unchecked")
	private void searchCompetences(String searchWord, List<Competence> competencesToExclude) {
		TextSearchResponse searchResponse = textSearch.searchCompetences(
			searchWord,
			0,
			Settings.getInstance().config.application.defaultSearchItemsNumber,
			false,
			ResourceUtilBean.convertToIdArray(competencesToExclude),
			null,
			SortingOption.ASC);
		
		foundCompetences = (Collection<Competence>) searchResponse.getFoundNodes();
		this.setFoundCompetencesSize((int) searchResponse.getHitsNumber());

	}

	public boolean hasMoreCompetences() {
		return foundCompetences.size() > Settings.getInstance().config.application.defaultSearchItemsNumber + 1;
	}

	public String getCompetenceUsersNumber(Competence competence) {
		long numberOfUsers = portfolioManager.getNumberOfUsersHavingCompetences(competence);

		if (numberOfUsers == 0) {
			return "0 members";
		} else if (numberOfUsers == 1) {
			return "1 member";
		} else {
			return numberOfUsers + " members";
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public Collection<Competence> getFoundCompetences() {
		return foundCompetences;
	}

	public int getFoundCompetencesSize() {
		return foundCompetencesSize;
	}

	public void setFoundCompetencesSize(int foundCompetencesSize) {
		this.foundCompetencesSize = foundCompetencesSize;
	}

	public String getLastSearch() {
		return lastSearch;
	}

}
