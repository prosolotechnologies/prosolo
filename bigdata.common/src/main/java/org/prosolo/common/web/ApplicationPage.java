package org.prosolo.common.web;

public enum ApplicationPage {

	LOGIN("/login.xhtml"),
	INDEX("/index.xhtml"),
	 ROOT("/root.xhtml"),
	//LEARN("/learn.xhtml"), 
	//PLAN("/plan.xhtml"), 
	//BROWSE_CREDENTIALS("/courseBrowse.xhtml"),
	//COURSE("/course.xhtml"),
	PROFILE("/profile.xhtml"),
	PUBLIC_PROFILE("/publicprofile.xhtml"),
	MESSAGES("/messages.xhtml"),
	DIGEST("/digest.xhtml"),
	COMMUNICATIONS("/communications.xhtml"),
	SEARCH("/search.xhtml"),
	SETTINGS_OLD("/admin/settings_old.xhtml"),
	SETTINGS("/settings.xhtml"),
	//COMPETENCES("/manage/competences.xhtml"),
	//COURSES("/manage/courses.xhtml"),
	REPORTS("/manage/reports.xhtml"),
	STUDENT_LIST("/manage/credential-students.xhtml"),
	STUDENT_PROFILE("/manage/studentProfile.xhtml"),
	//COMPETENCE("/manage/competence.xhtml"),
	//COURSE_MANAGE("/manage/course.xhtml"),
	EXTERNAL_TOOLS("/manage/tools.xhtml"),
	EXTERNAL_TOOL_DETAILS("/manage/externalTools/toolDetails.xhtml"),
	CREDENTIAL_FEEDS("/manage/credential-feeds.xhtml"),
	ADMIN_EDIT_ORGANIZATION("/admin/organization-settings.xhtml"),
	ADMIN_EDIT_ADMIN("/admin/adminNew.xhtml"),
	ADMIN_ORGANIZATIONS("/admin/adminOrganizations.xhtml"),
	ADMIN_USER_EDIT_PASSWORD("/admin/adminUserEditPassword.xhtml"),
	ADMIN_USER_NEW("/admin/adminUserNew.xhtml"),
	ADMIN_MESSAGES("/admin/messages.xhtml"),
	ADMIN_OTHER("/admin/other.xhtml"),
	ADMIN_UNIT_EDIT("/admin/unit-settings.xhtml"),
	ADMIN_UNITS("/admin/units.xhtml"),

	ROLES1("/admin/roles1.xhtml"),
	ROLES("/admin/roles.xhtml"),
	DASHBOARD("/admin/dashboard.xhtml"),
	ADMIN_SETTINGS("/admin/settings.xhtml"),
	ANALYTICS_SETTINGS("/admin/analyticsSettings.xhtml"),
	//INSTRUCTOR_DETAILS("/manage/credential-instructors-add.xhtml"),
	INSTRUCTOR_LIST("/manage/credential-instructors.xhtml"),
	MANAGE_CREDENTIAL_ASSESSMENTS("/manage/credential-delivery-assessments.xhtml"),
	CREDENTIAL_ASSESSMENT_MANAGE("/manage/credential-assessment.xhtml"),
	CREDENTIAL_ASSESSMENT("/credential-assessment.xhtml"),
	CREDENTIAL_ANNOUNCEMENTS("/manage/announcements.xhtml"),
	ANNOUNCEMENT("/announcement.xhtml"),
	ASSESSMENT("/assessments.xhtml"),
	//STUDENT_REASSIGN("/manage/credential-instructors-reassign.xhtml"),
	LTI_PROVIDER_LAUNCH("/ltiproviderlaunch.xhtml"),
	//COMPETENCE_ACTIVITIES("/manage/competence-activities.xhtml"),
	EMAIL("/email.xhtml"),
	USER_CREDENTIAL("/credential.xhtml"),
	USER_COMPETENCE("/competence.xhtml"),
	USER_ACTIVITY("/activity.xhtml"),
	USER_CREDENTIAL_LIBRARY("/credentialLibrary.xhtml"),
	MANAGER_CREDENTIAL_LIBRARY("/manage/credentialLibrary.xhtml"),
	EVIDENCES("/evidences.xhtml"),
	EVIDENCE("/evidence.xhtml"),
	EVIDENCE_EDIT("/evidence-edit.xhtml"),

	MANAGER_CREDENTIAL("/manage/credential.xhtml"),
	MANAGER_COMPETENCE("/manage/competence.xhtml"),
	MANAGER_ACTIVITY("/manage/activity.xhtml"),
	MANAGER_EDIT_CREDENTIAL("/manage/credential-create.xhtml"),
	MANAGER__EDIT_COMPETENCE("/manage/create-competence.xhtml"),
	MANAGER_EDIT_ACTIVITY("/manage/create-activity.xhtml"),
	USER_EDIT_CREDENTIAL("/credential-create.xhtml"),
	USER__EDIT_COMPETENCE("/create-competence.xhtml"),
	USER_EDIT_ACTIVITY("/create-activity.xhtml"),
	PEOPLE("/people.xhtml"),
	RESULTS("/activity-responses.xhtml"),
	USER_COMPETENCE_LIBRARY("/competenceLibrary.xhtml"),
	MANAGER_COMPETENCE_LIBRARY("/manage/competenceLibrary.xhtml"),
	MANAGER_COMPETENCE_STUDENTS("/manage/competence-students.xhtml"),
	MANAGER_CREDENTIAL_DELIVERIES("/manage/credential-deliveries.xhtml"),
	MANAGER_COMPETENCE_VISIBILITY("/manage/competence-who-can-learn.xhtml"),
	MANAGER_COMPETENCE_EDITORS("/manage/competence-editors.xhtml"),
	MANAGER_CREDENTIAL_EDITORS("/manage/credential-editors.xhtml"),
	MANAGER_CREDENTIAL_VISIBILITY("/manage/credential-who-can-learn.xhtml"),
	MANAGER_CREDENTIAL_PRIVACY("/manage/credential-privacy.xhtml"),
	MANAGER_COMPETENCE_PRIVACY("/manage/competence-privacy.xhtml"),
	MANAGER_RUBRIC_LIBRARY("/manage/rubricLibrary.xhtml"),
	MANAGER_RUBRIC_SETTINGS("/manage/rubricEdit.xhtml"),
	MANAGER_RUBRIC_PRIVACY("/manage/rubric-privacy.xhtml"),
	MANAGER_RUBRIC_CRITERIA("/manage/rubric-criteria.xhtml"),
	MANAGER_CREDENTIAL_ACTIVITY_ASSESSMENTS("/manage/credential-delivery-assessments-activity.xhtml"),
	//MANAGER_CREDENTIAL_ACTIVITY_USER_ASSESSMENT("/manage/credential-delivery-activity-assessment.xhtml"),

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
	ADMIN_MIGRATIONS("/admin/migrations.xhtml"),
	;

    private String uri; 
    
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
