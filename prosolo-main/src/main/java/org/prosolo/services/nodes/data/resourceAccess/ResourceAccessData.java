package org.prosolo.services.nodes.data.resourceAccess;

import java.io.Serializable;

public class ResourceAccessData implements Serializable {

	private static final long serialVersionUID = 4665592641386312458L;
	
	//the lowest possible privilege - it means that user is allowed to see resource in read only mode 
	private final boolean canRead;
	//does user have needed access in specified context
	private boolean canAccess;
	
	//can user edit resource in specified context
	private final boolean canEdit;
	//can user learn resource in specified context
	private final boolean canLearn;
	//can user instruct resource in specified context
	private final boolean canInstruct;
	
	public ResourceAccessData(boolean canRead, boolean canAccess, boolean canEdit, boolean canLearn, boolean canInstruct) {
		this.canRead = canRead;
		this.canAccess = canAccess;
		this.canEdit = canEdit;
		this.canLearn = canLearn;
		this.canInstruct = canInstruct;
	}

	public boolean isCanAccess() {
		return canAccess;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public boolean isCanLearn() {
		return canLearn;
	}

	public boolean isCanInstruct() {
		return canInstruct;
	}
	
	/**
	 * This method provide the only way to change this object. 
	 * Call this method when user enrolls in a resource and resource data object
	 * should reflect that change.
	 */
	/*
	TODO semantics changed for this object - if user is enrolled that does not mean canAccess should
	be true - change in all places logic for checking if user has access - logic should be:
	if canAccess == true or user is enrolled
	 */
	public void userEnrolled() {
		this.canAccess = true;
	}

	public boolean isCanRead() {
		return canRead;
	}

}
