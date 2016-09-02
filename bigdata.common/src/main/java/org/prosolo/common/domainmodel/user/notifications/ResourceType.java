package org.prosolo.common.domainmodel.user.notifications;

public enum ResourceType {
	
	Competence("Competence1"),
	Activity("Activity1"),
	Comment("Comment1"),
	CredentialAssessment("CredentialAssessment"),
	Credential("Credential1"),
	PostSocialActivity("PostSocialActivity1"), 
	Announcement("Announcement"),
	SocialActivity("SocialActivity1");

	private String dbTableName;
	
	private ResourceType(String dbTableName) {
		this.dbTableName = dbTableName;
	}
	
	public String getDbTableName() {
		return dbTableName;
	}
}
