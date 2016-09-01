package org.prosolo.common.domainmodel.credential;

public enum CommentedResourceType {

	Competence("Competence1"), 
	Activity("Activity1"),
	SocialActivity("SocialActivity1"),
	ActivityResult("TargetActivity1");
	
private String dbTableName;
	
	private CommentedResourceType(String dbTableName) {
		this.dbTableName = dbTableName;
	}
	
	public String getDbTableName() {
		return dbTableName;
	}
}
