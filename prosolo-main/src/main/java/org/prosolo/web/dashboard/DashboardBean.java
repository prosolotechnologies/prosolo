package org.prosolo.web.dashboard;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.app.Settings;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "dashboardBean")
@Component("dashboardBean")
@Scope("view")
public class DashboardBean implements Serializable {

	private static final long serialVersionUID = -117805452735249654L;

	public String getApiHost() {
		return Settings.getInstance().config.analyticalServerConfig.apiHost
				+ ":"
				+ Settings.getInstance().config.analyticalServerConfig.apiPort;
	}
	
	public String getNoResultsFoundMessage() {
		return "No results found for given parameters.";
	}
	
}
