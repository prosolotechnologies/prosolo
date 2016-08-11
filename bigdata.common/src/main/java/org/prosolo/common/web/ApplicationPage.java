package org.prosolo.common.web;

public enum ApplicationPage {

	LOGIN("/login.xhtml"),
	INDEX("/index.xhtml"), 
	LEARN("/learn.xhtml"), 
	//PLAN("/plan.xhtml"), 
	//BROWSE_CREDENTIALS("/courseBrowse.xhtml"),
	//COURSE("/course.xhtml"),
	PROFILE("/profile.xhtml"),
	PUBLIC_PROFILE("/publicprofile.xhtml"),
	MESSAGES("/messages.xhtml"),
	DIGEST("/digest.xhtml"),
	COMMUNICATIONS("/communications.xhtml"),
	SEARCH("/search.xhtml"),
	SETTINGS_OLD("/settings_old.xhtml"),
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
	PEOPLE("/people.xhtml");
	
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
