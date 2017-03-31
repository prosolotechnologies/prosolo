package org.prosolo.services.nodes.data.resourceAccess;

import java.io.Serializable;

public class ResourceAccessData implements Serializable {

	private static final long serialVersionUID = 4665592641386312458L;
	
	//does user have needed access in specified context
	private boolean canAccess;
	
	//can user edit resource in specified context
	private final boolean canEdit;
	//can user learn resource in specified context
	private final boolean canLearn;
	//can user instruct resource in specified context
	private final boolean canInstruct;
	
	public ResourceAccessData(boolean canAccess, boolean canEdit, boolean canLearn, boolean canInstruct) {
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
	public void userEnrolled() {
		this.canAccess = true;
	}

}
