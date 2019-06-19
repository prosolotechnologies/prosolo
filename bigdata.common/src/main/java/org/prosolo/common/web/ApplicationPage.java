package org.prosolo.common.web;

import java.util.HashMap;
import java.util.Map;

public enum ApplicationPage {

	LOGIN("/login.xhtml"),
	INDEX("/index.xhtml"),
	ROOT("/root.xhtml"),
	PROFILE("/profile.xhtml"),
	MESSAGES("/messages.xhtml"),
	SETTINGS("/settings.xhtml"),
	CREDENTIAL_PEER_ASSESSMENT("/credential-assessment.xhtml"),
	CREDENTIAL_INSTRUCTOR_ASSESSMENT("/credential-instructor-assessment.xhtml"),
	COMPETENCE_PEER_ASSESSMENT("/competence-peer-assessment.xhtml"),
	COMPETENCE_INSTRUCTOR_ASSESSMENT("/competence-instructor-assessment.xhtml"),
	ANNOUNCEMENT("/announcement.xhtml"),
	LTI_PROVIDER_LAUNCH("/ltiproviderlaunch.xhtml"),
	EMAIL("/email.xhtml"),
	USER_CREDENTIAL("/credential.xhtml"),
	USER_COMPETENCE("/competence.xhtml"),
	USER_ACTIVITY("/activity.xhtml"),
	USER_CREDENTIAL_LIBRARY("/credentialLibrary.xhtml"),
	EVIDENCES("/evidence-repository.xhtml"),
	EVIDENCE("/evidence.xhtml"),
	EVIDENCE_EDIT("/evidence-edit.xhtml"),
	USER_EDIT_CREDENTIAL("/credential-create.xhtml"),
	USER__EDIT_COMPETENCE("/create-competence.xhtml"),
	USER_EDIT_ACTIVITY("/create-activity.xhtml"),
	PEOPLE("/people.xhtml"),
	RESULTS("/activity-responses.xhtml"),
	USER_COMPETENCE_LIBRARY("/competenceLibrary.xhtml"),
	MY_ASSESSMENTS_CREDENTIAL("/my-assessments-credentials.xhtml"),
	MY_ASSESSMENTS_CREDENTIAL_ASSESSMENT("/my-assessments-credentials.xhtml"),
	MY_ASSESSMENTS_COMPETENCES("/my-assessments-competences.xhtml"),
	MY_ASSESSMENTS_COMPETENCE_ASSESSMENT("/my-assessments-competences-assessment.xhtml"),


