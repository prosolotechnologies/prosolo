package org.prosolo.common.domainmodel.lti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

@Entity
public class LtiService extends BaseLtiEntity{

	private static final long serialVersionUID = -8667916021936674286L;
	
	private String endpoint;
	private String formats;
	private List<String> formatList;
	private String actions;
	private List<String> actionList;
	private LtiConsumer consumer;
	
	public LtiService(){
		formatList = new ArrayList<>();
		actionList = new ArrayList<>();
	}


	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Column (length = 1000)
	public String getFormats() {
		if(formatList != null){
			formats = StringUtils.join(actionList, ',');
		}
		return formats;
	}

	public void setFormats(String formats) {
		this.formats = formats;
		if(formats != null){
			formatList = new ArrayList<String>(Arrays.asList(formats.split(",")));
		}
	}

	@Transient
	public List<String> getFormatList() {
		return formatList;
	}

	public void setFormatList(List<String> formatList) {
		this.formatList = formatList;
	}

	public String getActions() {
		if(actionList != null){
			actions = StringUtils.join(actionList, ',');
		}
		return actions;
	}
	
	public void setActions(String actions) {
		this.actions = actions;
		if(actions != null){
			actionList = new ArrayList<String>(Arrays.asList(actions.split(",")));
		}
	}

	@Transient
	public List<String> getActionList() {
		return actionList;
	}

	public void setActionList(List<String> actions) {
		this.actionList = actions;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public LtiConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(LtiConsumer consumer) {
		this.consumer = consumer;
	}

	

}