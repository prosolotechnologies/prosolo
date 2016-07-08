/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.InstructorAssignFilter;
import org.prosolo.search.util.credential.InstructorAssignFilterValue;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialMembersBean")
@Component("credentialMembersBean")
@Scope("view")
public class CredentialMembersBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4836624880668757356L;

	private static Logger logger = Logger.getLogger(CredentialMembersBean.class);

	private List<StudentData> members;

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private TextSearch textSearch;
	@Inject 
	private CredentialInstructorManager credInstructorManager;
	@Inject
	private CredentialManager credManager;
	@Inject 
	private LoggedUserBean loggedUserBean;
	@Inject 
	private EventFactory eventFactory;

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
	private InstructorAssignFilter instructorAssignFilter;
	
	private List<InstructorData> credentialInstructors;
	private StudentData studentToAssignInstructor;
	
	private String instructorSearchTerm = "";
	
	private long personalizedForUserId = -1;
	
	private String context;
	
	private String credentialTitle;
	
	private InstructorAssignFilter[] searchFilters;
	private CredentialMembersSortOption[] sortOptions;

	public void init() {
		sortOptions = CredentialMembersSortOption.values();
		instructorAssignFilter = new InstructorAssignFilter(InstructorAssignFilterValue.All, 0);
		//searchFilters = InstructorAssignFilterValue.values();
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			try {
				String title = credManager.getCredentialTitleForCredentialWithType(
						decodedId, LearningResourceType.UNIVERSITY_CREATED);
				if(title != null) {
					credentialTitle = title;
					boolean showAll = loggedUserBean.hasCapability("COURSE.MEMBERS.VIEW");
					if(!showAll) {
						personalizedForUserId = loggedUserBean.getUserId();
					}
					searchCredentialMembers();
					if(searchFilters == null) {
						InstructorAssignFilterValue[] values = InstructorAssignFilterValue.values();
						int size = values.length;
						searchFilters = new InstructorAssignFilter[size];
						for(int i = 0; i < size; i++) {
							InstructorAssignFilter filter = new InstructorAssignFilter(values[i], 0);
							searchFilters[i] = filter;
						}
					}
				} else {
					try {
						FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
					} catch (IOException e) {
						logger.error(e);
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
		TextSearchResponse1<StudentData> searchResponse = textSearch.searchCredentialMembers(
				searchTerm, 
				instructorAssignFilter.getFilter(), 
				page - 1, limit, 
				decodedId, personalizedForUserId, sortOption);

		credentialMembersNumber = (int) searchResponse.getHitsNumber();
		members = searchResponse.getFoundNodes();
		Map<String, Object> additional = searchResponse.getAdditionalInfo();
		if(additional != null) {
			searchFilters = (InstructorAssignFilter[]) additional.get("filters");
			instructorAssignFilter = (InstructorAssignFilter) additional.get("selectedFilter");
		}
	}
	
	public void loadCredentialInstructors(StudentData student) {
		studentToAssignInstructor = student;
		setInstructorSearchTerm("");
		context += "|context:/name:USER|id:" + student.getUser().getId() + "/";
		loadCredentialInstructors();
	}
	
	public void loadCredentialInstructors() {
		TextSearchResponse1<InstructorData> searchResponse = textSearch.searchInstructors(
				instructorSearchTerm, -1, -1, decodedId, InstructorSortOption.Date, null);
		
		if (searchResponse != null) {
			credentialInstructors = searchResponse.getFoundNodes();
		}
	}
	
	//assign student to an instructor
	public void selectInstructor(InstructorData instructor) {
		try {
			EventType event = null;
			Map<String, String> params = new HashMap<>();
			if(studentToAssignInstructor.getInstructor() == null 
					|| studentToAssignInstructor.getInstructor().getInstructorId() 
						!= instructor.getInstructorId()) {
				credInstructorManager.assignStudentToInstructor(studentToAssignInstructor.getUser().getId(), 
						instructor.getInstructorId(), decodedId);
				if(studentToAssignInstructor.getInstructor() == null) {
					event = EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR;
				} else {
					event = EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR;
					params.put("reassignedFromInstructorUserId", 
							studentToAssignInstructor.getInstructor().getUser().getId() + "");
				}
			} else {
				credInstructorManager.unassignStudentFromInstructor(
						studentToAssignInstructor.getUser().getId(), decodedId);
				event = EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR;
			}

			String page = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			try {
				User target = new User();
				target.setId(instructor.getUser().getId());
				User object = new User();
				object.setId(studentToAssignInstructor.getUser().getId());
				params.put("credId", decodedId + "");
				eventFactory.generateEvent(event, loggedUserBean.getUserId(), object, target, 
						page, context, service, params);
			} catch (EventException e) {
				logger.error(e);
			}
			String action = null;
			if(event == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR 
					|| event == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR) {
				studentToAssignInstructor.setInstructor(instructor);
				action = (event == EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR ? "re" : "") + "assigned";
			} else {
				studentToAssignInstructor.setInstructor(null);
				action = "unassigned";
			}
			
			studentToAssignInstructor = null;
			credentialInstructors = null;
			PageUtil.fireSuccessfulInfoMessage("Instructor successfully " + action);
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	public boolean isInstructorCurrentlyAssignedToStudent(InstructorData id) {
		InstructorData inst = studentToAssignInstructor.getInstructor();
		if(inst != null) {
			return inst.getInstructorId() == id.getInstructorId();
		}
		return false;
	}
	
	public boolean doesStudentHaveInstructorAssigned() {
		return studentToAssignInstructor.getInstructor() != null;
	}
	
	public void applySearchFilter(InstructorAssignFilter filter) {
		this.instructorAssignFilter = filter;
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

	public String getInstructorSearchTerm() {
		return instructorSearchTerm;
	}

	public void setInstructorSearchTerm(String instructorSearchTerm) {
		this.instructorSearchTerm = instructorSearchTerm;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public InstructorAssignFilter getInstructorAssignFilter() {
		return instructorAssignFilter;
	}

	public void setInstructorAssignFilter(InstructorAssignFilter instructorAssignFilter) {
		this.instructorAssignFilter = instructorAssignFilter;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public List<InstructorData> getCredentialInstructors() {
		return credentialInstructors;
	}

	public void setCredentialInstructors(List<InstructorData> credentialInstructors) {
		this.credentialInstructors = credentialInstructors;
	}

	public StudentData getStudentToAssignInstructor() {
		return studentToAssignInstructor;
	}

	public void setStudentToAssignInstructor(StudentData studentToAssignInstructor) {
		this.studentToAssignInstructor = studentToAssignInstructor;
	}

	public InstructorAssignFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(InstructorAssignFilter[] searchFilters) {
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
	
}
