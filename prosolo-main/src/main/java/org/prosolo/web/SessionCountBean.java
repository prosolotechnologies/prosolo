package org.prosolo.web;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.services.user.ActiveUsersSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "sessioncountbean")
@Component("sessioncountbean")
@Scope("singleton")
public class SessionCountBean implements Serializable {

	private static final long serialVersionUID = 4746791809809052163L;

	@Autowired
	private AnalyticalServiceCollector collector;
	@Inject private ActiveUsersSessionRegistry activeUsersSessionRegistry;

	@Autowired
	private AccessResolver resolver;

	private long sessionsCount() {
		return activeUsersSessionRegistry.getNumberOfActiveSessions();
	}

	public void storeCount() {
		collector.updateInstanceLoggedUserCount(resolver.findServerIPAddress(), Calendar.getInstance()
				.getTimeInMillis(), sessionsCount());
	}

}