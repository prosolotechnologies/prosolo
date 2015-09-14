package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MessageHandler {

	@SerializedName("message_type")
	private String messageType;
	private String path;
	private List<MessageParameter> parameter;
	
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<MessageParameter> getParameter() {
		return parameter;
	}
	public void setParameter(List<MessageParameter> parameter) {
		this.parameter = parameter;
	}
	
	
}
