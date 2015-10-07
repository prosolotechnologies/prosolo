package org.prosolo.web.lti.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.lti.LtiTool;

/**
 * @author Nikola Milikic
 * @version 0.5
 *			
 */
public class ExternalToolFormData implements Serializable {
	
	private static final long serialVersionUID = 8543622282202374274L;
	
	private String title;
	private String description;
	private String launchUrl;
	private String consumerKey;
	private String consumerSecret;
	private String regUrl;
	
	
	public ExternalToolFormData () {
		
	}
	
	public ExternalToolFormData(LtiTool tool) {
		title = tool.getName();
		description = tool.getDescription();
		launchUrl = tool.getFullLaunchURL();
		consumerKey = tool.getToolSet().getConsumer().getKeyLtiOne();
		consumerSecret = tool.getToolSet().getConsumer().getSecretLtiOne();
		regUrl = tool.getToolSet().getFullRegistrationURL();
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getRegUrl() {
		return regUrl;
	}

	public void setRegUrl(String regUrl) {
		this.regUrl = regUrl;
	}
	
	
}
