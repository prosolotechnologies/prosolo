package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseInstructor;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.BasicUserData;
import org.prosolo.web.courses.data.CourseInstructorData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Deprecated
@ManagedBean(name = "instructorEditBean")
@Component("instructorEditBean")
@Scope("view")
public class InstructorEditBean implements Serializable {

	private static final long serialVersionUID = 5585476106181894226L;

	private static Logger logger = Logger.getLogger(InstructorEditBean.class);
	
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CourseManager courseManager;
	@Inject private UserTextSearch userTextSearch;
	@Inject private RoleManager roleManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	// PARAMETERS
	private String id;
	private long decodedId;
	private String courseId;
	private long decodedCourseId;
	
	private boolean isNew;
	private CourseInstructorData instructor;
	private List<BasicUserData> assignedStudents;
	private List<BasicUserData> unassignedStudents;
	private List<Long> usersToAssign;
	private List<Long> usersToUnassign;
	
	private List<BasicUserData> instructors;

	private String title;
	
	private String instructorSearchTerm = "";
	private String studentSearchTerm = "";
	
	private long instructorRoleId;
	
	private int numberOfCurrentlySelectedUsers;
	
	//private boolean manuallyAssignStudents;
	
	private String context;
	
	private String courseTitle;
	
	public void init() {
		try {
			assignedStudents = new ArrayList<>();
			usersToAssign = new ArrayList<>();
			usersToUnassign = new ArrayList<>();
			decodedId = idEncoder.decodeId(id);
			decodedCourseId = idEncoder.decodeId(courseId);
			
			if (decodedCourseId > 0) {
				context = "name:CREDENTIAL|id:" + decodedCourseId;
				
				if (courseTitle == null) {
					courseTitle = courseManager.getCourseTitle(decodedCourseId);
				}
				//manuallyAssignStudents = courseManager.areStudentsManuallyAssignedToInstructor(decodedCourseId);
				searchUnassignedStudents();
				
				if (decodedId > 0) {
					//instructor edit
					title = "Edit instructor";
					context += "|context:/name:INSTRUCTOR|id:" + decodedId + "/";
					
					Map<String, Object> instructorData = courseManager.getBasicInstructorInfo(decodedId);
					instructor = new CourseInstructorData();
					
					if (instructorData != null && !instructorData.isEmpty()) {
						instructor.setInstructorId(decodedId);
						instructor.setFullName((String) instructorData.get("firstName"), (String) instructorData.get("lastName"));
						instructor.setMaxNumberOfStudents((int) instructorData.get("maxNumberOfStudents")); 
						instructor.setUserId((long) instructorData.get("userId"));
						
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> students = (List<Map<String, Object>>) instructorData.get("students");
						int size = students != null ? students.size() : 0;
						instructor.setNumberOfAssignedStudents(size);
						
						if (students != null) {
							for (Map<String, Object> student : students) {
								assignedStudents.add(new BasicUserData(student));
							}
						}
					} else {
						try {
							FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
						} catch (IOException e) {
							logger.error(e);
						}
					}
				} else if(decodedId == 0) {
					//instructor add
					isNew = true;
					title = "Add new instructor";
					List<Long> roleIds = roleManager.getRoleIdsForName("INSTRUCTOR");
					
					if (roleIds.size() == 1) {
						instructorRoleId = roleIds.get(0);
					}
					instructor = new CourseInstructorData();
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
	}

	public void searchInstructors() {
//		try {
//			instructors = new ArrayList<>();
//			
//			if (instructorSearchTerm != null && !"".equals(instructorSearchTerm)) {
//				Map<String, Object> result = textSearch.searchUsersWithInstructorRole(instructorSearchTerm, decodedCourseId, instructorRoleId);
//				@SuppressWarnings("unchecked")
//				List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("data");
//				
//				if (users != null) {
//					for (Map<String, Object> user : users) {
//						BasicUserData data = new BasicUserData(user);
//						instructors.add(data);
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error(e);
//		}
	}
	
	public void searchUnassignedStudents() {
		try {
//			unassignedStudents = new ArrayList<>();
//			
//			Map<String, Object> result = textSearch.searchUnassignedCourseMembers(studentSearchTerm, decodedCourseId);
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> unassignedUsers = (List<Map<String, Object>>) result.get("data");
//			
//			if (unassignedUsers != null) {
//				for (Map<String, Object> user : unassignedUsers) {
//					BasicUserData data = new BasicUserData(user);
//					unassignedStudents.add(data);
//				}
//			}
		} catch(Exception e) {
			logger.error(e);
		}
	}
	
	public void moveStudentsToAssigned() {
		Iterator<BasicUserData> iterator = unassignedStudents.iterator();
		
		while (iterator.hasNext()) {
			BasicUserData userData = iterator.next();
			
			if (userData.isSelected()) {
				usersToAssign.add(userData.getId());
				assignedStudents.add(userData);
				iterator.remove();
			}
		}
		saveAssignedStudents();
	}

	private void saveAssignedStudents() {
		try {
			courseManager.updateStudentsAssignedToInstructor(
					instructor.getInstructorId(), 
					decodedCourseId,
					usersToAssign, 
					usersToUnassign);
			
			String page = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("courseId", decodedCourseId + "");
					
					for (Long userId : usersToAssign) {
						try {
							User target = new User();
							target.setId(instructor.getUserId());
							User object = new User();
							object.setId(userId);
							
							eventFactory.generateEvent(
									EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR, 
									loggedUserBean.getUserId(), 
									object, 
									target, 
									page, 
									context, 
									service, 
									parameters);
						} catch (EventException e) {
							logger.error(e);
						}
					}
					
					for (Long userId : usersToUnassign) {
						try {
							User target = new User();
							target.setId(instructor.getUserId());
							User object = new User();
							object.setId(userId);
							eventFactory.generateEvent(
									EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR,
									loggedUserBean.getUserId(), 
									object, 
									target,
									page, 
									context, 
									service, 
									parameters);
						} catch (EventException e) {
							logger.error(e);
						}
					}
				}
			});
			
			int numberOfAssigned = instructor.getNumberOfAssignedStudents();
			instructor.setNumberOfAssignedStudents(numberOfAssigned + numberOfCurrentlySelectedUsers);
			numberOfCurrentlySelectedUsers = 0;
			
			PageUtil.fireSuccessfulInfoMessage("Instructor updated");
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void selectInstructor(BasicUserData user) {
		instructor.setUserId(user.getId());
		instructor.setName(user.getFullName());
	}
	
	public void saveInstructor() {
		try {
			int numberOfStudents = 0;
			Integer i = instructor.getMaxNumberOfStudents();
			
			if (i != null) {
				numberOfStudents = i.intValue();
			}
			
			CourseInstructor courseInstructor = courseManager.saveCourseInstructor(
					instructor.getInstructorId(),
					instructor.getUserId(), 
					decodedCourseId, 
					numberOfStudents);
			
			if (instructor.getInstructorId() == 0) {
				String page = PageUtil.getPostParameter("page");
				String service = PageUtil.getPostParameter("service");
				final String lContext = context;
				
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						Course course = new Course();
						course.setId(decodedCourseId);
						User instr = new User();
						instr.setId(instructor.getUserId());
						
						try {
							eventFactory.generateEvent(EventType.INSTRUCTOR_ASSIGNED_TO_CREDENTIAL, loggedUserBean.getUserId(), instr, course, page, lContext, service, null);
						} catch (EventException e) {
								logger.error(e);
						}
					}
				});
			}
			
			instructor.setInstructorId(courseInstructor.getId());
			instructor.setMaxNumberOfStudents(courseInstructor.getMaxNumberOfStudents());
			context += "|context:/name:INSTRUCTOR|id:" + instructor.getInstructorId() + "/";
			
			PageUtil.fireSuccessfulInfoMessage("Instructor updated");
		} catch(Exception e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}
	
