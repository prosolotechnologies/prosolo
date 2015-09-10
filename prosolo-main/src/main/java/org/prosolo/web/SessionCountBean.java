package org.prosolo.web;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.bean.ManagedBean;

import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.logging.AccessResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="sessioncountbean")
@Component("sessioncountbean")
@Scope("singleton")
public class SessionCountBean implements Serializable {

	private static final long serialVersionUID = 4746791809809052163L;
	
	@Autowired
	private AnalyticalServiceCollector collector;
	
	@Autowired
	private AccessResolver resolver;
	
	private Set<String> authenticatedSessions = Collections.synchronizedSet(new HashSet<String>());
	
	public long sessionsCount() {
		return authenticatedSessions.size();
	}
	
	public void addSession(String id) {
		authenticatedSessions.add(id);
		collector.updateInstanceLoggedUserCount(resolver.findServerIPAddress(), Calendar.getInstance()
				.getTimeInMillis(), authenticatedSessions.size());
	}

	public void removeSession(String id) {
		authenticatedSessions.remove(id);
		collector.updateInstanceLoggedUserCount(resolver.findServerIPAddress(), Calendar.getInstance()
				.getTimeInMillis(), authenticatedSessions.size());
	}

}
