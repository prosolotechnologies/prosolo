package org.prosolo.services.nodes.data;

public class ObjectStatusTransitions {

	public static ObjectStatus changeTransition(ObjectStatus status) {
		if(status == ObjectStatus.UP_TO_DATE) {
			return ObjectStatus.CHANGED;
		}
		return status;
	}
	
	public static ObjectStatus upToDateTransition(ObjectStatus status) {
		if(status == ObjectStatus.CHANGED || status == ObjectStatus.REMOVED) {
			return ObjectStatus.UP_TO_DATE;
		}
		return status;
	}
	
	public static ObjectStatus removeTransition(ObjectStatus status) {
		if(status == ObjectStatus.UP_TO_DATE || status == ObjectStatus.CHANGED) {
			return ObjectStatus.REMOVED;
		}
		return status;
	}
}
