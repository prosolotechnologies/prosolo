package org.prosolo.web.activitywall.data;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.web.activitywall.data.UserData; 
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.common.util.ImageFormat;
 

/**
 * @author Zoran Jeremic, Sep 19, 2015
 *
 */
public class UserDataFactory {

	public static UserData createUserData(User user) {
		if (user != null){
			UserData userData=new UserData();
			userData.setId(user.getId());
			userData.setName(user.getName() + ((user.getLastname() != null) ? " " + user.getLastname() : ""));
			userData.setProfileUrl(user.getProfileUrl());
			userData.setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(user, ImageFormat.size120x120));
			
			if (user.getUserType().equals(UserType.TWITTER_USER)) {
				userData.setPosition("Twitter User");
				userData.setExternalUser(true);
			} else {
				userData.setPosition(user.getPosition());
			}
			
			// location
			userData.setLocationName(user.getLocationName());
			if(user.getLatitude()!=null)
				userData.setLatitude(String.valueOf(user.getLatitude()));
			if(user.getLongitude()!=null)
				userData.setLongitude(String.valueOf(user.getLongitude()));
			return userData;
		}
		return null;
		
	}
	public static UserData createUserData(User user, ImageFormat imageFormat) {
		UserData userData=createUserData(user);
		userData.setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(user, imageFormat));
		return userData;
	}
 
}
