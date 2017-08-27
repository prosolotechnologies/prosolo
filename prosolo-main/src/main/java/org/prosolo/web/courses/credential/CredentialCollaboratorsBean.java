/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.LearningStatus;
import org.prosolo.search.util.credential.LearningStatusFilter;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.people.PeopleActionBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "credentialCollaboratorsBean")
@Component("credentialCollaboratorsBean")
@Scope("view")
public class CredentialCollaboratorsBean implements Serializable, Paginable {

	private static final long serialVersionUID = 8110468361441307656L;

	private static Logger logger = Logger.getLogger(CredentialCollaboratorsBean.class);

	private List<StudentData> members;

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject private UserTextSearch userTextSearch;
	@Inject
	private CredentialManager credManager;
	@Inject 
	private LoggedUserBean loggedUserBean;
	@Inject
	private PeopleActionBean peopleActionBean;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private CredentialMembersSortOption sortOption = CredentialMembersSortOption.DATE;
	private PaginationData paginationData = new PaginationData();
	private LearningStatusFilter learningStatusFilter;
	
	private String credentialTitle;
	
	private LearningStatusFilter[] searchFilters;
	private CredentialMembersSortOption[] sortOptions;

	public void init() {
		sortOptions = CredentialMembersSortOption.values();
		learningStatusFilter = new LearningStatusFilter(LearningStatus.All, 0);
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				credentialTitle = credManager.getCredentialTitle(decodedId);
				searchCredentialMembers();
				if(searchFilters == null) {
					LearningStatus[] values = LearningStatus.values();
					int size = values.length;
					searchFilters = new LearningStatusFilter[size];
					for(int i = 0; i < size; i++) {
						LearningStatusFilter filter = new LearningStatusFilter(values[i], 0);
						searchFilters[i] = filter;
					}
				}
			} catch (Exception e) {
				PageUtil.fireErrorMessage(e.getMessage());
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	public void searchCredentialMembers() {
		try {
			if (members != null) {
				this.members.clear();
			}

			getCredentialMembers();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public void followOrUnfollowUser(UserData user) {
		if(user.isFollowedByCurrentUser()) {
			unfollowUser(user);
		} else {
			followUser(user);
		}
	}
	
	private void followUser(UserData user) {
		try {
			peopleActionBean.followUserById(user.getId());
			user.setFollowedByCurrentUser(true);
			PageUtil.fireSuccessfulInfoMessage("You are following " + user.getFullName());
		} catch(EntityAlreadyExistsException ex) {
			PageUtil.fireErrorMessage("You are already following " + user.getFullName());
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error following " + user.getFullName() + ". Please try again");
		}
	}

	private void unfollowUser(UserData user) {
		try {
			peopleActionBean.unfollowUserById(user.getId());
			user.setFollowedByCurrentUser(false);
		} catch (EventException e) {
			logger.error(e);
		}

		PageUtil.fireSuccessfulInfoMessage("You are not following " + user.getFullName());
	}
	
	public void resetAndSearch() {
		paginationData.setPage(1);
		searchCredentialMembers();
	}

	public void getCredentialMembers() {
		PaginatedResult<StudentData> searchResponse = userTextSearch
				.searchCredentialMembersWithLearningStatusFilter(
						loggedUserBean.getOrganizationId(),
						searchTerm, 
						learningStatusFilter.getStatus(), 
						paginationData.getPage() - 1, 
						paginationData.getLimit(), 
						decodedId, 
						loggedUserBean.getUserId(), 
						sortOption);

		paginationData.update((int) searchResponse.getHitsNumber());
		members = searchResponse.getFoundNodes();
		Map<String, Object> additional = searchResponse.getAdditionalInfo();
		if(additional != null) {
			searchFilters = (LearningStatusFilter[]) additional.get("filters");
			learningStatusFilter = (LearningStatusFilter) additional.get("selectedFilter");
		}
	}
	
	public void applySearchFilter(LearningStatusFilter filter) {
		this.learningStatusFilter = filter;
		paginationData.setPage(1);
		searchCredentialMembers();
	}
	
	public void applySortOption(CredentialMembersSortOption sortOption) {
		this.sortOption = sortOption;
		paginationData.setPage(1);
		searchCredentialMembers();
	}
	
	@Override
	public void changePage(int page) {
		if(paginationData.getPage() != page) {
			paginationData.setPage(page);
			searchCredentialMembers();
		}
	}
	
//	public void setSortByStudentName() {
//		setSortField(CredentialMembersSortField.STUDENT_NAME);
//	}
//	
//	public void setSortByCourseProgress() {
//		setSortField(CredentialMembersSortField.PROGRESS);
//	}
//	
//	public void setSortByProfileType() {
//		setSortField(CredentialMembersSortField.PROFILE_TYPE);
//	}
	
//	public void setSortField(CredentialMembersSortField field) {
//		if(sortField == field) {
//			changeSortOrder();
//		} else {
//			sortField = field;
//			sortOrder = SortingOption.ASC;
//		}
//		page = 1;
//	}
	
//	private void changeSortOrder() {
//		if(sortOrder == SortingOption.ASC) {
//			sortOrder = SortingOption.DESC;
//		} else {
//			sortOrder = SortingOption.ASC;
//		}
//		
//	}
	
//	public void resetSearchOptions() {
//		this.page = 1;
//		resetSortOptions();	
//	}
	
//	public void resetSortOptions() {
//		this.sortField = CredentialMembersSortField.STUDENT_NAME;
//		this.sortOrder = SortingOption.ASC;
//	}
//	
//	public boolean isSortByStudent() {
//		return sortField == CredentialMembersSortField.STUDENT_NAME;
//	}
//	
//	public boolean isSortByCourseProgress() {
//		return sortField == CredentialMembersSortField.PROGRESS;
//	}
//	
//	public boolean isSortByProfileType() {
//		return sortField == CredentialMembersSortField.PROFILE_TYPE;
//	}
//	
//	public boolean isASCOrder() {
//		return sortOrder == SortingOption.ASC;
//	}

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

	public List<StudentData> getMembers() {
		return members;
	}

	public void setMembers(List<StudentData> members) {
		this.members = members;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public CredentialMembersSortOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(CredentialMembersSortOption sortOption) {
		this.sortOption = sortOption;
	}

	public CredentialMembersSortOption[] getSortOptions() {
		return sortOptions;
	}

	public void setSortOptions(CredentialMembersSortOption[] sortOptions) {
		this.sortOptions = sortOptions;
	}

	public LearningStatusFilter getLearningStatusFilter() {
		return learningStatusFilter;
	}

	public void setLearningStatusFilter(LearningStatusFilter learningStatusFilter) {
		this.learningStatusFilter = learningStatusFilter;
	}

	public LearningStatusFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(LearningStatusFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}
	
}
