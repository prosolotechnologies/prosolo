package org.prosolo.web.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.data.BasicUserData;
import org.prosolo.web.courses.data.CourseInstructorData;
import org.prosolo.web.courses.data.ExtendedUserData;
import org.prosolo.web.courses.data.InstructorStudentsData;
import org.prosolo.web.search.data.SortingOption;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "studentReassignBean")
@Component("studentReassignBean")
@Scope("view")
public class StudentReassignBean implements Serializable {

	private static final long serialVersionUID = 8480340793622642424L;

	private static Logger logger = Logger.getLogger(StudentReassignBean.class);
	
	@Inject private UrlIdEncoder idEncoder;
	@Inject private CourseManager courseManager;
	@Inject private TextSearch textSearch;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	// PARAMETERS
	private String id;
	private long decodedId;
	private String courseId;
	private long decodedCourseId;
	
	private CourseInstructorData instructor;
	private List<BasicUserData> assignedStudents;

	private List<CourseInstructorData> otherInstructorsList;
	private Map<Long, InstructorStudentsData> instructorsWithReassignedStudents;
	private InstructorStudentsData selectedInstructorData;
	
	private String instructorSearchTerm = "";
	
	private int numberOfCurrentlySelectedUsers;
	
	private boolean instructorSelected;
	
	private String context;
	
	private String courseTitle;
	
