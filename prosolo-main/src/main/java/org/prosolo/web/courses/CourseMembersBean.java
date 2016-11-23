/**
 * 
 */
package org.prosolo.web.courses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.CredentialMembersSortOption1;
import org.prosolo.search.util.credential.InstructorAssignFilterValue;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseInstructorData;
import org.prosolo.web.courses.data.UserData;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Deprecated
@ManagedBean(name = "courseMembersBean")
@Component("courseMembersBean")
@Scope("view")
public class CourseMembersBean implements Serializable {

	private static final long serialVersionUID = 1827743731093959636L;

	private static Logger logger = Logger.getLogger(CourseMembersBean.class);

	private List<UserData> members;

	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private TextSearch textSearch;
	@Inject
	private CourseManager courseManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private int courseMembersNumber;
	private int page = 1;
	private int limit = 10;
	private CredentialMembersSortOption sortField = CredentialMembersSortOption.STUDENT_NAME;
	private SortingOption sortOrder = SortingOption.ASC;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;
	private InstructorAssignFilterValue instructorAssignedFilter = InstructorAssignFilterValue.All;
	private boolean filterUnassigned;
	
	private List<CourseInstructorData> courseInstructors;
	private UserData userToAssignInstructor;
	
	private String instructorSearchTerm = "";
	
	private long personalizedForUserId = -1;
	
	private String context;
	
