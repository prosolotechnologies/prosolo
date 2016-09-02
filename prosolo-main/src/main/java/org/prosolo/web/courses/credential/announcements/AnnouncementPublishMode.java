package org.prosolo.web.courses.credential.announcements;

public enum AnnouncementPublishMode {
	
	ALL_STUDENTS("All"),
	ACTIVE_STUDENTS("Active");
	
	private AnnouncementPublishMode(String text) {
		this.text = text;
	}
	
	private final String text;

	public String getText() {
		return text;
	}
	
	public static AnnouncementPublishMode fromString(String text) {
	    if (text != null) {
	      for (AnnouncementPublishMode b : AnnouncementPublishMode.values()) {
	        if (text.equalsIgnoreCase(b.text)) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }
}
