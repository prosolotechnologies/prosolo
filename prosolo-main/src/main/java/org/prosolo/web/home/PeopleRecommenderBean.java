/**
 * 
 */
package org.prosolo.web.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.es.MoreUsersLikeThis;
import org.prosolo.services.logging.LoggingDBManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name = "peoplerecommend")
@Component("peoplerecommend")
@Scope("session")
public class PeopleRecommenderBean implements Serializable {

	private static final long serialVersionUID = 7048812664979698316L;
	
	private static Logger logger = Logger.getLogger(PeopleRecommenderBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private MoreUsersLikeThis mult;
	@Autowired private CollaboratorsRecommendation cRecommendation;
	@Autowired private LoggingDBManager loggingDBManager;
	
	private List<UserData> locationRecommendedUsers;
	private List<UserData> activityRecommendedUsers;
	private List<UserData> similarityRecommendedUsers;
	
	/*
	 * ACTIONS
	 */
	public void initLocationRecommend() {
		//if (locationRecommendedUsers == null) {
			locationRecommendedUsers = new ArrayList<UserData>();
			List<User> users=cRecommendation.getRecommendedCollaboratorsBasedOnLocation(loggedUser.getUser(), 3);
			 
			if (users != null && !users.isEmpty()) {
				for (User user : users) {
					UserData userData = new UserData(user);
					locationRecommendedUsers.add(userData);
				}
				logger.debug("Location based user recommendations initialized '"+loggedUser.getUser()+"'");
			}
		//}
	}
	
	public String getLocationRecommendedUsersAsJSON() {
	
		if (locationRecommendedUsers != null && loggedUser.getUser().getLocationName() != null) {
			JsonArray mapData = new JsonArray();
			
			JsonArray loggedUserData = new JsonArray();
			loggedUserData.add(new JsonPrimitive(loggedUser.getUser().getId()));
			loggedUserData.add(new JsonPrimitive(Settings.getInstance().config.application.domain + "publicprofile.xhtml?id="+loggedUser.getUser().getId()));
			loggedUserData.add(new JsonPrimitive(loggedUser.getName() + " " + loggedUser.getLastName()));
			if((loggedUser.getUser().getLocationName()!=null && !loggedUser.getUser().getLocationName().equals(""))){
				loggedUserData.add(new JsonPrimitive(loggedUser.getUser().getLocationName()));
			}
			if(loggedUser.getUser().getLatitude()!=null && loggedUser.getUser().getLongitude()!=null){
				loggedUserData.add(new JsonPrimitive(loggedUser.getUser().getLatitude()));
				loggedUserData.add(new JsonPrimitive(loggedUser.getUser().getLongitude()));
			}
			mapData.add(loggedUserData);
			
			if (locationRecommendedUsers != null && !locationRecommendedUsers.isEmpty()) {
				for (UserData locationUserData : locationRecommendedUsers) {
					JsonArray jsonData = new JsonArray();
					jsonData.add(new JsonPrimitive(locationUserData.getId()));
					jsonData.add(new JsonPrimitive(Settings.getInstance().config.application.domain + "publicprofile.xhtml?id="+locationUserData.getId()));
					jsonData.add(new JsonPrimitive(locationUserData.getName()));
					String locName=(locationUserData.getLocationName()!=null ? locationUserData.getLocationName() : "");
						jsonData.add(new JsonPrimitive(locName));
					String lat=(locationUserData.getLatitude()!=null ? locationUserData.getLatitude():"");
					jsonData.add(new JsonPrimitive(lat));
					String lon=(locationUserData.getLongitude()!=null ? locationUserData.getLongitude():"");
					jsonData.add(new JsonPrimitive(lon));
					mapData.add(jsonData);
				}
			}
			return mapData.toString();
		}
		return "[]";
	}
	
	public void initActivityRecommend() {
		if (activityRecommendedUsers == null) {
			logger.debug("Initializing activity based user recommendation for a user '"+loggedUser.getUser()+"'");

			activityRecommendedUsers = new ArrayList<UserData>();
			
			List<User> users=cRecommendation.getMostActiveRecommendedUsers(loggedUser.getUser(), 3);
			if (users != null && !users.isEmpty()) {
				for (User user : users) {
					UserData userData = new UserData(user);
					
					// TODO: Zoran - put here last activity date
					long timestamp=loggingDBManager.getMostActiveUsersLastActivityTimestamp(user.getId());
					userData.setLastAction(new Date(timestamp));
					
					activityRecommendedUsers.add(userData);
				}
				logger.debug("Activity based user recommendations initialized '"+loggedUser.getUser()+"'");
			}
		}
	}
	
	public void initSimilarityRecommend() {
		if (similarityRecommendedUsers == null) {
			similarityRecommendedUsers = new ArrayList<UserData>();
			logger.debug("Initializing similarity based user recommendation for a user '"+loggedUser.getUser()+"'");
			List<User> users=cRecommendation.getRecommendedCollaboratorsBasedOnSimilarity(loggedUser.getUser(), 3);
			if (users != null && !users.isEmpty()) {
				for (User user : users) {
					UserData userData = new UserData(user);
					similarityRecommendedUsers.add(userData);
				}
				logger.debug("Similarity based user recommendations initialized '"+loggedUser.getUser()+"'");
			}
		}
	}
	
	
	/*
	 * GETTERS / SETTERS
	 */

	public List<UserData> getLocationRecommendedUsers() {
		return locationRecommendedUsers;
	}

	public List<UserData> getActivityRecommendedUsers() {
		return activityRecommendedUsers;
	}

	public List<UserData> getSimilarityRecommendedUsers() {
		return similarityRecommendedUsers;
	}
	
}