	MANAGER_CREDENTIAL_LIBRARY("/manage/credentialLibrary.xhtml"),
	MANAGER_CREDENTIAL("/manage/credential.xhtml"),
	MANAGER_COMPETENCE("/manage/competence.xhtml"),
	MANAGER_ACTIVITY("/manage/activity.xhtml"),
	MANAGER_EDIT_CREDENTIAL("/manage/credential-create.xhtml"),
	MANAGER__EDIT_COMPETENCE("/manage/create-competence.xhtml"),
	MANAGER_EDIT_ACTIVITY("/manage/create-activity.xhtml"),
	MANAGER_COMPETENCE_LIBRARY("/manage/competenceLibrary.xhtml"),
	MANAGER_COMPETENCE_STUDENTS("/manage/competence-students.xhtml"),
	MANAGER_CREDENTIAL_DELIVERIES("/manage/credential-deliveries.xhtml"),
	MANAGER_COMPETENCE_VISIBILITY("/manage/competence-who-can-learn.xhtml"),
	MANAGER_COMPETENCE_EDITORS("/manage/competence-editors.xhtml"),
	MANAGER_CREDENTIAL_EDITORS("/manage/credential-editors.xhtml"),
	MANAGER_CREDENTIAL_VISIBILITY("/manage/credential-who-can-learn.xhtml"),
	MANAGER_CREDENTIAL_PRIVACY("/manage/credential-privacy.xhtml"),
	MANAGER_COMPETENCE_PRIVACY("/manage/competence-privacy.xhtml"),
	CREDENTIAL_ANNOUNCEMENTS("/manage/announcements.xhtml"),
	INSTRUCTOR_LIST("/manage/credential-instructors.xhtml"),
	MANAGE_CREDENTIAL_ASSESSMENTS("/manage/credential-delivery-assessments.xhtml"),
	CREDENTIAL_ASSESSMENT_MANAGE("/manage/credential-assessment.xhtml"),
	MANAGER_RUBRIC_LIBRARY("/manage/rubricLibrary.xhtml"),
	MANAGER_RUBRIC_SETTINGS("/manage/rubricEdit.xhtml"),
	MANAGER_RUBRIC_PRIVACY("/manage/rubric-privacy.xhtml"),
	MANAGER_RUBRIC_CRITERIA("/manage/rubric-criteria.xhtml"),
	MANAGER_CREDENTIAL_ACTIVITY_ASSESSMENTS("/manage/credential-delivery-assessments-activity.xhtml"),
	REPORTS("/manage/reports.xhtml"),
	STUDENT_LIST("/manage/credential-students.xhtml"),
	STUDENT_PROFILE("/manage/studentProfile.xhtml"),
	EXTERNAL_TOOLS("/manage/tools.xhtml"),
	EXTERNAL_TOOL_DETAILS("/manage/externalTools/toolDetails.xhtml"),


	ROLES("/admin/roles.xhtml"),
	ADMIN_SETTINGS("/admin/settings.xhtml"),
	ADMIN_EDIT_ORGANIZATION("/admin/organization-settings.xhtml"),
	ADMIN_EDIT_ADMIN("/admin/adminNew.xhtml"),
	ADMIN_ORGANIZATIONS("/admin/adminOrganizations.xhtml"),
	ADMIN_USER_EDIT_PASSWORD("/admin/adminUserEditPassword.xhtml"),
	ADMIN_USER_NEW("/admin/adminUserNew.xhtml"),
	ADMIN_OTHER("/admin/other.xhtml"),
	ADMIN_UNIT_EDIT("/admin/unit-settings.xhtml"),
	ADMIN_UNITS("/admin/units.xhtml"),
	ADMIN_ORGANIZATION_USERS("/admin/organizationUsers.xhtml"),
	ADMIN_ORGANIZATION_USER_EDIT("/admin/organizationUserEdit.xhtml"),
	ADMIN_UNIT_TEACHERS("/admin/unit-teachers.xhtml"),
	ADMIN_UNIT_STUDENTS("/admin/unit-students.xhtml"),
	ADMIN_UNIT_INSTRUCTORS("/admin/unit-instructors.xhtml"),
	ADMIN_UNIT_GROUPS("/admin/unit-groups.xhtml"),
	ADMIN_UNIT_GROUP_USERS("/admin/unit-group-users.xhtml"),
	ADMIN_UNIT_CREDENTIALS("/admin/unit-credentials.xhtml"),
	ADMIN_CREDENTIAL("/admin/credential.xhtml"),
	ADMIN_CREDENTIAL_WHO_CAN_LEARN("/admin/credential-who-can-learn.xhtml"),
	ADMIN_COMPETENCE("/admin/competence.xhtml"),
	ADMIN_ACTIVITY("/admin/activity.xhtml"),
	ADMIN_DATA_INIT("/admin/data-init.xhtml");

    private String uri;
	private static Map<String,ApplicationPage> inversePageMap;

	public static ApplicationPage getPageForURI(String uri) {
		if (inversePageMap == null) {
			inversePageMap = new HashMap<>();
			for (ApplicationPage ap : ApplicationPage.values()) {
				inversePageMap.put(ap.getUri(), ap);
			}
		}
		return inversePageMap.get(uri);
	}
    
    ApplicationPage(String uri) {
        this.uri = uri;
    }

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
    
}
