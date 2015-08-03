package org.prosolo.common.messaging.data;

import java.io.Serializable;

public  class MessageWrapper implements Serializable{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sender;
	private long timecreated=0;
	private SimpleMessage message;
	
	
	
	public long getTimecreated() {
		return timecreated;
	}
	public void setTimecreated(long timecreated) {
		this.timecreated = timecreated;
	}
	public SimpleMessage getMessage() {
		return message;
	}
	public void setMessage(SimpleMessage message) {
		this.message = message;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
}
