package org.prosolo.common.domainmodel.user.notifications;

public enum ResourceType {
	
	Competence("Competence1"),
	Activity("Activity1"),
	Comment("Comment1"),
	CredentialAssessment("CredentialAssessment"),
	CompetenceAssessment("CompetenceAssessment"),
	ActivityAssessment("ActivityAssessment"),
	Credential("Credential1"),
	PostSocialActivity("PostSocialActivity1"), 
	Announcement("Announcement"),
	SocialActivity("SocialActivity1"),
	ActivityResult("TargetActivity1"),
    Student("User");

	private String dbTableName;
	
	ResourceType(String dbTableName) {
		this.dbTableName = dbTableName;
	}
	
	public String getDbTableName() {
		return dbTableName;
	}
}
