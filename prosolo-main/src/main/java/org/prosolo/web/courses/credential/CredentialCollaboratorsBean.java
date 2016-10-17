/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.LearningStatus;
import org.prosolo.search.util.credential.LearningStatusFilter;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.people.PeopleActionBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialCollaboratorsBean")
@Component("credentialCollaboratorsBean")
@Scope("view")
public class CredentialCollaboratorsBean implements Serializable, Paginable {

	private static final long serialVersionUID = 8110468361441307656L;

	private static Logger logger = Logger.getLogger(CredentialCollaboratorsBean.class);

	private List<StudentData> members;

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private TextSearch textSearch;
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
	private int credentialMembersNumber;
	private int page = 1;
	private int limit = 10;
	private CredentialMembersSortOption sortOption = CredentialMembersSortOption.DATE;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;
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
			generatePagination();
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
			PageUtil.fireSuccessfulInfoMessage("Started following " + user.getFullName() + ".");
		} catch(EntityAlreadyExistsException ex) {
			PageUtil.fireErrorMessage("You are already following " + user.getFullName());
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error occured. Please try again");
		}
	}

	private void unfollowUser(UserData user) {
		try {
			peopleActionBean.unfollowUserById(user.getId());
			user.setFollowedByCurrentUser(false);
		} catch (EventException e) {
			logger.error(e);
		}

		PageUtil.fireSuccessfulInfoMessage("Stopped following " + user.getFullName() + ".");
	}
	
	public void resetAndSearch() {
		this.page = 1;
		searchCredentialMembers();
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(credentialMembersNumber, limit, page, 
				1, "...");
		//if we want to genearte all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}

	public void getCredentialMembers() {
		TextSearchResponse1<StudentData> searchResponse = textSearch
				.searchCredentialMembersWithLearningStatusFilter(
						searchTerm, 
						learningStatusFilter.getStatus(), 
						page - 1, 
						limit, 
						decodedId, 
						loggedUserBean.getUserId(), 
						sortOption);

		credentialMembersNumber = (int) searchResponse.getHitsNumber();
		members = searchResponse.getFoundNodes();
		Map<String, Object> additional = searchResponse.getAdditionalInfo();
		if(additional != null) {
			searchFilters = (LearningStatusFilter[]) additional.get("filters");
			learningStatusFilter = (LearningStatusFilter) additional.get("selectedFilter");
		}
	}
	
	public void applySearchFilter(LearningStatusFilter filter) {
		this.learningStatusFilter = filter;
		this.page = 1;
		searchCredentialMembers();
	}
	
	public void applySortOption(CredentialMembersSortOption sortOption) {
		this.sortOption = sortOption;
		this.page = 1;
		searchCredentialMembers();
	}
	
	@Override
	public boolean isCurrentPageFirst() {
		return page == 1 || numberOfPages == 0;
	}
	
	@Override
	public boolean isCurrentPageLast() {
		return page == numberOfPages || numberOfPages == 0;
	}
	
	@Override
	public void changePage(int page) {
		if(this.page != page) {
			this.page = page;
			searchCredentialMembers();
		}
	}

	@Override
	public void goToPreviousPage() {
		changePage(page - 1);
	}

	@Override
	public void goToNextPage() {
		changePage(page + 1);
	}

	@Override
	public boolean isResultSetEmpty() {
		return credentialMembersNumber == 0;
	}
	
	@Override
	public boolean shouldBeDisplayed() {
		return numberOfPages > 1;
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

	public int getCredentialMembersNumber() {
		return credentialMembersNumber;
	}

	public void setCourseMembersNumber(int credentialMembersNumber) {
		this.credentialMembersNumber = credentialMembersNumber;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
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
