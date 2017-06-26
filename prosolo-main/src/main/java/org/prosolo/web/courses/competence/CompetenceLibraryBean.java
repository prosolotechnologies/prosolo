/**
 * 
 */
package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CompetenceSearchConfig;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("competenceLibraryBean")
@Scope("view")
public class CompetenceLibraryBean implements Serializable, Paginable {

	private static final long serialVersionUID = -759648446829569092L;

	private static Logger logger = Logger.getLogger(CompetenceLibraryBean.class);

	@Inject private CompetenceTextSearch textSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private LoggingService loggingService;
	@Inject private Competence1Manager compManager;
	@Inject private UrlIdEncoder idEncoder;

	private List<CompetenceData1> competences;
	
	//search
	private String searchTerm = "";
	private LearningResourceSearchFilter searchFilter = LearningResourceSearchFilter.ALL;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private LearningResourceSortOption[] sortOptions;
	private LearningResourceSearchFilter[] searchFilters;
	
	private final CompetenceSearchConfig config = CompetenceSearchConfig.of(
			true, true, false, true, LearningResourceType.USER_CREATED);

	private String context = "name:library";

	public void init() {
		sortOptions = LearningResourceSortOption.values();
		searchFilters = Arrays.stream(LearningResourceSearchFilter.values()).filter(
				f -> f != LearningResourceSearchFilter.BY_STUDENTS &&
					 f != LearningResourceSearchFilter.YOUR_CREDENTIALS)
				.toArray(LearningResourceSearchFilter[]::new);
		searchCompetences(false);
	}

	public void searchCompetences(boolean userSearch) {
		try {
			getCompetenceSearchResults();
			
			if(userSearch) {
				String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
				LearningContextData lcd = new LearningContextData(page, context, null);
				Map<String, String> params = new HashMap<>();
				params.put("query", searchTerm);
				try {
					loggingService.logServiceUse(loggedUserBean.getUserId(), 
							ComponentName.SEARCH_COMPETENCES, 
							params, loggedUserBean.getIpAddress(), lcd);
				} catch(Exception e) {
					logger.error(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	public void resetAndSearch() {
		paginationData.setPage(1);
		searchCompetences(true);
	}

	public void getCompetenceSearchResults() {
		PaginatedResult<CompetenceData1> response = textSearch.searchCompetences(
				searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), loggedUserBean.getUserId(), 
				searchFilter, sortOption, config);
	
		paginationData.update((int) response.getHitsNumber());
		competences = response.getFoundNodes();
	}
	
	public void applySearchFilter(LearningResourceSearchFilter filter) {
		this.searchFilter = filter;
		paginationData.setPage(1);
		searchCompetences(true);
	}
	
	public void applySortOption(LearningResourceSortOption sortOption) {
		this.sortOption = sortOption;
		paginationData.setPage(1);
		searchCompetences(true);
	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCompetences(true);
		}
	}
	
	@Override
	public PaginationData getPaginationData() {
		return paginationData;
	}
	
	/*
	 * ACTIONS
	 */
	
	public void enrollInCompetence(CompetenceData1 comp) {
		try {
			LearningContextData context = PageUtil.extractLearningContextData();
			
			compManager.enrollInCompetence(comp.getCompetenceId(), loggedUserBean.getUserId(), context);
			
			ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				extContext.redirect(extContext.getRequestContextPath() + 
						"/competences/" + idEncoder.encodeId(comp.getCompetenceId()) + "?justEnrolled=true");
			} catch (IOException e) {
				logger.error(e);
			}
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while enrolling in a competency");
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public LearningResourceSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(LearningResourceSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public List<CompetenceData1> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CompetenceData1> competences) {
		this.competences = competences;
	}

	public LearningResourceSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(LearningResourceSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}

	public LearningResourceSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(LearningResourceSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

	public LearningResourceSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(LearningResourceSearchFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	
}