	public void studentSelectionListener(BasicUserData user) {
		if (user.isSelected()) {
			numberOfCurrentlySelectedUsers++;
		} else {
			numberOfCurrentlySelectedUsers--;
		}
	}
	
	public boolean isNumberOfAssignedStudentsLimitAchieved() {
		int max = instructor.getMaxNumberOfStudents() != null ? instructor.getMaxNumberOfStudents() : 0;
		int spotsAvailable = max - instructor.getNumberOfAssignedStudents();
		return numberOfCurrentlySelectedUsers == spotsAvailable;
	}
	
	public void unassignStudent(BasicUserData user) {
		usersToUnassign.add(user.getId());
		unassignedStudents.add(user);
		assignedStudents.remove(user);
		int no = instructor.getNumberOfAssignedStudents();
		instructor.setNumberOfAssignedStudents(no - 1);
		
		PageUtil.fireSuccessfulInfoMessage("Student removed");
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public CourseInstructorData getInstructor() {
		return instructor;
	}

	public void setInstructor(CourseInstructorData instructor) {
		this.instructor = instructor;
	}

	public List<BasicUserData> getAssignedStudents() {
		return assignedStudents;
	}

	public void setAssignedStudents(List<BasicUserData> assignedStudents) {
		this.assignedStudents = assignedStudents;
	}

	public List<BasicUserData> getUnassignedStudents() {
		return unassignedStudents;
	}

	public void setUnassignedStudents(List<BasicUserData> unassignedStudents) {
		this.unassignedStudents = unassignedStudents;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInstructorSearchTerm() {
		return instructorSearchTerm;
	}

	public void setInstructorSearchTerm(String instructorSearchTerm) {
		this.instructorSearchTerm = instructorSearchTerm;
	}

	public String getStudentSearchTerm() {
		return studentSearchTerm;
	}

	public void setStudentSearchTerm(String studentSearchTerm) {
		this.studentSearchTerm = studentSearchTerm;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<BasicUserData> getInstructors() {
		return instructors;
	}

	public void setInstructors(List<BasicUserData> instructors) {
		this.instructors = instructors;
	}

	public int getNumberOfCurrentlySelectedUsers() {
		return numberOfCurrentlySelectedUsers;
	}

	public void setNumberOfCurrentlySelectedUsers(int numberOfCurrentlySelectedUsers) {
		this.numberOfCurrentlySelectedUsers = numberOfCurrentlySelectedUsers;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}
	
}
