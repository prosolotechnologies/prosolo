package org.prosolo.services.util.page;

import org.prosolo.common.domainmodel.user.notifications.ObjectType;

public class ObjectToPageMapper {

	public static String getViewPageForObjectType(ObjectType type) {
		switch(type) {
			case Activity:
				return "activity";
			case Competence:
				return "competence";
			case Credential:
				return "credential";
			default:
				return "notFound";
		}
	}
}
