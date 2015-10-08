package org.prosolo.services.lti.util;

public enum ToolSearchFilterEnum {
	general(""), learningGoalId("Credential"), competenceId("Competence"), activityId("Activity");
		
		private final String val;
		
		private ToolSearchFilterEnum(String val) {
			this.val = val;
		}
	
		public String getVal() {
			return val;
		}
}
