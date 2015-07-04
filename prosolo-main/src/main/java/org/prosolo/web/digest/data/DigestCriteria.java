package org.prosolo.web.digest.data;


/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class DigestCriteria {
	
	private String filter;
	private String date;
	private String interval;
	
	public String getFilter() {
		return filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}
	
	
	
}
