/**
 * 
 */
package org.prosolo.web.courses.competence;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CompetenceSearchConfig;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

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
	@Inject private RoleManager roleManager;
	@Inject private UnitManager unitManager;

	@Getter @Setter
	private List<CompetenceData1> competences;
	@Getter @Setter
	private LearningResourceSearchFilter searchFilter = LearningResourceSearchFilter.ALL;
	@Getter
	private LearningResourceSearchFilter[] searchFilters;

	//search
	@Getter @Setter
	private String searchTerm = "";
	private PaginationData paginationData = new PaginationData();

	private String context = "name:library";
	private List<Long> unitIds = new ArrayList<>();

	private final CompetenceSearchConfig config = CompetenceSearchConfig.of(
			true, true, false, true, LearningResourceType.USER_CREATED);

	public void init() {
		searchFilters = Arrays.stream(LearningResourceSearchFilter.values()).filter(
				f -> f != LearningResourceSearchFilter.BY_STUDENTS &&
					 f != LearningResourceSearchFilter.YOUR_CREDENTIALS)
				.toArray(LearningResourceSearchFilter[]::new);

		try {
			Long userRoleId = roleManager.getRoleIdByName(SystemRoleNames.USER);
			unitIds = unitManager.getUserUnitIdsInRole(loggedUserBean.getUserId(), userRoleId);

			searchCompetences(false);
		} catch (DbConnectionException e) {
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}

	public void searchCompetences(boolean userSearch) {
		try {
			getCompetenceSearchResults();
			
			if(userSearch) {
				String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
				PageContextData lcd = new PageContextData(page, context, null);
				Map<String, String> params = new HashMap<>();
				params.put("query", searchTerm);
				try {
					loggingService.logServiceUse(loggedUserBean.getUserContext(lcd), ComponentName.SEARCH_COMPETENCES,
							null, params, loggedUserBean.getIpAddress());
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
				loggedUserBean.getOrganizationId(), searchTerm, paginationData.getPage() - 1,
				paginationData.getLimit(), loggedUserBean.getUserId(), unitIds, searchFilter, config);
	
		paginationData.update((int) response.getHitsNumber());
		competences = response.getFoundNodes();
	}
	
	public void applySearchFilter(LearningResourceSearchFilter filter) {
		this.searchFilter = filter;
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
			compManager.enrollInCompetence(comp.getCredentialId(), comp.getCompetenceId(), loggedUserBean.getUserId(), loggedUserBean.getUserContext());

			PageUtil.redirect("/credentials/" + idEncoder.encodeId(comp.getCredentialId()) + "/competences/" + idEncoder.encodeId(comp.getCompetenceId()) + "?justEnrolled=true");
		} catch(DbConnectionException e) {
			logger.error("Error", e);
			PageUtil.fireErrorMessage("Error enrolling in a " + ResourceBundleUtil.getMessage("label.competence").toLowerCase());
		}
	}

}
