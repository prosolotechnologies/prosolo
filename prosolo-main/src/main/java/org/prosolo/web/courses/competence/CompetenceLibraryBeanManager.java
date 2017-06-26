/**
 * 
 */
package org.prosolo.web.courses.competence;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.event.EventException;
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

@Component("competenceLibraryBeanManager")
@Scope("view")
public class CompetenceLibraryBeanManager implements Serializable, Paginable {

	private static final long serialVersionUID = 2945077443985779948L;

	private static Logger logger = Logger.getLogger(CompetenceLibraryBeanManager.class);

	@Inject private CompetenceTextSearch textSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private LoggingService loggingService;
	@Inject private Competence1Manager compManager;
	@Inject private UrlIdEncoder idEncoder;

	private List<CompetenceData1> competences;
	
	//search
	private String searchTerm = "";
	private CompetenceSearchFilter searchFilter = CompetenceSearchFilter.ACTIVE;
	private LearningResourceSortOption sortOption = LearningResourceSortOption.ALPHABETICALLY;
	private PaginationData paginationData = new PaginationData();
	
	private LearningResourceSortOption[] sortOptions;
	private CompetenceSearchFilter[] searchFilters;
	
	private CompetenceData1 selectedComp;
	
	private String context = "name:library";

	public void init() {
		sortOptions = LearningResourceSortOption.values();
		searchFilters = CompetenceSearchFilter.values();
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

	public void prepareComp(CompetenceData1 comp) {
		this.selectedComp = comp;
	}

	public void getCompetenceSearchResults() {
		PaginatedResult<CompetenceData1> response = textSearch.searchCompetencesForManager(
				searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), loggedUserBean.getUserId(), 
				searchFilter, sortOption);
	
		paginationData.update((int) response.getHitsNumber());
		competences = response.getFoundNodes();
	}
	
	public void applySearchFilter(CompetenceSearchFilter filter) {
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
	
	public void archive() {
		if(selectedComp != null) {
			LearningContextData ctx = PageUtil.extractLearningContextData();
			boolean archived = false;
			try {
				compManager.archiveCompetence(selectedComp.getCompetenceId(), loggedUserBean.getUserId(), ctx);
				archived = true;
				searchTerm = null;
				paginationData.setPage(1);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while trying to archive competence");
			}
			if(archived) {
				try {
					reloadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage("Competency archived successfully");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error while refreshing data");
				}
			}
		}
	}
	
	public void restore() {
		if(selectedComp != null) {
			LearningContextData ctx = PageUtil.extractLearningContextData();
			boolean success = false;
			try {
				compManager.restoreArchivedCompetence(selectedComp.getCompetenceId(), loggedUserBean.getUserId(), ctx);
				success = true;
				searchTerm = null;
				paginationData.setPage(1);
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while trying to restore competency");
			}
			if(success) {
				try {
					reloadDataFromDB();
					PageUtil.fireSuccessfulInfoMessage("Competency restored successfully");
				} catch(DbConnectionException e) {
					logger.error(e);
					PageUtil.fireErrorMessage("Error while refreshing data");
				}
			}
		}
	}
	
	private void reloadDataFromDB() {
		paginationData.update((int) compManager.countNumberOfCompetences(searchFilter, 
				loggedUserBean.getUserId(), UserGroupPrivilege.Edit));
		competences = compManager.searchCompetencesForManager(searchFilter, paginationData.getLimit(), 
				paginationData.getPage() - 1, sortOption, loggedUserBean.getUserId());
	}
	
	public void duplicate() {
		if(selectedComp != null) {
			LearningContextData ctx = PageUtil.extractLearningContextData();
			try {
				long compId = compManager.duplicateCompetence(selectedComp.getCompetenceId(), 
						loggedUserBean.getUserId(), ctx);
				ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
				try {
					extContext.redirect(extContext.getRequestContextPath() + "/manage/competences/" 
							+ idEncoder.encodeId(compId) + "/edit");
				} catch (IOException e) {
					logger.error(e);
				}
			} catch(DbConnectionException e) {
				logger.error(e);
				PageUtil.fireErrorMessage("Error while trying to duplicate competence");
			} catch(EventException ee) {
				logger.error(ee);
			}
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

	public CompetenceSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CompetenceSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}

	public LearningResourceSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(LearningResourceSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

	public CompetenceSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(CompetenceSearchFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	public CompetenceData1 getSelectedComp() {
		return selectedComp;
	}

	
}
