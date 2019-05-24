package org.prosolo.web.logging;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
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
	
	private User user;

	@Getter @Setter private String link;
	@Getter @Setter private String component;
	@Getter @Setter private String parameters;
	@Getter @Setter private String page;
	@Getter @Setter private String learningContext;
	@Getter @Setter private String service;
	
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
		Map<String, String> parameters = new HashMap<>();
		parameters.put(parm1, value1);
		parameters.put(parm2, value2);
		
		try {
			loggingService.logServiceUse(loggedUser.getUserContext(new PageContextData(page, learningContext, service)), component, link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}  
	}
	
	public void logServiceUse(ComponentName component, String query){
		try {
			Map<String, String> params = new HashMap<>();
			params.put("query", query);
			loggingService.logServiceUse(loggedUser.getUserContext(new PageContextData(page, learningContext, service)), component, null, params, getIpAddress());
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
			loggingService.logServiceUse(loggedUser.getUserContext(new PageContextData(page, learningContext, service)), component, link, parameters, getIpAddress());
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
			loggingService.logNavigationFromContext(loggedUser.getUserContext(new PageContextData(page, learningContext, service)), link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void submitTabNavigation(){
		try {
			loggingService.logTabNavigationFromContext(loggedUser.getUserContext(new PageContextData(page, learningContext, service)), link, parameters, getIpAddress());
		} catch (LoggingException e) {
			logger.error(e);
		}
	}
	
	public void submitServiceUse(){
		Map<String, String> params = convertToMap(parameters);
		params.put("objectType", component);
		eventFactory.generateEvent(EventType.SERVICEUSE, loggedUser.getUserContext(new PageContextData(page, learningContext, service)), null, null,
				null, params);
		//loggingService.logServiceUse(loggedUser.getUser(), component, parameters, getIpAddress());
	}
	
	private String getIpAddress() {
		String ipAddress = loggedUser.getIpAddress();
		
		if (loggedUser.isLoggedIn()) {
			ipAddress = accessResolver.findRemoteIPAddress();
		}
		return ipAddress;
	}
	
	private Map<String, String> convertToMap(String parametersJson) {
		Map<String, String> parameters = new HashMap<>();
		
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

}
