/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.InstructorAssignFilter;
import org.prosolo.search.util.credential.InstructorAssignFilterValue;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.util.pagination.Paginable;
import org.prosolo.web.courses.util.pagination.PaginationLink;
import org.prosolo.web.courses.util.pagination.Paginator;
import org.prosolo.web.util.PageUtil;
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

	@Inject private TextSearch textSearch;
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private LoggedUserBean loggedUserBean;
	@Inject private EventFactory eventFactory;
	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private NodeChangeObserver nodeChangeObserver;

	private long credId;

	private String studentSearchTerm;
	private int studentsNumber;
	private int page = 1;
	private int limit = 3;
	private List<PaginationLink> paginationLinks;
	private int numberOfPages;
	private InstructorAssignFilter searchFilter;
	private InstructorAssignFilter[] searchFilters;
	
	private InstructorData instructorForStudentAssign;
	private int maxNumberOfStudents;
	
	private String context;

	private List<StudentData> students;
	private List<Long> studentsToAssign;
	private List<Long> studentsToUnassign;
	
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
		searchFilter = new InstructorAssignFilter(InstructorAssignFilterValue.All, 0);
		instructorForStudentAssign = id;
		maxNumberOfStudents = instructorForStudentAssign.getMaxNumberOfStudents();
		studentSearchTerm = "";
		studentsToAssign = new ArrayList<>();
		studentsToUnassign = new ArrayList<>();
		searchStudents();
	}
	
	public void searchStudents() {
		TextSearchResponse1<StudentData> result = textSearch
				.searchUnassignedAndStudentsAssignedToInstructor(studentSearchTerm, credId, 
						instructorForStudentAssign.getUser().getId(), searchFilter.getFilter(), 
						page - 1, limit);
		students = result.getFoundNodes();
		studentsNumber = (int) result.getHitsNumber();
		Map<String, Object> additional = result.getAdditionalInfo();
		if(additional != null) {
			searchFilters = (InstructorAssignFilter[]) additional.get("filters");
			searchFilter = (InstructorAssignFilter) additional.get("selectedFilter");
		}
		generatePagination();
	}
	
//	public void updateMaxNumberOfStudents() {
//		logger.info("Max number of students updated " + instructorForStudentAssign
//				.getMaxNumberOfStudents());
//	}
	
	public void studentAssignChecked(int index) {
		StudentData sd = students.get(index);
		if(sd.isAssigned()) {
			if(sd.getInstructor() != null) {
				studentsToUnassign.remove(new Long(sd.getUser().getId()));
			} else {
				studentsToAssign.add(sd.getUser().getId());
			}
		} else {
			if(sd.getInstructor() != null) {
				studentsToUnassign.add(sd.getUser().getId());
			} else {
				studentsToAssign.remove(new Long(sd.getUser().getId()));
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
				credInstructorManager.updateInstructorAndStudentsAssigned(credId, 
						instructorForStudentAssign, studentsToAssign, studentsToUnassign);
				fireAssignEvent(instructorForStudentAssign, studentsToAssign, page, lContext, service);
				fireUnassignEvent(instructorForStudentAssign, studentsToUnassign, page, lContext, service);
				int numberOfAffectedStudents = studentsToAssign.size() - studentsToUnassign.size();
				instructorForStudentAssign.setNumberOfAssignedStudents(
						instructorForStudentAssign.getNumberOfAssignedStudents() + numberOfAffectedStudents);
				//instructorForStudentAssign = null;
				PageUtil.fireSuccessfulInfoMessage("Changes are saved");
			}
		} catch(DbConnectionException e) {
			logger.error(e);
			instructorForStudentAssign.setMaxNumberOfStudentsToOriginalValue();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	private void generatePagination() {
		//if we don't want to generate all links
		Paginator paginator = new Paginator(studentsNumber, limit, page, 
				1, "...");
		//if we want to genearate all links in paginator
//		Paginator paginator = new Paginator(courseMembersNumber, limit, page, 
//				true, "...");
		numberOfPages = paginator.getNumberOfPages();
		paginationLinks = paginator.generatePaginationLinks();
	}
	
	public void resetAndSearch() {
		this.page = 1;
		searchStudents();	
	}
	
	public void applySearchFilter(InstructorAssignFilter filter) {
		this.searchFilter = filter;
		this.page = 1;
		searchStudents();
	}
	
	public void fireAssignEvent(InstructorData id, List<Long> studentsToAssign, String page, 
			String context, String service) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("credId", credId + "");
		
		for (Long userId : studentsToAssign) {
			try {
				User target = new User();
				target.setId(id.getUser().getId());
				User object = new User();
				object.setId(userId);
				
				@SuppressWarnings("unchecked")
				Event event = eventFactory.generateEvent(
						EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR, 
						loggedUserBean.getUser(), object, target, 
						null, page, context, service, 
						new Class[] {NodeChangeObserver.class}, parameters);
				nodeChangeObserver.handleEvent(event);
			} catch (EventException e) {
				logger.error(e);
			}
		}
	}
	
	public void fireUnassignEvent(InstructorData id, List<Long> studentsToAssign, String page, 
			String context, String service) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("credId", credId + "");
		
		for (Long userId : studentsToAssign) {
			try {
				User target = new User();
				target.setId(id.getUser().getId());
				User object = new User();
				object.setId(userId);
				
				@SuppressWarnings("unchecked")
				Event event = eventFactory.generateEvent(
						EventType.STUDENT_UNASSIGNED_FROM_INSTRUCTOR, 
						loggedUserBean.getUser(), object, target, 
						null, page, context, service, 
						new Class[] {NodeChangeObserver.class}, parameters);
				nodeChangeObserver.handleEvent(event);
			} catch (EventException e) {
				logger.error(e);
			}
		}
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
			searchStudents();
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
		return studentsNumber == 0;
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

	public List<PaginationLink> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<PaginationLink> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

	public InstructorAssignFilter getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(InstructorAssignFilter searchFilter) {
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

	public InstructorAssignFilter[] getSearchFilters() {
		return searchFilters;
	}

	public void setSearchFilters(InstructorAssignFilter[] searchFilters) {
		this.searchFilters = searchFilters;
	}

	public int getMaxNumberOfStudents() {
		return maxNumberOfStudents;
	}

	public void setMaxNumberOfStudents(int maxNumberOfStudents) {
		this.maxNumberOfStudents = maxNumberOfStudents;
	}

}
