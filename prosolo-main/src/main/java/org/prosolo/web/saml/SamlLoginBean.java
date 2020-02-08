package org.prosolo.web.saml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.prosolo.app.Settings;
import org.prosolo.config.app.SAMLIdentityProviderInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "samlLoginBean")
@Component("samlLoginBean")
@Scope("request")
public class SamlLoginBean implements Serializable {

	private static final long serialVersionUID = 1013750139171515348L;

	//private static Logger logger = Logger.getLogger(SamlIDPSelectionBean.class);
	
	private List<SAMLIdentityProviderInfo> identityProviders;

	@PostConstruct
	public void init() {
		identityProviders = Settings.getInstance().config.application.registration.samlConfig.samlProviders;

		if (identityProviders == null) {
			identityProviders = new ArrayList<>();
		} else {
			identityProviders = identityProviders.stream()
					.filter(ip -> ip.isEnabled())
					.collect(Collectors.toList());
		}
	}

	public List<SAMLIdentityProviderInfo> getIdentityProviders() {
		return identityProviders;
	}

}
