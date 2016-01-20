package org.prosolo.web;

public enum ApplicationPages {

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
	STUDENT_LIST("/manage/studentList.xhtml"),
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
	ANALYTICS_SETTINGS("/admin/analyticsSettings.xhtml");
	

    private String uri; 
    
    ApplicationPages(String uri) {
        this.uri = uri;
    }

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
    
}
