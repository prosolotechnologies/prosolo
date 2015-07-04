/**
 * 
 */
package org.prosolo.services.rest.courses.data;

/**
 * @author "Nikola Milikic"
 * 
 */
public class SerieJsonData {

	private SerieType name;
	private long start;
	private long duration;

	public SerieType getName() {
		return name;
	}

	public void setName(SerieType name) {
		this.name = name;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

}
