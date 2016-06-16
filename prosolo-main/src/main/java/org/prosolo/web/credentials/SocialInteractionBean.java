package org.prosolo.web.credentials;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.app.Settings;
import org.prosolo.config.AnalyticalServerConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@ManagedBean(name = "socialInteractionBean")
@Component("socialInteractionBean")
@Scope("view")
public class SocialInteractionBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String getApiHost() {
		AnalyticalServerConfig config = Settings.getInstance().config.analyticalServerConfig;
		return config.apiHost + ":" + config.apiPort+"/"+config.apiServicesPath;
	}


}
