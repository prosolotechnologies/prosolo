/**
 * 
 */
package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchFilteredResponse;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
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

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ManagedBean(name = "credentialMembersBean")
@Component("credentialMembersBean")
@Scope("view")
public class CredentialMembersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4836624880668757356L;

	private static Logger logger = Logger.getLogger(CredentialMembersBean.class);

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private UserTextSearch userTextSearch;
	@Inject
	private CredentialManager credManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private StudentEnrollBean studentEnrollBean;
	@Inject
	private AssignStudentToInstructorDialogBean assignStudentToInstructorDialogBean;

	private List<StudentData> members;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private CredentialMembersSortOption sortOption = CredentialMembersSortOption.STUDENT_NAME;
	private PaginationData paginationData = new PaginationData();
	private CredentialMembersSearchFilter searchFilter;
	
	private long personalizedForUserId = -1;

	private String context;
	
	private CredentialIdData credentialIdData;
	
	private CredentialMembersSearchFilter[] searchFilters;
	private CredentialMembersSortOption[] sortOptions;
	
	private ResourceAccessData access;

	public void init() {
		sortOptions = CredentialMembersSortOption.values();
		CredentialMembersSearchFilter.SearchFilter[] values = CredentialMembersSearchFilter.SearchFilter.values();
		int size = values.length;
		searchFilters = new CredentialMembersSearchFilter[size];
		for(int i = 0; i < size; i++) {
			CredentialMembersSearchFilter filter = new CredentialMembersSearchFilter(values[i], 0);
			searchFilters[i] = filter;
		}
		searchFilter = new CredentialMembersSearchFilter(CredentialMembersSearchFilter.SearchFilter.All, 0);
		//searchFilters = InstructorAssignFilterValue.values();
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId + "|context:/name:STUDENTS/";
			try {
				credentialIdData = credManager.getCredentialIdData(decodedId, CredentialType.Delivery);
				if (credentialIdData != null) {
					//user needs instruct or edit privilege to be able to access this page
					access = credManager.getResourceAccessData(decodedId, loggedUserBean.getUserId(),
							ResourceAccessRequirements.of(AccessMode.MANAGER)
													  .addPrivilege(UserGroupPrivilege.Instruct)
													  .addPrivilege(UserGroupPrivilege.Edit));
					if (!access.isCanAccess()) {
						PageUtil.accessDenied();
					} else {
						/*
						 * if user can't edit resource, it means that he can only instruct and that is why
						 * he can only see his students
						 */
						if(!access.isCanEdit()) {
							personalizedForUserId = loggedUserBean.getUserId();
						}
						searchCredentialMembers();
						studentEnrollBean.init(decodedId, context);
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
	
	public void resetAndSearch() {
		this.paginationData.setPage(1);
		searchCredentialMembers();
	}

	public void getCredentialMembers() {
		TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilter.SearchFilter> searchResponse =
				userTextSearch.searchCredentialMembers(
					loggedUserBean.getOrganizationId(),
					searchTerm,
					searchFilter.getFilter(),
					this.paginationData.getPage() - 1, this.paginationData.getLimit(),
					decodedId, personalizedForUserId, sortOption);

		this.paginationData.update((int) searchResponse.getHitsNumber());
		members = searchResponse.getFoundNodes();

		for(CredentialMembersSearchFilter filter : searchFilters) {
			filter.setNumberOfResults(searchResponse.getNumberOfResultsForFilter(filter.getFilter()));
		}
		searchFilter.setNumberOfResults(searchResponse.getNumberOfResultsForFilter(searchFilter.getFilter()));
	}
	
	public void addStudentsAndResetData() {
		studentEnrollBean.enrollStudents();
		this.paginationData.setPage(1);
		searchTerm = "";
		sortOption = CredentialMembersSortOption.DATE;
		members = credManager.getCredentialStudentsData(decodedId, this.paginationData.getLimit());
		searchFilters = credManager.getFiltersWithNumberOfStudentsBelongingToEachCategory(decodedId);
		for (CredentialMembersSearchFilter f : searchFilters) {
			if (f.getFilter() == CredentialMembersSearchFilter.SearchFilter.All) {
				searchFilter = f;
				this.paginationData.update((int) f.getNumberOfResults());
				break;
			}
		}
	}


	private void updateFiltersStudentAssigned() {
		updateFilters(1, -1);
	}

	private void updateFiltersStudentUnassigned() {
		updateFilters(-1, 1);
	}

	/**
	 * This method is called after student has chosen an instructor from the modal (it this option is enabled for
	 * the delivery)
	 */
	public void updateAfterInstructorIsAssigned() {
		InstructorData instructor = assignStudentToInstructorDialogBean.getInstructor();
		UserData student = assignStudentToInstructorDialogBean.getStudentToAssignInstructor();

		Optional<StudentData> updatedStudent = members.stream().filter(s -> s.getUser().getId() == student.getId())
				.findAny();

		if (updatedStudent.isPresent()) {
			updatedStudent.get().setInstructor(instructor);
		}

		// update filters
		switch (assignStudentToInstructorDialogBean.getLastAction()) {
			case ASSIGNED:
				updateFiltersStudentAssigned();
				break;
			case UNASSIGNED:
				updateFiltersStudentUnassigned();
				break;
		}
	}

	private void updateFilters(int numberOfAssignedToAdd, int numberOfUnassignedToAdd) {
		updateFilterResultNumber(CredentialMembersSearchFilter.SearchFilter.Assigned, numberOfAssignedToAdd);
		updateFilterResultNumber(CredentialMembersSearchFilter.SearchFilter.Unassigned, numberOfUnassignedToAdd);
	}

	private void updateFilterResultNumber(CredentialMembersSearchFilter.SearchFilter filter, int numberToAdd) {
		CredentialMembersSearchFilter searchFilter = Arrays.stream(searchFilters).filter(f -> f.getFilter() == filter).findFirst().get();
		searchFilter.setNumberOfResults(searchFilter.getNumberOfResults() + numberToAdd);
	}

	public void applySearchFilter(CredentialMembersSearchFilter filter) {
		this.searchFilter = filter;
		this.paginationData.setPage(1);
		searchCredentialMembers();
	}
	
	public void applySortOption(CredentialMembersSortOption sortOption) {
		this.sortOption = sortOption;
		this.paginationData.setPage(1);
		searchCredentialMembers();
	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchCredentialMembers();
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

	@Override
	public PaginationData getPaginationData() {
		return paginationData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public CredentialMembersSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CredentialMembersSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}

	public String getCredentialTitle() {
		return credentialIdData.getTitle();
	}

	public CredentialMembersSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(CredentialMembersSearchFilter[] searchFilters) {
		this.searchFilters = searchFilters;
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

	public CredentialIdData getCredentialIdData() {
		return credentialIdData;
	}
}
