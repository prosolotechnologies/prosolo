package org.prosolo.web;

public enum ApplicationPage {

	LOGIN("/login.xhtml"),
	INDEX("/index.xhtml"), 
	LEARN("/learn.xhtml"), 
	PLAN("/plan.xhtml"), 
	BROWSE_CREDENTIALS("/courseBrowse.xhtml"),
	CREDENTIAL("/course.xhtml"),
	PROFILE("/profile.xhtml"),
	PUBLIC_PROFILE("/publicprofile.xhtml"),
	MESSAGES("/messages.xhtml"),
	DIGEST("/digest.xhtml"),
	COMMUNICATIONS("/communications.xhtml"),
	SEARCH("/search.xhtml"),
	SETTINGS("/settings.xhtml"),
	COMPETENCES("/manage/competences.xhtml"),
	CREDENTIALS("/manage/courses.xhtml"),
	REPORTS("/manage/reports.xhtml"),
	STUDENT_LIST("/manage/credential-students.xhtml"),
	STUDENT_PROFILE("/manage/studentProfile.xhtml"),
	COMPETENCE("/manage/competence.xhtml"),
	CREDENTIAL_MANAGE("/manage/course.xhtml"),
	EXTERNAL_TOOLS("/manage/tools.xhtml"),
	EXTERNAL_TOOL_DETAILS("/manage/externalTools/toolDetails.xhtml"),
	COURSE_FEEDS("/manage/courseFeeds.xhtml"),
	USER_ADMINISTRATION("/admin/users.xhtml"),
	ROLES("/admin/roles.xhtml"),
	DASHBOARD("/admin/dashboard.xhtml"),
	ADMIN_SETTINGS("/admin/settings.xhtml"),
	ANALYTICS_SETTINGS("/admin/analyticsSettings.xhtml"),
	INSTRUCTOR_DETAILS("/manage/credential-instructors-add.xhtml"),
	INSTRUCTOR_LIST("/manage/credential-instructors.xhtml"),
	STUDENT_REASSIGN("/manage/credential-instructors-reassign.xhtml"),
	LTI_PROVIDER_LAUNCH("/ltiproviderlaunch.xhtml");

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
