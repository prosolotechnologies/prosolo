package org.prosolo.common.domainmodel.user.notifications;

public enum ObjectType {
	
	Competence("Competence1"),
	Activity("Activity1"),
	Comment("Comment1");
	
	
	private String dbTableName;
	
	private ObjectType(String dbTableName) {
		this.dbTableName = dbTableName;
	}
	
	public String getDbTableName() {
		return dbTableName;
	}
}
