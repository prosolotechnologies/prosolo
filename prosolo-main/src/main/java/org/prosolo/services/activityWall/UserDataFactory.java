package org.prosolo.services.activityWall;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.util.AvatarUtils;

public class UserDataFactory {

	public static UserData createUserData(User user) {
		UserData userData = new UserData();
		userData.setId(user.getId());
		userData.setFirstName(user.getName());
		userData.setLastName(user.getLastname());
		userData.setName(user.getFullName());
		userData.setAvatarUrl(AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120));
		userData.setFollowed(false);
		
		return userData;
	}
}
