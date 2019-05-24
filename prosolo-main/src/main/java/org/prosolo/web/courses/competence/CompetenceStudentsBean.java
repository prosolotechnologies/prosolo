/**
 * 
 */
package org.prosolo.web.courses.competence;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchFilteredResponse;
import org.prosolo.search.util.competences.CompetenceStudentsSearchFilter;
import org.prosolo.search.util.competences.CompetenceStudentsSearchFilterValue;
import org.prosolo.search.util.competences.CompetenceStudentsSortOption;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.user.data.StudentData;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@Component("competenceStudentsBean")
@Scope("view")
public class CompetenceStudentsBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4836624880668757356L;

	private static Logger logger = Logger.getLogger(CompetenceStudentsBean.class);

	private List<StudentData> students;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private UserTextSearch userTextSearch;
	@Inject private Competence1Manager compManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private CredentialManager credManager;

	// PARAMETERS
	private String id;
	private long decodedCompId;
	private String credId;
	private long decodedCredId;

	private String searchTerm = "";
	private CompetenceStudentsSortOption sortOption = CompetenceStudentsSortOption.DATE;
	private PaginationData paginationData = new PaginationData();
	private CompetenceStudentsSearchFilter searchFilter;
	
	private String competenceTitle;
	private CredentialIdData credentialIdData;
	
	private CompetenceStudentsSearchFilter[] searchFilters;
	private CompetenceStudentsSortOption[] sortOptions;
	
	private ResourceAccessData access;

	public void init() {
		sortOptions = CompetenceStudentsSortOption.values();
		CompetenceStudentsSearchFilterValue[] values = CompetenceStudentsSearchFilterValue.values();
		int size = values.length;
		searchFilters = new CompetenceStudentsSearchFilter[size];
		for(int i = 0; i < size; i++) {
			CompetenceStudentsSearchFilter filter = new CompetenceStudentsSearchFilter(
					values[i], 0);
			searchFilters[i] = filter;
		}
		searchFilter = new CompetenceStudentsSearchFilter(CompetenceStudentsSearchFilterValue.ALL, 0);
		//searchFilters = InstructorAssignFilterValue.values();
		decodedCompId = idEncoder.decodeId(id);
		decodedCredId = idEncoder.decodeId(credId);

		if (decodedCompId > 0 && decodedCredId > 0) {
			// check if credential and competency are connected
			compManager.checkIfCompetenceIsPartOfACredential(decodedCredId, decodedCompId);

			try {
				String title = compManager.getCompetenceTitleForCompetenceWithType(
						decodedCompId, LearningResourceType.UNIVERSITY_CREATED);
				this.credentialIdData = credManager.getCredentialIdData(decodedCredId, null);

				if (title != null) {
					ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
						.addPrivilege(UserGroupPrivilege.Edit);
					access = compManager.getResourceAccessData(decodedCompId, loggedUserBean.getUserId(), req);

					if (!access.isCanAccess()) {
						PageUtil.accessDenied();
					} else {
						competenceTitle = title;
						searchCompetenceStudents();
					}
				} else {
					PageUtil.notFound();
				}	
			} catch (Exception e) {
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			PageUtil.notFound();
		}
	}

	public void searchCompetenceStudents() {
		try {
			if (students != null) {
				this.students.clear();
			}

			getCompetenceStudents();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		searchCompetenceStudents();
	}

	public void getCompetenceStudents() {
		TextSearchFilteredResponse<StudentData, CompetenceStudentsSearchFilterValue> searchResponse = 
				userTextSearch.searchCompetenceStudents(
					loggedUserBean.getOrganizationId(),
					searchTerm,
						decodedCompId,
					searchFilter.getFilter(), 
					sortOption, 
					paginationData.getPage() - 1, 
					paginationData.getLimit());

		this.paginationData.update((int) searchResponse.getHitsNumber());
		students = searchResponse.getFoundNodes();
		
		for(CompetenceStudentsSearchFilter filter : searchFilters) {
			filter.setNumberOfResults(searchResponse.getNumberOfResultsForFilter(filter.getFilter()));
		}
		searchFilter.setNumberOfResults(searchResponse.getNumberOfResultsForFilter(searchFilter.getFilter()));
	}
	
	public void applySearchFilter(CompetenceStudentsSearchFilter filter) {
		this.searchFilter = filter;
		this.paginationData.setPage(1);
		searchCompetenceStudents();
	}
	
	public void applySortOption(CompetenceStudentsSortOption sortOption) {
		this.sortOption = sortOption;
		this.paginationData.setPage(1);
		searchCompetenceStudents();
	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCompetenceStudents();
		}
	}
	
	public boolean canEdit() {
		return access != null && access.isCanEdit();
	}


	/*
	 * PARAMETERS
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
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

	@Override
	public PaginationData getPaginationData() {
		return paginationData;
	}

	public long getDecodedCompId() {
		return decodedCompId;
	}

	public List<StudentData> getStudents() {
		return students;
	}

	public CompetenceStudentsSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(CompetenceStudentsSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public CompetenceStudentsSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CompetenceStudentsSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}

	public String getCompetenceTitle() {
		return competenceTitle;
	}

	public CompetenceStudentsSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public CompetenceStudentsSortOption[] getSortOptions() {
		return sortOptions;
	}

	public String getCredId() {
		return credId;
	}

	public void setCredId(String credId) {
		this.credId = credId;
	}

	public String getCredTitle() {
		return credentialIdData.getTitle();
	}

	public CredentialIdData getCredentialIdData() {
		return credentialIdData;
	}
}
