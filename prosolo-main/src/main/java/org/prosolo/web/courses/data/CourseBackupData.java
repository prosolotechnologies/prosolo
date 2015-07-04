package org.prosolo.web.courses.data;

import java.io.Serializable;

/**
 *
 * @author Zoran Jeremic Apr 20, 2014
 *
 */

public class CourseBackupData implements Serializable {
	
	private static final long serialVersionUID = 2801833408965796874L;

	private String filename;
	private String path;
	private String link;
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
}
