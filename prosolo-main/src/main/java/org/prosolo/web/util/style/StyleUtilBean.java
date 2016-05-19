package org.prosolo.web.util.style;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.nodes.data.ActivityType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "styleUtilBean")
@Component("styleUtilBean")
@Scope("request")
public class StyleUtilBean implements Serializable {

	private static final long serialVersionUID = 3275340449093388469L;

	public String getStyleClassBasedOnActivityType(ActivityType type) {
		switch(type) {
			case TEXT:
				return "activityText";
			case VIDEO:
				return "activityVideo";
			case SLIDESHARE:
				return "activitySlide";
			case EXTERNAL_TOOL:
				return "activityExternal";
			default:
				return "";	
		}
	}
	
	public String getStyleClassBasedOnNotificationType(NotificationType type) {
		switch(type) {
			case Follow_User:
				return "notifFollowed";
			case Comment:
				return "notifComment";
			case Comment_Like:
				return "notifLikeComment";
			case Assessment_Given:
				return "notifAssessment";
			case Mention:
				return "notifMention";
			default:
				return "";	
		}
	}

}
