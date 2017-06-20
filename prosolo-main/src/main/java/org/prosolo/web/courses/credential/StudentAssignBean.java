/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "studentAssignBean")
@Component("studentAssignBean")
@Scope("view")
public class StudentAssignBean implements Serializable, Paginable {

	private static final long serialVersionUID = -5266580663667959010L;

	private static Logger logger = Logger.getLogger(StudentAssignBean.class);

	@Inject private UserTextSearch userTextSearch;
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private CredentialManager credManager;

	private long credId;

	private String studentSearchTerm;
	private PaginationData paginationData = new PaginationData(3);
	private CredentialMembersSearchFilter searchFilter;
	private CredentialMembersSearchFilter[] searchFilters;
	
	private InstructorData instructorForStudentAssign;
	private int maxNumberOfStudents;
	
	private String context;

	private List<StudentData> students;
	private List<Long> studentsToAssign;
	private List<Long> studentsToUnassign;
	
	private boolean selectAll;
	private List<Long> studentsToExcludeFromAssign;
	
	public void init(long credId, String context) {
		this.credId = credId;
		this.context = context;
	}
	
	public boolean isLimitExceeded() {
		return isLimitExceeded(maxNumberOfStudents);
	}
	
	public boolean isLimitExceeded(int max) {
		if(max == 0) {
			return false;
		}
		int currentNumberOfAssignedStudents = getCurrentNumberOfAssignedStudents();
		return currentNumberOfAssignedStudents > max;
	}
	
	public int getCurrentNumberOfAssignedStudents() {
		int numberOfAffectedStudents = studentsToAssign.size() - studentsToUnassign.size();
		return instructorForStudentAssign.getNumberOfAssignedStudents() 
				+ numberOfAffectedStudents;
	}
	
	public void prepareStudentAssign(InstructorData id) {
		selectAll = false;
		searchFilter = new CredentialMembersSearchFilter(CredentialMembersSearchFilterValue.All, 0);
		instructorForStudentAssign = id;
		maxNumberOfStudents = instructorForStudentAssign.getMaxNumberOfStudents();
		studentSearchTerm = "";
		studentsToAssign = new ArrayList<>();
		studentsToUnassign = new ArrayList<>();
		studentsToExcludeFromAssign = new ArrayList<>();
		paginationData.setPage(1);
		searchStudents();
	}
	
	public void selectAllChecked() {
		studentsToUnassign = new ArrayList<>();
		studentsToAssign = new ArrayList<>();
		studentsToExcludeFromAssign = new ArrayList<>();
		//if select all is checked then all students should be preselected
		if(selectAll) {
			for(StudentData s : students) {
				s.setAssigned(true);
			}
		} else {
			for(StudentData s : students) {
				if(s.getInstructor() == null) {
					s.setAssigned(false);
				} else {
					s.setAssigned(true);
				}
			}
		}
	}
	
	public void searchStudents() {
		PaginatedResult<StudentData> result = userTextSearch
				.searchUnassignedAndStudentsAssignedToInstructor(studentSearchTerm, credId, 
						instructorForStudentAssign.getUser().getId(), searchFilter.getFilter(), 
						paginationData.getPage() - 1, paginationData.getLimit());
		students = result.getFoundNodes();
		setCurrentlyAssignedAndUnassignedStudents();
		paginationData.update((int) result.getHitsNumber());
		Map<String, Object> additional = result.getAdditionalInfo();
		
		if (additional != null) {
			searchFilters = (CredentialMembersSearchFilter[]) additional.get("filters");
			searchFilter = (CredentialMembersSearchFilter) additional.get("selectedFilter");
		}
	}
	
//	public void updateMaxNumberOfStudents() {
//		logger.info("Max number of students updated " + instructorForStudentAssign
//				.getMaxNumberOfStudents());
//	}
	
	/**
	 * set assigned to true for students that are currently assigned in memory
	 * and to false for students that are currently unassigned in memory
	 */
	private void setCurrentlyAssignedAndUnassignedStudents() {
		if(students != null) {
			for(StudentData sd : students) {
				if(sd.isAssigned()) {
					sd.setAssigned(!checkIfExists(sd.getUser().getId(), studentsToUnassign));
				} else {
					if(selectAll) {
						if(!checkIfExists(sd.getUser().getId(), studentsToExcludeFromAssign)) {
							sd.setAssigned(true);
						}
					} else {
						sd.setAssigned(checkIfExists(sd.getUser().getId(), studentsToAssign));
					}
				}
			}
		}
	}

	private boolean checkIfExists(long id, List<Long> list) {
		if(list == null) {
			return false;
		}
		for(Long l : list) {
			if(id == l) {
				return true;
			}
		}
		return false;
	}

	public void studentAssignChecked(int index) {
		StudentData sd = students.get(index);
		if(sd.isAssigned()) {
			if(sd.getInstructor() != null) {
				studentsToUnassign.remove(new Long(sd.getUser().getId()));
			} else {
				if(selectAll) {
					studentsToExcludeFromAssign.remove(new Long(sd.getUser().getId()));
				} else {
					studentsToAssign.add(sd.getUser().getId());
				}
			}
		} else {
			if(sd.getInstructor() != null) {
				studentsToUnassign.add(sd.getUser().getId());
			} else {
				if(selectAll) {
					studentsToExcludeFromAssign.add(sd.getUser().getId());
				} else {
					studentsToAssign.remove(new Long(sd.getUser().getId()));
				}
			}
		}
	}
	
