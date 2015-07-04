package org.prosolo.web.activitywall;

import javax.faces.bean.ManagedBean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
@author Zoran Jeremic Dec 13, 2014
*
*/

@ManagedBean(name = "ltiBean")
@Component("ltiBean")
@Scope("view")
public class LtiBean {

	public String launchUrl;
	public String sharedSecret;
	public String consumerKey;
	public long activityId;
	public long targetActivityId;

	public long getTargetActivityId() {
		return targetActivityId;
	}

	public void setTargetActivityId(long targetActivityId) {
		this.targetActivityId = targetActivityId;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}
	
}

