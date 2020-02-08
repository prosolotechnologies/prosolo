package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.user.data.StudentData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.util.ArrayList;
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

    private static Logger logger = Logger.getLogger(AssignStudentToInstructorDialogBean.class);

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

    private List<Long> instructorsToExcludeUserIds = new ArrayList<>();

    public void loadCredentialInstructors(long credentialId, long studentId, boolean loadOnlyActiveInstructors) {
        try {
            StudentData student = credentialManager.getCredentialStudentsData(credentialId, studentId);
            loadCredentialInstructors(credentialId, student, loadOnlyActiveInstructors);
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error loading " + ResourceBundleUtil.getLabel("instructor.plural").toLowerCase());
        }
    }

    public void loadCredentialInstructors(long credentialId, StudentData student, boolean loadOnlyActiveInstructors) {
        studentToAssignInstructor = student.getUser();
        instructor = student.getInstructor();
        if (loadOnlyActiveInstructors) {
            instructorsToExcludeUserIds.clear();
            instructorsToExcludeUserIds.addAll(credInstructorManager.getInactiveCredentialInstructorUserIds(credentialId));
            if (instructor != null) {
                instructorsToExcludeUserIds.remove(instructor.getUser().getId());
            }
        }
        setInstructorSearchTerm("");
        context = PageUtil.getPostParameter("context");
        searchCredentialInstructors(credentialId);
    }

    public void searchCredentialInstructors(long credentialId) {
        PaginatedResult<InstructorData> searchResponse = userTextSearch.searchInstructors(
                loggedUserBean.getOrganizationId(), instructorSearchTerm, -1, -1, credentialId, InstructorSortOption.Date, instructorsToExcludeUserIds);

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
                credInstructorManager.assignStudentToInstructor(studentToAssignInstructor.getId(),
                        ins.getInstructorId(), credentialId, loggedUserBean.getUserContext(ctx));
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
