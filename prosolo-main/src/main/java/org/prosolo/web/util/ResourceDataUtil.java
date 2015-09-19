/**
 * 
 */
package org.prosolo.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.prosolo.common.web.activitywall.data.UserData;

/**
 * @author "Nikola Milikic"
 *
 */
public class ResourceDataUtil {

	public static Collection<Long> getUserIds(List<UserData> userDataList) {
		List<Long> users = new ArrayList<Long>();
		
		if (userDataList != null && !userDataList.isEmpty()) {
			for (UserData userData : userDataList) {
				users.add(userData.getId());
			}
		}
		return users;
	}
}
