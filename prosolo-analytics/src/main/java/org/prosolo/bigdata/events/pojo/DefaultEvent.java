package org.prosolo.bigdata.events.pojo;

import java.io.Serializable;

import org.prosolo.bigdata.streaming.Topic;

/**
 * @author Zoran Jeremic Apr 5, 2015
 *
 */

public class DefaultEvent implements Serializable, Comparable<Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6560789959659890575L;
	private Topic topic;
	private String eventType;
	private Long timestamp;

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@Override
	public int compareTo(Object o) {
		DefaultEvent f = (DefaultEvent) o;
		if (this.getTimestamp() > f.getTimestamp()) {
			return 1;
		} else if (this.getTimestamp() < f.getTimestamp()) {
			return -1;
		} else {
			return 0;
		}
	}

}
