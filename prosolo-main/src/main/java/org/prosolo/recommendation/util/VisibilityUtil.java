/**
 * 
 */
package org.prosolo.recommendation.util;

import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Nikola Milikic
 * 
 */
public class VisibilityUtil {

	public static boolean isVisibileToUser(User user, Node resource) {
		if (user != null && resource != null) {
			switch (resource.getVisibility()) {
				case PUBLIC:
					return true;
				case PRIVATE:
					User maker = resource.getMaker();
					return (maker != null) && maker.equals(user);
//				case LIMITED:
//					Collection<Node> visibleToLimited = visibility.getLimitedToEntities();
//					return (visibleToLimited != null) && visibleToLimited.contains(user);
				default:
					return false;
			}
		}
		return false;
	}
	
}
