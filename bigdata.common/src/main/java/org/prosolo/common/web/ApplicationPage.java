package org.prosolo.common.web;

public enum ApplicationPage {

	LOGIN("/login.xhtml"),
	INDEX("/index.xhtml"), 
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
	USER_ADMINISTRATION("/admin/users.xhtml"),
	USER_ADMINISTRATION1("/admin/users1.xhtml"),
	ADMIN_ADMINISTRATION("/admin/adminAdmins.xhtml"),
	ADMIN_EDIT_ORGANIZATION("/admin/adminOrganizationNew.xhtml"),
	ADMIN_EDIT_ADMIN("/admin/adminNew.xhtml"),
	ADMIN_ORGANIZATIONS("/admin/adminOrganizations.xhtml"),
	ADMIN_USER_EDIT_PASSWORD("/admin/adminUserEditPassword.xhtml"),
	ADMIN_USER_EDIT("/admin/adminUserEdit.xhtml"),
	ADMIN_USER_NEW("/admin/adminUserNew.xhtml"),
	ADMIN_MESSAGES("/admin/messages.xhtml"),
	ADMIN_OTHER("/admin/other.xhtml"),
	ADMIN_UNIT_EDIT("/admin/unitEdit.xhtml"),
	ADMIN_UNITS("/admin/units.xhtml"),

	ROLES1("/admin/roles1.xhtml"),
	ROLES("/admin/roles.xhtml"),
	DASHBOARD("/admin/dashboard.xhtml"),
	ADMIN_SETTINGS("/admin/settings.xhtml"),
	ANALYTICS_SETTINGS("/admin/analyticsSettings.xhtml"),
	//INSTRUCTOR_DETAILS("/manage/credential-instructors-add.xhtml"),
	INSTRUCTOR_LIST("/manage/credential-instructors.xhtml"),
	CREDENTIAL_ASSESSMENT_LIST("/manage/credential-assessments.xhtml"),
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
	ACHIEVEMENTS_CREDENTIALS("/achievements-credentials.xhtml"),
	ACHIEVEMENTS_COMPETENCES("/achievements-competences.xhtml"),
	ACHIEVEMENTS_EXTERNAL_COMPETENCES("/achievements-externalcompetences.xhtml"),
	ACHIEVEMENTS_INPROGRESS("/achievements-inprogress.xhtml"),
	MANAGER_CREDENTIAL("/manage/credential.xhtml"),
	MANAGER_COMPETENCE("/manage/competence.xhtml"),
	MANAGER_ACTIVITY("/manage/activity.xhtml"),
	MANAGER_EDIT_CREDENTIAL("/manage/create-credential.xhtml"),
	MANAGER__EDIT_COMPETENCE("/manage/create-competence.xhtml"),
	MANAGER_EDIT_ACTIVITY("/manage/create-activity.xhtml"),
	USER_EDIT_CREDENTIAL("/create-credential.xhtml"),
	USER__EDIT_COMPETENCE("/create-competence.xhtml"),
	USER_EDIT_ACTIVITY("/create-activity.xhtml"),
	PEOPLE("/people.xhtml"),
	RESULTS("/activity-results.xhtml"),
	MANAGER_RESULTS("/manage/activity-results.xhtml"),
	USER_COMPETENCE_LIBRARY("/competenceLibrary.xhtml"),
	MANAGER_COMPETENCE_LIBRARY("/manage/competenceLibrary.xhtml"),
	MANAGER_COMPETENCE_STUDENTS("/manage/competence-students.xhtml"),
	MANAGER_CREDENTIAL_DELIVERIES("/manage/credential-deliveries.xhtml"),
	MANAGER_COMPETENCE_VISIBILITY("/manage/competence-who-can-learn.xhtml"),
	MANAGER_COMPETENCE_PRIVACY("/manage/competence-editors.xhtml"),
	MANAGER_CREDENTIAL_PRIVACY("/manage/credential-editors.xhtml"),
	MANAGER_CREDENTIAL_VISIBILITY("/manage/credential-who-can-learn.xhtml"),

	ADMIN_ORGANIZATION_USERS("/admin/organizationUsers.xhtml"),
	ADMIN_ORGANIZATION_USER_EDIT("/admin/orgUserEdit.xhtml"),
	ADMIN_UNIT_TEACHERS("/admin/unit-teachers.xhtml"),
	ADMIN_UNIT_STUDENTS("/admin/unit-students.xhtml"),
	ADMIN_UNIT_INSTRUCTORS("/admin/unit-instructors.xhtml"),
	ADMIN_UNIT_GROUPS("/admin/unit-groups.xhtml"),
	ADMIN_UNIT_GROUP_USERS("/admin/unit-group-users.xhtml");

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
