/**
 * 
 */
package org.prosolo.web.communications.util;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.web.activitywall.data.UserData;

/**
 * @author "Nikola Milikic"
 *
 */
public class UserDataConverter {

	public static List<UserData> convertUsers(List<User> users) {
		List<UserData> usersData = new ArrayList<UserData>();
		
		if (users != null && !users.isEmpty()) {
			for (User user : users) {
				usersData.add(new UserData(user));
			}
		}
		return usersData;
	}
	
}
