package org.prosolo.web.courses.credential;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.List;

/**
 * This bean is used for the modal where students are assigned to instructors.
 *
 * @author Nikola Milikic
 * @date 2018-07-23
 * @since 1.2
 */
@ManagedBean(name = "assignStudentToInstructorDialogBean")
@Component("assignStudentToInstructorDialogBean")
@Scope("view")
public class AssignStudentToInstructorDialogBean {

    @Inject
    private UserTextSearch userTextSearch;
    @Inject
    private CredentialInstructorManager credInstructorManager;
    @Inject
    private CredentialManager credentialManager;
    @Inject
    private LoggedUserBean loggedUserBean;

    private StudentData studentToAssignInstructor;
    private String instructorSearchTerm = "";
    private List<InstructorData> credentialInstructors;

    private String context;

    public void loadCredentialInstructors(long credentialId, long studentId) {
        StudentData student = credentialManager.getCredentialStudentsData(credentialId, studentId);
        loadCredentialInstructors(credentialId, student);
    }

    public void loadCredentialInstructors(long credentialId, StudentData student) {
        studentToAssignInstructor = student;
        setInstructorSearchTerm("");
        context += "|context:/name:USER|id:" + student.getUser().getId() + "/";
        loadCredentialInstructors(credentialId);
    }

    public void loadCredentialInstructors(long credentialId) {
        PaginatedResult<InstructorData> searchResponse = userTextSearch.searchInstructors(
                loggedUserBean.getOrganizationId(), instructorSearchTerm, -1, -1, credentialId, InstructorSortOption.Date, null);

        if (searchResponse != null) {
            credentialInstructors = searchResponse.getFoundNodes();
        }
    }

    //assign student to an instructor
    public void selectInstructor(long credentialId, InstructorData instructor) {
        try {
            String page = PageUtil.getPostParameter("page");
            String service = PageUtil.getPostParameter("service");
            PageContextData ctx = new PageContextData(page, context, service);
            String action = null;

            if (studentToAssignInstructor.getInstructor() == null
                    || studentToAssignInstructor.getInstructor().getInstructorId() != instructor.getInstructorId()) {
                long formerInstructoruserId = studentToAssignInstructor.getInstructor() != null
                        ? studentToAssignInstructor.getInstructor().getUser().getId()
                        : 0;
                credInstructorManager.assignStudentToInstructor(studentToAssignInstructor.getUser().getId(),
                        instructor.getInstructorId(), credentialId, formerInstructoruserId,
                        loggedUserBean.getUserContext(ctx));
                if (studentToAssignInstructor.getInstructor() == null) {
                    action = "assigned";
                    //update filters if student was unassigned
                    //updateFiltersStudentAssigned();
                } else {
                    action = "reassigned";
                }
                studentToAssignInstructor.setInstructor(instructor);
            } else {
                credInstructorManager.unassignStudentFromInstructor(
                        studentToAssignInstructor.getUser().getId(), credentialId, loggedUserBean.getUserContext(ctx));
                studentToAssignInstructor.setInstructor(null);
                action = "unassigned";
                //updateFiltersStudentUnassigned();
            }

            studentToAssignInstructor = null;
            credentialInstructors = null;
            PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getLabel("instructor").toLowerCase() + " has been " + action);
        } catch (DbConnectionException e) {
            PageUtil.fireErrorMessage(e.getMessage());
        }
    }

    public boolean isInstructorCurrentlyAssignedToStudent(InstructorData id) {
        InstructorData inst = studentToAssignInstructor.getInstructor();
        return inst != null && inst.getInstructorId() == id.getInstructorId();
    }

    public boolean areInstructorAndStudentSameUser(InstructorData id) {
        return id.getUser().getId() == studentToAssignInstructor.getUser().getId();
    }


    /**
     * GETTERS & SETTERS
     */

    public StudentData getStudentToAssignInstructor() {
        return studentToAssignInstructor;
    }

    public void setStudentToAssignInstructor(StudentData studentToAssignInstructor) {
        this.studentToAssignInstructor = studentToAssignInstructor;
    }

    public void setInstructorSearchTerm(String instructorSearchTerm) {
        this.instructorSearchTerm = instructorSearchTerm;
    }

    public String getInstructorSearchTerm() {
        return instructorSearchTerm;
    }

    public List<InstructorData> getCredentialInstructors() {
        return credentialInstructors;
    }

    public void setCredentialInstructors(List<InstructorData> credentialInstructors) {
        this.credentialInstructors = credentialInstructors;
    }
}