	public void assignStudents() {
		try {
			if(isLimitExceeded()) {
				FacesContext context = FacesContext.getCurrentInstance();
				UIInput input = (UIInput) context.getViewRoot().findComponent(
						"formAssignStudents:inputTextMaxNumberOfStudents");
				input.setValid(false);
				FacesContext.getCurrentInstance().validationFailed();
			} else {
				instructorForStudentAssign.setMaxNumberOfStudents(maxNumberOfStudents);
				String page = PageUtil.getPostParameter("page");
				String service = PageUtil.getPostParameter("service");
				String lContext = context + "|context:/name:INSTRUCTOR|id:" 
						+ instructorForStudentAssign.getInstructorId() + "/";
				LearningContextData ctx = new LearningContextData(page, lContext, service);
				List<Long> usersToAssign = null;
				if (selectAll) {
					List<Long> studentsToExcludeCopy = new ArrayList<>(studentsToExcludeFromAssign);
					//exclude instructor because user can not be instructor for himself
					studentsToExcludeCopy.add(instructorForStudentAssign.getUser().getId());
					usersToAssign = credManager.getUnassignedCredentialMembersIds(
							credId, studentsToExcludeCopy);
				} else {
					usersToAssign = studentsToAssign;
				}
				credInstructorManager.updateInstructorAndStudentsAssigned(credId, instructorForStudentAssign, 
						usersToAssign, studentsToUnassign, loggedUserBean.getUserId(), ctx);
				//fireAssignEvent(instructorForStudentAssign, usersToAssign, page, lContext, service);
				//fireUnassignEvent(instructorForStudentAssign, studentsToUnassign, page, lContext, service);
				int numberOfAffectedStudents = usersToAssign.size() - studentsToUnassign.size();
				instructorForStudentAssign.setNumberOfAssignedStudents(
						instructorForStudentAssign.getNumberOfAssignedStudents() + numberOfAffectedStudents);
				//instructorForStudentAssign = null;
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			}
		} catch (DbConnectionException e) {
			logger.error(e);
			instructorForStudentAssign.setMaxNumberOfStudentsToOriginalValue();
			PageUtil.fireErrorMessage(e.getMessage());
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	public void resetAndSearch() {
		paginationData.setPage(1);
		searchStudents();	
	}
	
	public void applySearchFilter(CredentialMembersSearchFilter filter) {
		this.searchFilter = filter;
		paginationData.setPage(1);
		searchStudents();
	}
	
	//VALIDATOR METHODS
	
//	public void validateMaxNumberOfStudents(FacesContext context, UIComponent component, Object value) 
//			throws ValidatorException {
//		String msg = null;
//		try {
//			int max = Integer.parseInt(value.toString());
//			boolean exceeded = isLimitExceeded(max);
//			if(exceeded) {
//				msg = "You have exceeded limit for maximum number of students that can be assigned";
//			}
//		} catch (NumberFormatException nfe){
//			msg = "Only numbers allowed";
//		}
//		if(msg != null) {
//			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					msg, null));
//		}
//			
//	}
//	
//	public void validateStudentAssign(FacesContext context, UIComponent component, Object value) 
//			throws ValidatorException {
//		boolean assigned = (boolean) value;
//		int currentNumberOfAssigned = getCurrentNumberOfAssignedStudents();
//		currentNumberOfAssigned += assigned ? 1 : -1;
//		
//		if(currentNumberOfAssigned > maxNumberOfStudents) {
//			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					"You have exceeded limit for maximum number of students that can be assigned", null));
//		}	
//	}
	
	@Override
	public void changePage(int page) {
		if(this.paginationData.getPage() != page) {
			this.paginationData.setPage(page);
			searchStudents();
		}
	}

	public long getCredId() {
		return credId;
	}

	public void setCredId(long credId) {
		this.credId = credId;
	}

	public String getStudentSearchTerm() {
		return studentSearchTerm;
	}

	public void setStudentSearchTerm(String studentSearchTerm) {
		this.studentSearchTerm = studentSearchTerm;
	}

	public CredentialMembersSearchFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CredentialMembersSearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}

	public List<StudentData> getStudents() {
		return students;
	}

	public void setStudents(List<StudentData> students) {
		this.students = students;
	}

	public InstructorData getInstructorForStudentAssign() {
		return instructorForStudentAssign;
	}

	public void setInstructorForStudentAssign(InstructorData instructorForStudentAssign) {
		this.instructorForStudentAssign = instructorForStudentAssign;
	}

	public CredentialMembersSearchFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(CredentialMembersSearchFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	public int getMaxNumberOfStudents() {
		return maxNumberOfStudents;
	}

	public void setMaxNumberOfStudents(int maxNumberOfStudents) {
		this.maxNumberOfStudents = maxNumberOfStudents;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
}