	private String courseTitle;

//	public void init() {
//		decodedId = idEncoder.decodeId(id);
//		if (decodedId > 0) {
//			context = "name:CREDENTIAL|id:" + decodedId;
//			try {
//				boolean showAll = loggedUserBean.hasCapability("COURSE.MEMBERS.VIEW");
//				if(!showAll) {
//					personalizedForUserId = loggedUserBean.getUser().getId();
//				}
//				if(courseTitle == null) {
//					courseTitle = courseManager.getCourseTitle(decodedId);
//				}
//				searchCourseMembers();
//				// List<Map<String, Object>> result =
//				// courseManager.getCourseParticipantsWithCourseInfo(decodedId);
//				// populateCourseMembersData(result);
//			} catch (Exception e) {
//				PageUtil.fireErrorMessage(e.getMessage());
//			}
//		}
//	}
//
//	// private void populateCourseMembersData(List<Map<String, Object>> result)
//	// {
//	// members = new LinkedList<>();
//	// for (Map<String, Object> resMap :result){
//	// User user = (User) resMap.get("user");
//	// User instructor = (User) resMap.get("instructor");
//	// int progress = (int) resMap.get("courseProgress");
//	//
//	// UserData ud = new UserData(user, instructor, progress);
//	//
//	// members.add(ud);
//	// }
//	// }
//
//	public void searchCourseMembers() {
//		try {
//			if (members != null) {
//				this.members.clear();
//			}
//
//			getCourseMembers();
//			generatePagination();
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e);
//		}
//	}
//
//	// public void search(String searchQuery) {
//	// search(searchQuery, null, false);
//	// }
//
//	private void generatePagination() {
//		//if we don't want to generate all links
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				1, "...");
//		//if we want to genearte all links in paginator
////		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
////				true, "...");
//		numberOfPages = paginator.getNumberOfPages();
//		paginationLinks = paginator.generatePaginationLinks();
//	}
//
//	public void getCourseMembers() {
////		CredentialMembersSortOption sortOption = new CredentialMembersSortOption(sortField, sortOrder);
////		
////		Map<String, Object> searchResponse = textSearch.searchCourseMembers(searchTerm, 
////				instructorAssignedFilter, 
////				page - 1, limit, 
////				decodedId, personalizedForUserId, sortOption);
////
////		populateCourseMembersData(searchResponse);
//	}
//
//	private void populateCourseMembersData(Map<String, Object> searchResponse) {
//		members = new ArrayList<>();
//		
//		if (searchResponse != null) {
//			courseMembersNumber = ((Long) searchResponse.get("resultNumber")).intValue();
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> data = (List<Map<String, Object>>) searchResponse.get("data");
//			if(data != null) {
//				for (Map<String, Object> resMap : data) {
//					User user = (User) resMap.get("user");
//					@SuppressWarnings("unchecked")
//					Map<String, Object> instructor = (Map<String, Object>) resMap.get("instructor");
//					int progress = (int) resMap.get("courseProgress");
//					String profileType = (String) resMap.get("profileType");
//					String profileTitle = (String) resMap.get("profileTitle");
//					UserData ud = new UserData(user, instructor, progress, profileType, profileTitle);
//					
//					members.add(ud);
//				}
//			}
//
//		}
//	}
//	
//	public void loadCourseInstructors(UserData user) {
//		userToAssignInstructor = user;
//		setInstructorSearchTerm("");
//		context += "|context:/name:USER|id:" + user.getId() + "/";
//		loadCourseInstructors();
//	}
//	
//	public void loadCourseInstructors() {
//		courseInstructors = new ArrayList<>();
//		Map<String, Object> searchResponse = textSearch.searchInstructors(instructorSearchTerm, 
//				-1, -1, decodedId, SortingOption.ASC, null);
//		
//		if (searchResponse != null) {
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> data = (List<Map<String, Object>>) searchResponse.get("data");
//			if(data != null) {
//				for (Map<String, Object> resMap : data) {
//					courseInstructors.add(new CourseInstructorData(resMap));
//				}
//			}
//		}
//	}
//	
//	//assign student to an instructor
//	public void selectInstructor(CourseInstructorData instructor) {
//		try {
//			courseManager.assignInstructorToStudent(userToAssignInstructor.getId(), instructor.getInstructorId(),
//				decodedId);
//			
//			String page = PageUtil.getPostParameter("page");
//			String service = PageUtil.getPostParameter("service");
//			
//			try {
//				User target = new User();
//				target.setId(instructor.getUserId());
//				User object = new User();
//				object.setId(userToAssignInstructor.getId());
//				eventFactory.generateEvent(EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR, loggedUserBean.getUser(), object, target, 
//						page, context, service, null);
//			} catch (EventException e) {
//				logger.error(e);
//			}
//			CourseInstructorData instructorData = new CourseInstructorData();
//			instructorData.setName(instructor.getName());
//			instructorData.setInstructorId(instructor.getInstructorId());
//			userToAssignInstructor.setInstructor(instructorData);
//			userToAssignInstructor = null;
//			courseInstructors = null;
//			PageUtil.fireSuccessfulInfoMessage("Instructor successfully assigned");
//		} catch(DbConnectionException e) {
//			PageUtil.fireErrorMessage(e.getMessage());
//		}
//	}
//	
//	public void filterCourseMembers() {
//		this.page = 1;
//		if(filterUnassigned) {
//			instructorAssignedFilter = InstructorAssignedFilter.Unassigned;
//		} else {
//			instructorAssignedFilter = InstructorAssignedFilter.All;
//		}
//		searchCourseMembers();
//	}
//	
//	public boolean isCurrentPageFirst() {
//		return page == 1 || numberOfPages == 0;
//	}
//	
//	public boolean isCurrentPageLast() {
//		return page == numberOfPages || numberOfPages == 0;
//	}
//	// public void search(String searchQuery) {
//		// search(searchQuery, null, false);
//		// }
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
//	
//	public void setSortField(CredentialMembersSortField field) {
//		if(sortField == field) {
//			changeSortOrder();
//		} else {
//			sortField = field;
//			sortOrder = SortingOption.ASC;
//		}
//		page = 1;
//	}
//	
//	private void changeSortOrder() {
//		if(sortOrder == SortingOption.ASC) {
//			sortOrder = SortingOption.DESC;
//		} else {
//			sortOrder = SortingOption.ASC;
//		}
//		
//	}
//	
//	public void resetSearchOptions() {
//		this.page = 1;
//		resetSortOptions();	
//	}
//	
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
//
//	/*
//	 * PARAMETERS
//	 */
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public String getId() {
//		return id;
//	}
//
//	/*
//	 * GETTERS / SETTERS
//	 */
//
//	public List<UserData> getMembers() {
//		return members;
//	}
//
//	public void setMembers(List<UserData> members) {
//		this.members = members;
//	}
//
//	public String getSearchTerm() {
//		return searchTerm;
//	}
//
//	public void setSearchTerm(String searchTerm) {
//		this.searchTerm = searchTerm;
//	}
//
//	public int getCourseMembersNumber() {
//		return courseMembersNumber;
//	}
//
//	public void setCourseMembersNumber(int courseMembersNumber) {
//		this.courseMembersNumber = courseMembersNumber;
//	}
//
//	public int getPage() {
//		return page;
//	}
//
//	public void setPage(int page) {
//		this.page = page;
//	}
//
//	public int getLimit() {
//		return limit;
//	}
//
//	public void setLimit(int limit) {
//		this.limit = limit;
//	}
//
//	public CredentialMembersSortField getSortField() {
//		return sortField;
//	}
//
//	public List<PaginationLink> getPaginationLinks() {
//		return paginationLinks;
//	}
//
//	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
//		this.paginationLinks = paginationLinks;
//	}
//
//	public boolean isFilterUnassigned() {
//		return filterUnassigned;
//	}
//
//	public void setFilterUnassigned(boolean filterAssigned) {
//		this.filterUnassigned = filterAssigned;
//	}
//
//	public List<CourseInstructorData> getCourseInstructors() {
//		return courseInstructors;
//	}
//
//	public void setCourseInstructors(List<CourseInstructorData> courseInstructors) {
//		this.courseInstructors = courseInstructors;
//	}
//
//	public UserData getUserToAssignInstructor() {
//		return userToAssignInstructor;
//	}
//
//	public void setUserToAssignInstructor(UserData userToAssignInstructor) {
//		this.userToAssignInstructor = userToAssignInstructor;
//	}
//
//	public String getInstructorSearchTerm() {
//		return instructorSearchTerm;
//	}
//
//	public void setInstructorSearchTerm(String instructorSearchTerm) {
//		this.instructorSearchTerm = instructorSearchTerm;
//	}
//
//	public long getDecodedId() {
//		return decodedId;
//	}
//
//	public void setDecodedId(long decodedId) {
//		this.decodedId = decodedId;
//	}
//
//	public String getCourseTitle() {
//		return courseTitle;
//	}
//
//	public void setCourseTitle(String courseTitle) {
//		this.courseTitle = courseTitle;
//	}
	
}
