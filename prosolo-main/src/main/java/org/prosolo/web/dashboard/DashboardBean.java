package org.prosolo.web.dashboard;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ManagedBean(name = "dashboardBean")
@Component("dashboardBean")
@Scope("view")
public class DashboardBean implements Serializable {

	private static final long serialVersionUID = -117805452735249654L;

	private static Logger logger = Logger.getLogger(DashboardBean.class.getName());

	@Autowired
	private LoggedUserBean loggedUser;

	@Autowired
	private AccessResolver accessResolver;

	@Autowired
	private EventFactory eventFactory;

	public int getTotalUsers() {
		return 0;
	}

	public String getTotalUsersPercent() {
		return "0%";
	}

	public int getActiveUsers() {
		return 0;
	}

	public String getActiveUsersPercent() {
		return "0%";
	}

	public long getCurrentlyLoggedIn() {
		return 0;
	}

	public String getNoResultsFoundMessage() {
		return "No results found for given parameters.";
	}
	
	public String getSystemNotAvailableMessage() {
		return "System not available.";
	}

	private String getIpAddress() {
		return loggedUser.isLoggedIn() ? accessResolver.findRemoteIPAddress() : loggedUser.getIpAddress();
	}

	public void disableHashtag() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		String hashtag = context.getRequestParameterMap().get("disable-form:hashtag-to-disable");

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("hashtag", hashtag);
		parameters.put("ip", getIpAddress());
		//TODO what to do with organization id
		eventFactory.generateAndPublishEvent(EventType.HASHTAG_DISABLED, loggedUser.getUserContext(), null, null, null, parameters);
	}

	public void enableHashtag() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		String hashtag = context.getRequestParameterMap().get("enable-form:hashtag-to-enable");

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("hashtag", hashtag);
		parameters.put("ip", getIpAddress());
		//TODO what to do with organization id since this is admin section and admins do not have to have organizationId in session
		eventFactory.generateAndPublishEvent(EventType.HASHTAG_ENABLED, loggedUser.getUserContext(), null, null, null, parameters);
	}

}
