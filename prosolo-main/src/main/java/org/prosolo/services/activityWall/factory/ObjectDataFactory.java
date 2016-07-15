package org.prosolo.services.activityWall.factory;

import java.util.Locale;

import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.activityWall.impl.data.ObjectData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

@Component
public class ObjectDataFactory {

	public ObjectData getObjectData(long id, String title, ObjectType type, long userId, String firstName, 
			String lastName, Locale locale) {
		ObjectData obj = new ObjectData();
		obj.setId(id);
		obj.setTitle(title);
		obj.setType(type);
		if(userId > 0) {
			UserData user = new UserData(userId, firstName, lastName, null, null, null, false);
			obj.setCreator(user);
		}
		obj.setShortType(ResourceBundleUtil.getResourceType(obj.getType().name(), locale));
		return obj;
	}
}
