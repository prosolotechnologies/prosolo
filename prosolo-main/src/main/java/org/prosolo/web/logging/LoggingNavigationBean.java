package org.prosolo.web.logging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * @author Zoran Jeremic 2013-10-13
 *
 */
@ManagedBean(name = "loggingBean")
@Component("loggingBean")
@Scope("request")
public class LoggingNavigationBean implements Serializable {
	
	private static final long serialVersionUID = -2212469354561972959L;
	
	private Logger logger = Logger.getLogger(LoggingNavigationBean.class);

	@Autowired private LoggingService loggingService;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private AccessResolver accessResolver;
	@Inject private EventFactory eventFactory;
	
	private long userId;
	private User user;
	private String link;
	private String context;
	private String component;
	private String parameters;
	
	/*
	 * ACTIONS
	 */
	
	public void logPageNavigation(User user, String link){
		try {
			loggingService.logNavigationFromContext(user, link, null, null, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void logPageNavigationFromContext(User user, String link, String context){
		try {
			loggingService.logNavigationFromContext(user,link, context, null, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void logServiceUseWithIp(String ipAddress, ComponentName component, String... parms) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		for (int i = 0; i < parms.length; i+=2) {
			if (parms[i] != null && parms[i+1] != null)
				parameters.put(parms[i], parms[i+1]);
		}
		
		try {
			loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, ipAddress);
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
//	// this can not be called from the xhtml page
//	public void logServiceUse(ComponentName component, String... parms) {
//		Map<String, String> parameters = new HashMap<String, String>();
//		
//		for (int i = 0; i < parms.length; i+=2) {
//			if (parms[i] != null && parms[i+1] != null)
//				parameters.put(parms[i], parms[i+1]);
//		}
//		
//		loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, getIpAddress());  
//	}
	
	public void logServiceUse(ComponentName component, String parm1, String value1) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parm1, value1);
		
		try {
			loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logServiceUse(ComponentName component, String parm1, String value1, String parm2, String value2) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		
		try {
			loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logServiceUse(ComponentName component, String parm1, String value1, String parm2, 
			String value2, String parm3, String value3) {
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		parameters.put(parm3, value3);
		
		try {
			loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logServiceUse(ComponentName component, String ipAddress, String parm1, String value1, String parm2, 
			String value2, String parm3, String value3) {
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		parameters.put(parm3, value3);
		
		try {
			loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, ipAddress);
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logServiceUse(ComponentName component, String parm1, String value1, String parm2, 
			String value2, String parm3, String value3, String parm4, String value4) {
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		parameters.put(parm3, value3);
		parameters.put(parm4, value4);
		
		try {
			loggingService.logServiceUse(loggedUser.getUser(), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logEvent(EventType eventType, Map<String, String> parameters) {
		loggingService.logEventObserved(eventType, loggedUser.getUser(), null, 0, parameters, getIpAddress());
	}
	
	public void logEvent(EventType eventType, String objectType, Map<String, String> parameters) {
		loggingService.logEventObserved(eventType, loggedUser.getUser(), objectType, 0, parameters, getIpAddress());
	}
	
	public void logEvent(EventType eventType, String objectType, long objectId, Map<String, String> parameters) {
		loggingService.logEventObserved(eventType, loggedUser.getUser(), objectType, objectId, parameters, getIpAddress());
	}
	
	public void submitPageNavigation(){
		try {
			loggingService.logNavigationFromContext(loggedUser.getUser(), link, context, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void submitTabNavigation(){
		try {
			loggingService.logTabNavigationFromContext(loggedUser.getUser(), link, context, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void submitServiceUse(){
		try {
			Map<String, String> params = convertToMap(parameters);
			params.put("objectType", component);
			eventFactory.generateEvent(EventType.SERVICEUSE, loggedUser.getUser(), null, params);
			//loggingService.logServiceUse(loggedUser.getUser(), component, parameters, getIpAddress());
		} catch (EventException e) {
			logger.error(e);
		}
	}

	private String getIpAddress() {
		String ipAddress = loggedUser.getIpAddress();
		
		if (loggedUser.getUser() == null) {
			ipAddress = accessResolver.findRemoteIPAddress();
		}
		return ipAddress;
	}
	
	private Map<String, String> convertToMap(String parametersJson) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		if (parametersJson != null && parametersJson.length() > 0) {
			BasicDBObject parametersObject = (BasicDBObject) JSON.parse(parametersJson);
			Set<String> keys = parametersObject.keySet();
			
			for (String key : keys) {
				parameters.put(key, parametersObject.getString(key));
			}
		}
		return parameters;
	}
	
	/* 
	 * GETTERS / SETTERS
	 */

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
