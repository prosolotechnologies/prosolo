/**
 * 
 */
package org.prosolo.web.courses.credential;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.TextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "studentEnrollBean")
@Component("studentEnrollBean")
@Scope("view")
public class StudentEnrollBean implements Serializable, Paginable {

	private static final long serialVersionUID = -9062234630459921609L;

	private static Logger logger = Logger.getLogger(StudentEnrollBean.class);

	@Inject private TextSearch textSearch;
	@Inject private CredentialManager credManager;
	@Inject private RoleManager roleManager;
	@Inject private LoggedUserBean loggedUserBean;

	private long credId;

	private String studentSearchTerm;
	private PaginationData paginationData = new PaginationData(3);
	
	private String context;

	private List<StudentData> students;
	private List<Long> studentsToEnroll;
	
	private long userRoleId;
	
	public void init(long credId, String context) {
		this.credId = credId;
		this.context = context;
	}
	
	public void prepareStudentEnroll() {
		if(userRoleId == 0) {
			Role role = roleManager.getRoleByName("User");
			if(role != null) {
				userRoleId = role.getId();
			}
		}
		studentSearchTerm = "";
		paginationData.setPage(1);
		studentsToEnroll = new ArrayList<>();
		searchStudents();
	}
	
	public void searchStudents() {
		TextSearchResponse1<StudentData> result = textSearch
				.searchUnenrolledUsersWithUserRole(studentSearchTerm, paginationData.getPage() - 1, paginationData.getLimit(), credId, userRoleId);
		students = result.getFoundNodes();
		setCurrentlyEnrolledStudents();
		paginationData.update((int) result.getHitsNumber());
	}
	
//	public void updateMaxNumberOfStudents() {
//		logger.info("Max number of students updated " + instructorForStudentAssign
//				.getMaxNumberOfStudents());
//	}
	
	/**
	 * set enrolled to true for students that are currently enrolled in memory
	 *
	 */
	private void setCurrentlyEnrolledStudents() {
		if(students != null) {
			for(StudentData sd : students) {
				sd.setEnrolled(checkIfExists(sd.getUser().getId(), studentsToEnroll));
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

	public void studentEnrollChecked(int index) {
		StudentData sd = students.get(index);
		if(sd.isEnrolled()) {
			studentsToEnroll.add(sd.getUser().getId());
		} else {
			studentsToEnroll.remove(new Long(sd.getUser().getId()));
		}
	}
	
	public void enrollStudents() {
		try {
			String page = PageUtil.getPostParameter("page");
			String service = PageUtil.getPostParameter("service");
			credManager.enrollStudentsInCredential(credId, loggedUserBean.getUserId(), studentsToEnroll,
					new LearningContextData(page, context, service));
			PageUtil.fireSuccessfulInfoMessage("Changes are saved");
		} catch(DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	public void resetAndSearch() {
		this.paginationData.setPage(1);
		searchStudents();	
	}
	
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

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public List<StudentData> getStudents() {
		return students;
	}

	public void setStudents(List<StudentData> students) {
		this.students = students;
	}

}
