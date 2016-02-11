package org.prosolo.web.courses.data;

import org.prosolo.services.nodes.data.ObjectStatus;

public class ObjectStatusTransitions {

	public static ObjectStatus changeTransition(ObjectStatus status) {
		if(status == ObjectStatus.UP_TO_DATE) {
			return ObjectStatus.CHANGED;
		}
		return status;
	}
}
