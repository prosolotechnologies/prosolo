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
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
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
	@Inject
	private StudentEnrollBean studentEnrollBean;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private CredentialMembersSortOption sortOption = CredentialMembersSortOption.DATE;
	private PaginationData paginationData = new PaginationData();
	private CredentialMembersSearchFilter instructorAssignFilter;
	
	private List<InstructorData> credentialInstructors;
	private StudentData studentToAssignInstructor;
	
	private String instructorSearchTerm = "";
	
	private long personalizedForUserId = -1;
	
	private String context;
	
	private String credentialTitle;
	
	private CredentialMembersSearchFilter[] searchFilters;
	private CredentialMembersSortOption[] sortOptions;

	public void init() {
		sortOptions = CredentialMembersSortOption.values();
		instructorAssignFilter = new CredentialMembersSearchFilter(CredentialMembersSearchFilterValue.All, 0);
		//searchFilters = InstructorAssignFilterValue.values();
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId + "|context:/name:STUDENTS/";
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
						CredentialMembersSearchFilterValue[] values = CredentialMembersSearchFilterValue.values();
						int size = values.length;
						searchFilters = new CredentialMembersSearchFilter[size];
						for(int i = 0; i < size; i++) {
							CredentialMembersSearchFilter filter = new CredentialMembersSearchFilter(values[i], 0);
							searchFilters[i] = filter;
						}
					}
					studentEnrollBean.init(decodedId, context);
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
		TextSearchResponse1<StudentData> searchResponse = textSearch.searchCredentialMembers(
				searchTerm, 
				instructorAssignFilter.getFilter(), 
				this.paginationData.getPage() - 1, this.paginationData.getLimit(), 
				decodedId, personalizedForUserId, sortOption);

		this.paginationData.update((int) searchResponse.getHitsNumber());
		members = searchResponse.getFoundNodes();
		
		Map<String, Object> additional = searchResponse.getAdditionalInfo();
		if (additional != null) {
			searchFilters = (CredentialMembersSearchFilter[]) additional.get("filters");
			instructorAssignFilter = (CredentialMembersSearchFilter) additional.get("selectedFilter");
		}
	}
	
	public void addStudentsAndResetData() {
		studentEnrollBean.enrollStudents();
		this.paginationData.setPage(1);
		searchTerm = "";
		sortOption = CredentialMembersSortOption.DATE;
		members = credManager.getCredentialStudentsData(decodedId, this.paginationData.getLimit());
		searchFilters = credManager.getFiltersWithNumberOfStudentsBelongingToEachCategory(decodedId);
		for (CredentialMembersSearchFilter f : searchFilters) {
			if (f.getFilter() == CredentialMembersSearchFilterValue.All) {
				instructorAssignFilter = f;
				this.paginationData.update((int) f.getNumberOfResults());
				break;
			}
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
	
	public boolean areInstructorAndStudentSameUser(InstructorData id) {
		return id.getUser().getId() == studentToAssignInstructor.getUser().getId();
	}
	
	public boolean doesStudentHaveInstructorAssigned() {
		return studentToAssignInstructor.getInstructor() != null;
	}
	
	public void applySearchFilter(CredentialMembersSearchFilter filter) {
		this.instructorAssignFilter = filter;
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

	@Override
	public PaginationData getPaginationData() {
		return paginationData;
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

	public CredentialMembersSearchFilter getInstructorAssignFilter() {
		return instructorAssignFilter;
	}

	public void setInstructorAssignFilter(CredentialMembersSearchFilter instructorAssignFilter) {
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
	
}
