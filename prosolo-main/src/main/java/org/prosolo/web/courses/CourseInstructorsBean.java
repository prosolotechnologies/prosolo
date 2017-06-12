/**
 * 
 */
package org.prosolo.web.courses;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.CourseInstructorData;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "courseInstructorsBean")
@Component("courseInstructorsBean")
@Scope("view")
public class CourseInstructorsBean implements Serializable {

	private static final long serialVersionUID = -4892911343069292524L;

	private static Logger logger = Logger.getLogger(CourseInstructorsBean.class);

	private List<CourseInstructorData> instructors;

	@Inject private UrlIdEncoder idEncoder;
	@Inject private CourseManager courseManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private NodeChangeObserver nodeChangeObserver;

	// PARAMETERS
	private String id;
	private long decodedId;

	private String searchTerm = "";
	private SortingOption sortOrder = SortingOption.ASC;
	private PaginationData paginationData = new PaginationData();
	
	private CourseInstructorData instructorForRemoval;
	private CourseInstructorData instructorForReassign;
	
	private boolean manuallyAssignStudents;
	
	private String context;
	
	private String courseTitle;

	public void init() {
		decodedId = idEncoder.decodeId(id);
		
		if (decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedId;
			
			try {
				if (courseTitle == null) {
					courseTitle = courseManager.getCourseTitle(decodedId);
				}
				manuallyAssignStudents = courseManager.areStudentsManuallyAssignedToInstructor(decodedId);
				searchCourseInstructors();
			} catch (Exception e) {
				PageUtil.fireErrorMessage(e.getMessage());
			}
		}
	}

	public void searchCourseInstructors() {
		try {
			if (instructors != null) {
				instructors.clear();
			}

			getCourseInstructors();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	//TODO
	public void getCourseInstructors() {
//		Map<String, Object> searchResponse = textSearch.searchInstructors(
//				searchTerm, page - 1, limit, decodedId, sortOrder, null); 
//	
//		populateInstructorsData(searchResponse);
	}

//	private void populateInstructorsData(Map<String, Object> searchResponse) {
//		instructors = new ArrayList<>();
//		
//		if (searchResponse != null) {
//			paginationData.update(((Long) searchResponse.get("resultNumber")).intValue());
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> data = (List<Map<String, Object>>) searchResponse.get("data");
//			if(data != null) {
//				for (Map<String, Object> resMap : data) {
//					instructors.add(new CourseInstructorData(resMap));
//				}
//			}
//		}
//	}
	
	public void changeSortOrder() {
		if(sortOrder == SortingOption.ASC) {
			sortOrder = SortingOption.DESC;
		} else {
			sortOrder = SortingOption.ASC;
		}
		
	} 
	
	public void resetSearchOptions() {
		paginationData.setPage(1);
		resetSortOptions();	
	}
	
	public void resetSortOptions() {
		this.sortOrder = SortingOption.ASC;
	}
	
	public boolean isASCOrder() {
		return sortOrder == SortingOption.ASC;
	}
	
	public void automaticallyReassignStudents() {
		try {
			Map<String, Object> result = courseManager.reassignStudentsAutomatically(instructorForReassign.getInstructorId(), decodedId);
			String appPage = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			fireReassignEvents(instructorForReassign, appPage, service, result, true);
			searchCourseInstructors();
			instructorForReassign = null;
			PageUtil.fireSuccessfulInfoMessage("Students successfully reassigned");
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	public void removeInstructorFromCourse(boolean automatic) {
		try {
			Map<String, Object> res = courseManager.removeInstructorFromCourse(instructorForRemoval.getInstructorId(),
					decodedId, automatic);
			String appPage = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");

			Course course = new Course();
			course.setId(decodedId);
			User instr = new User();
			instr.setId(instructorForRemoval.getUserId());
			try {
				eventFactory.generateEvent(EventType.INSTRUCTOR_REMOVED_FROM_CREDENTIAL, loggedUserBean.getUserId(), 
						instr, course, appPage, context, service, null);
				fireReassignEvents(instructorForRemoval, appPage, service, res, automatic);

			} catch (EventException e) {
				logger.error(e);
			}

			searchCourseInstructors();
			instructorForRemoval = null;
			PageUtil.fireSuccessfulInfoMessage("Instructor successfully removed from course");
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	private void fireReassignEvents(CourseInstructorData instructorData, String appPage, 
			String service, Map<String, Object> res, boolean automatic) {
		long instructorUserId = instructorData.getUserId();
		long instructorId = instructorData.getUserId();
		String lContext = context + "|context:/name:INSTRUCTOR|id:" + instructorId + "/";
		
		if(automatic) {
			@SuppressWarnings("unchecked")
			Map<Long, Long> ids = (Map<Long, Long>) res.get("automatic");
			if(ids != null && !ids.isEmpty()) {
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("courseId", decodedId + "");
				parameters.put("reassignedFromInstructorUserId", instructorUserId + "");
				for(Entry<Long, Long> entry : ids.entrySet()) {
					long eid = entry.getKey();
					long studentUserId = courseManager.getUserIdForEnrollment(eid);
					long insId = entry.getValue();
					long insUserId = courseManager.getUserIdForInstructor(insId);
					try {
						User target = new User();
						target.setId(insUserId);
						User object = new User();
						object.setId(studentUserId);
						@SuppressWarnings("unchecked")
						Event event = eventFactory.generateEvent(EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR, loggedUserBean.getUserId(), 
								object, target, 
								appPage, lContext, service, 
								new Class[] {NodeChangeObserver.class}, parameters);
						nodeChangeObserver.handleEvent(event);
					} catch(Exception e) {
						logger.error(e);
					}
				}
			}	
		} 
			
		@SuppressWarnings("unchecked")
		List<Long> unassignedEnrollmentIds = (List<Long>) res.get("manual");
		if(unassignedEnrollmentIds != null && !unassignedEnrollmentIds.isEmpty()) {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("courseId", decodedId + "");
			List<Long> unassignedUserIds = courseManager
					.getUserIdsForEnrollments(unassignedEnrollmentIds);
			for(Long userId : unassignedUserIds) {
				try {
					User target = new User();
					target.setId(instructorUserId);
					User object = new User();
					object.setId(userId);
					
					@SuppressWarnings("unchecked")
					Event event = eventFactory.generateEvent(EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR, loggedUserBean.getUserId(), 
							object, target, 
							appPage, lContext, service, 
							new Class[] {NodeChangeObserver.class} ,parameters);
					nodeChangeObserver.handleEvent(event);
				} catch (EventException e) {
					logger.error(e);
				}
			}
		}
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

	public List<CourseInstructorData> getInstructors() {
		return instructors;
	}

	public void setInstructors(List<CourseInstructorData> instructors) {
		this.instructors = instructors;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public CourseInstructorData getInstructorForRemoval() {
		return instructorForRemoval;
	}

	public void setInstructorForRemoval(CourseInstructorData instructorForRemoval) {
		this.instructorForRemoval = instructorForRemoval;
	}

	public boolean isManuallyAssignStudents() {
		return manuallyAssignStudents;
	}

	public void setManuallyAssignStudents(boolean manuallyAssignStudents) {
		this.manuallyAssignStudents = manuallyAssignStudents;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public CourseInstructorData getInstructorForReassign() {
		return instructorForReassign;
	}

	public void setInstructorForReassign(CourseInstructorData instructorForReassign) {
		this.instructorForReassign = instructorForReassign;
	}

}