	public void init() {
		assignedStudents = new ArrayList<>();
		instructorsWithReassignedStudents = new HashMap<>();
		selectedInstructorData = new InstructorStudentsData();
		decodedId = idEncoder.decodeId(id);
		decodedCourseId = idEncoder.decodeId(courseId);
		if (decodedCourseId > 0 && decodedId > 0) {
			context = "name:CREDENTIAL|id:" + decodedCourseId + "|context:/name:INSTRUCTOR|id:" + decodedId + "/";
			try {
				if(courseTitle == null) {
					courseTitle = courseManager.getCourseTitle(decodedCourseId);
				}
				Map<String, Object> instructorData = courseManager.getBasicInstructorInfo(decodedId);
				instructor = new CourseInstructorData();
				if(instructorData != null && !instructorData.isEmpty()) {
					instructor.setInstructorId(decodedId);
					instructor.setFullName((String) instructorData.get("firstName"), 
							(String) instructorData.get("lastName"));
					instructor.setMaxNumberOfStudents((int) instructorData.get("maxNumberOfStudents")); 
					instructor.setUserId((long) instructorData.get("userId"));
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> students = (List<Map<String, Object>>) instructorData.get("students");
					int size = students != null ? students.size() : 0;
					instructor.setNumberOfAssignedStudents(size);
					if(students != null) {
						for(Map<String, Object> student : students) {
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

	public void loadInstructors() {
		instructorSearchTerm = "";
		loadCourseInstructors();
	}
	
	public void loadCourseInstructors() {
		otherInstructorsList = new ArrayList<>();
		List<Long> excluded = new ArrayList<>();
		excluded.add(instructor.getUserId());
		//TODO
//		Map<String, Object> searchResponse = textSearch.searchInstructors(instructorSearchTerm, 
//				-1, -1, decodedCourseId, SortingOption.ASC, excluded);
//		
//		if (searchResponse != null) {
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> data = (List<Map<String, Object>>) searchResponse.get("data");
//			if(data != null) {
//				for (Map<String, Object> resMap : data) {
//					otherInstructorsList.add(new CourseInstructorData(resMap));
//				}
//			}
//		}
	}
	
	public void reassignStudentsTemp() {
		Iterator<BasicUserData> iterator = assignedStudents.iterator();
		while(iterator.hasNext()) {
			BasicUserData userData = iterator.next();
			if(userData.isSelected()) {
				userData.setSelected(false);
				List<ExtendedUserData> extStudentDataList = selectedInstructorData.getStudents();
				ExtendedUserData eud = new ExtendedUserData(userData, true);
				extStudentDataList.add(eud);
				selectedInstructorData.getStudentsToAssign().add(userData.getId());
				iterator.remove();
			}
		}
		numberOfCurrentlySelectedUsers = 0;
	}

	public void saveReassignedStudents() {
		try {
			List<Map<String, Object>> instructorsForUpdate = new ArrayList<>();
			for(Entry<Long, InstructorStudentsData> entry : instructorsWithReassignedStudents.entrySet()) {
				InstructorStudentsData inst = entry.getValue();
				if(!inst.getStudentsToAssign().isEmpty()) {
					long id = entry.getKey();
					Map<String, Object> instructorForUpdate = new HashMap<>();
					instructorForUpdate.put("id", id);
					instructorForUpdate.put("courseId", decodedCourseId);
					instructorForUpdate.put("assign", inst.getStudentsToAssign());
					instructorsForUpdate.add(instructorForUpdate);
				}
			}
			courseManager.updateStudentsAssignedToInstructors(instructorsForUpdate);
			
			String page = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("courseId", decodedCourseId + "");
					parameters.put("reassignedFromInstructorUserId", instructor.getUserId() + "");
					
					for(Entry<Long, InstructorStudentsData> entry : instructorsWithReassignedStudents.entrySet()) {
						InstructorStudentsData inst = entry.getValue();
						if(inst.getStudentsToAssign() != null) {
							for(long id : inst.getStudentsToAssign()) {
								try {
									User target = new User();
									target.setId(inst.getInstructor().getUserId());
									User object = new User();
									object.setId(id);
									eventFactory.generateEvent(EventType.STUDENT_REASSIGNED_TO_INSTRUCTOR, loggedUserBean.getUser(), object, target, 
											page, context, service, parameters);
								} catch(Exception e) {
									logger.error(e);
								}
							}
						}
						
					}			
				}
			});
		} catch(DbConnectionException e) {
			PageUtil.fireErrorMessage(e.getMessage());
		}
		
	}
	
	//select instructor to reassign students to him
	public void selectInstructor(CourseInstructorData user) {
		InstructorStudentsData isd = instructorsWithReassignedStudents.get(user.getInstructorId());
		if(isd == null) {
			isd = new InstructorStudentsData();
			List<ExtendedUserData> students = loadInstructorStudents(user.getInstructorId());
			isd.setInstructor(user);
			isd.setStudents(students);
			isd.setStudentsToAssign(new ArrayList<>());
			instructorsWithReassignedStudents.put(user.getInstructorId(), isd);
		}
		selectedInstructorData = isd;
		numberOfCurrentlySelectedUsers = 0;
		instructorSelected = true;
	}
	
	private List<ExtendedUserData> loadInstructorStudents(long instructorId) {
		List<User> users = courseManager.getUsersAssignedToInstructor(instructorId);
		List<ExtendedUserData> extendUserData = new ArrayList<>();
		if(users != null) {
			for(User user : users) {
				BasicUserData bud = new BasicUserData(user);
				extendUserData.add(new ExtendedUserData(bud, false));
			}
		}
		return extendUserData;
	}
	
	public void studentSelectionListener(BasicUserData user) {
		if(user.isSelected()) {
			numberOfCurrentlySelectedUsers ++;
		} else {
			numberOfCurrentlySelectedUsers --;
		}
	}
	
	public boolean isNumberOfAssignedStudentsLimitAchieved() {
		int numberOfAssigned = selectedInstructorData.getStudents() != null 
				? selectedInstructorData.getStudents().size() : 0;
		int max = selectedInstructorData.getInstructor() != null 
				? selectedInstructorData.getInstructor().getMaxNumberOfStudents()
			    : 0;
		int spotsAvailable = max - numberOfAssigned;
		return numberOfCurrentlySelectedUsers == spotsAvailable;
	}
	
	public void moveStudentToAssigned(ExtendedUserData user) {
		selectedInstructorData.getStudents().remove(user);
		selectedInstructorData.getStudentsToAssign().remove(new Long(user.getUser().getId()));
		assignedStudents.add(user.getUser());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
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

	public List<CourseInstructorData> getOtherInstructorsList() {
		return otherInstructorsList;
	}

	public void setOtherInstructorsList(List<CourseInstructorData> otherInstructorsList) {
		this.otherInstructorsList = otherInstructorsList;
	}

	public InstructorStudentsData getSelectedInstructorData() {
		return selectedInstructorData;
	}

	public void setSelectedInstructorData(InstructorStudentsData selectedInstructorData) {
		this.selectedInstructorData = selectedInstructorData;
	}

	public String getInstructorSearchTerm() {
		return instructorSearchTerm;
	}

	public void setInstructorSearchTerm(String instructorSearchTerm) {
		this.instructorSearchTerm = instructorSearchTerm;
	}

	public boolean isInstructorSelected() {
		return instructorSelected;
	}

	public void setInstructorSelected(boolean instructorSelected) {
		this.instructorSelected = instructorSelected;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public long getDecodedCourseId() {
		return decodedCourseId;
	}

	public void setDecodedCourseId(long decodedCourseId) {
		this.decodedCourseId = decodedCourseId;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}
	
}
