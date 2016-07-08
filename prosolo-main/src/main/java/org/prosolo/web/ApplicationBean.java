package org.prosolo.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpSession;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.config.Config;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.web.home.ColleguesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/*
 * @author Zoran Jeremic 2013-05-18
 */
@ManagedBean(name="applicationbean")
@Component("applicationbean")
@Scope("singleton")
public class ApplicationBean implements Serializable {

	private static final long serialVersionUID = 6323655674786060348L;
	
	@Autowired AccessResolver accessResolver;
	
	private BiMap<Long, HttpSession> userSessions;
	private BiMap<HttpSession, Long> sessionIdsUser;
	private String serverIP = null;
	private long loginTime;
	
	public ApplicationBean(){
		userSessions = HashBiMap.create();
		sessionIdsUser = userSessions.inverse();
		loginTime = new Date().getTime();
	}
	
	public void registerNewUserSession(User user, HttpSession session){
		long userId = user.getId();
		
		if (userSessions.containsKey(userId)) {
			userSessions.remove(userId);
		}
		userSessions.put(userId, session);
	}

	public HttpSession unregisterUser(long userId) {
		if (userId == 0) {
			return null;
		}
		
		HttpSession sessionToReturn = null;
		
		if (userSessions.containsKey(userId)) {
			sessionToReturn = userSessions.get(userId);
			userSessions.remove(userId);
		}
		
		return sessionToReturn;
	}
	
	public void unregisterSession(HttpSession session) {
		if (sessionIdsUser.containsKey(session)) {
			sessionIdsUser.remove(session);
		}
	}
	
	public HttpSession getUserSession(long userId) {
		if (userSessions.containsKey(userId)) {
			return userSessions.get(userId);
		} else{
			return null;
		}
	}
	
	public boolean hasUserSession(long userId){
		if(userSessions.containsKey(userId)){
			return true;
		}else return false;
	}
	
	public List<HttpSession> getFollowersHttpSessions(long userId) {
		List<HttpSession> followersSessions = new ArrayList<HttpSession>();
		HttpSession userSession = getUserSession(userId);
		
		if (userSession != null) {
			ColleguesBean colleguesBean = (ColleguesBean) userSession.getAttribute("colleguesBean");
			List<UserData> followers = colleguesBean.getFollowers();
			
			followersSessions = getHttpSessionsOfUsersByUserData(followers);
		}
		return followersSessions;
	}
	
	public List<HttpSession> getHttpSessionsOfUsers(List<User> users) {
		List<HttpSession> usersSessions = new ArrayList<HttpSession>();
		
		if (users != null) {
			for (User user : users) {
				HttpSession userSession = getUserSession(user.getId());
				
				if (userSession != null) {
					usersSessions.add(userSession);
				}
			}
		}
		
		return usersSessions;	
	}
	
	public Map<Long, HttpSession> getHttpSessionsExcludingUsers(List<Long> userIds) {
		Map<Long, HttpSession> subMap = new HashMap<Long, HttpSession>();
		
		for (Entry<Long, HttpSession> entry : userSessions.entrySet()) {
			if (userIds == null || !userIds.contains(entry.getKey()) && entry.getValue() != null) {
				subMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		return subMap;	
	}
	
	public List<HttpSession> getHttpSessionsOfUsersByUserData(List<UserData> users) {
		List<HttpSession> usersSessions = new ArrayList<HttpSession>();
		
		if (users != null) {
			for (UserData user : users) {
				HttpSession userSession = getUserSession(user.getId());
				
				if (userSession != null) {
					usersSessions.add(userSession);
				}
			}
		}
		
		return usersSessions;	
	}
	
	public Map<Long, HttpSession> getAllHttpSessions() {
		 return userSessions;
	}
	
	public Config getConfig() {
		return Settings.getInstance().config;
	}
	public String getServerIp(){
		if(serverIP==null){
			this.serverIP=accessResolver.findServerIPAddress();
		}
		return serverIP;
	}
	public String getDomain(){
		return Settings.getInstance().config.application.domain;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

}

