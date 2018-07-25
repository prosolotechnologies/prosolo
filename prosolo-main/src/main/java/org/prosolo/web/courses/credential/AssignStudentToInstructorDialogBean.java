package org.prosolo.web.courses.credential;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
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

    private UserData studentToAssignInstructor;
    private InstructorData instructor;

    private String instructorSearchTerm = "";
    private List<InstructorData> credentialInstructors;

    private LastAction lastAction;
    private String context;

    public void loadCredentialInstructors(long credentialId, long studentId) {
        StudentData student = credentialManager.getCredentialStudentsData(credentialId, studentId);
        loadCredentialInstructors(credentialId, student);
    }

    public void loadCredentialInstructors(long credentialId, StudentData student) {
        studentToAssignInstructor = student.getUser();
        instructor = student.getInstructor();
        setInstructorSearchTerm("");
        context = PageUtil.getPostParameter("context");
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
    public void selectInstructor(long credentialId, InstructorData ins) {
        try {
            String page = PageUtil.getPage();
            String service = PageUtil.getPostParameter("service");
            PageContextData ctx = new PageContextData(page, context, service);

            if (instructor == null
                    || instructor.getInstructorId() != ins.getInstructorId()) {
                long formerInstructoruserId = instructor != null
                        ? instructor.getUser().getId()
                        : 0;
                credInstructorManager.assignStudentToInstructor(studentToAssignInstructor.getId(),
                        ins.getInstructorId(), credentialId, formerInstructoruserId,
                        loggedUserBean.getUserContext(ctx));
                if (instructor == null) {
                    lastAction = LastAction.ASSIGNED;
                } else {
                    lastAction = LastAction.REASSIGNED;
                }
                instructor = ins;
            } else {
                credInstructorManager.unassignStudentFromInstructor(
                        studentToAssignInstructor.getId(), credentialId, loggedUserBean.getUserContext(ctx));
                instructor = null;
                lastAction = LastAction.UNASSIGNED;
            }

            credentialInstructors = null;
            PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getLabel("instructor") + " has been updated");
        } catch (DbConnectionException e) {
            PageUtil.fireErrorMessage(e.getMessage());
        }
    }

    public boolean isInstructorCurrentlyAssignedToStudent(InstructorData ins) {
        InstructorData inst = instructor;
        return inst != null && inst.getInstructorId() == ins.getInstructorId();
    }

    public boolean areInstructorAndStudentSameUser(InstructorData id) {
        return id.getUser().getId() == studentToAssignInstructor.getId();
    }


    /**
     * GETTERS & SETTERS
     */

    public UserData getStudentToAssignInstructor() {
        return studentToAssignInstructor;
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

    public InstructorData getInstructor() {
        return instructor;
    }

    public LastAction getLastAction() {
        return lastAction;
    }

    public enum LastAction {
        ASSIGNED,
        REASSIGNED,
        UNASSIGNED;
    }
}
