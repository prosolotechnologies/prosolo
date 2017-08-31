package org.prosolo.web.logging;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
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

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//import com.mongodb.BasicDBObject;
//import com.mongodb.util.JSON;

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
	
	// added for new context approach
	private String page;
	private String learningContext;
	private String service;
	
	/*
	 * ACTIONS
	 */
	
	public void logEmailNavigation(UserContextData context, String link) {
		try {
			loggingService.logEmailNavigation(context, link, null, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void logServiceUse(ComponentName component, String parm1, String value1, String parm2, String value2) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		
		try {
			loggingService.logServiceUse(loggedUser.getUserContext(), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logServiceUse(ComponentName component, String query){
		try {
			Map<String, String> params = new HashMap<>();
			params.put("query", query);
			loggingService.logServiceUse(loggedUser.getUserContext(), component, null, params, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void logServiceUse(ComponentName component, String parm1, String value1, String parm2, 
			String value2, String parm3, String value3) {
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		parameters.put(parm3, value3);
		
		try {
			loggingService.logServiceUse(loggedUser.getUserContext(), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logEventWithIp(EventType eventType, String ipAdress, Map<String, String> parameters) {
		logEventWithIp(eventType, loggedUser.getUserContext(), ipAdress, parameters);
	}

	public void logEventWithIp(EventType eventType, UserContextData context, String ipAddress,
							   Map<String, String> params) {
		try {
			loggingService.logEventObserved(eventType, context, null, 0, null, null, params, ipAddress);
		} catch (LoggingException e) {
			logger.error("Error", e);
		}
	}
	
	public void submitPageNavigation(){
		try {
			loggingService.logNavigationFromContext(loggedUser.getUserContext(), link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void submitTabNavigation(){
		try {
			loggingService.logTabNavigationFromContext(loggedUser.getUserContext(), link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void submitServiceUse(){
		try {
			Map<String, String> params = convertToMap(parameters);
			params.put("objectType", component);
			eventFactory.generateEvent(EventType.SERVICEUSE, loggedUser.getUserContext(), null, null,
					null, params);
			//loggingService.logServiceUse(loggedUser.getUser(), component, parameters, getIpAddress());
		} catch (EventException e) {
			logger.error(e);
		}
	}
	
	private String getIpAddress() {
		String ipAddress = loggedUser.getIpAddress();
		
		if (loggedUser.isLoggedIn()) {
			ipAddress = accessResolver.findRemoteIPAddress();
		}
		return ipAddress;
	}
	
	private Map<String, String> convertToMap(String parametersJson) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		if (parametersJson != null && parametersJson.length() > 0) {
			try{
				JSONObject parametersObject = (JSONObject) new JSONParser().parse(parametersJson);
				Set<String> keys = parametersObject.keySet();

				for (String key : keys) {
					parameters.put(key, parametersObject.get(key).toString());
				}
			}catch(ParseException pe){
				logger.error(pe);
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

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getLearningContext() {
		return learningContext;
	}

	public void setLearningContext(String learningContext) {
		this.learningContext = learningContext;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

}
