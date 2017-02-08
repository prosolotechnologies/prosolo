package org.prosolo.services.nodes.data;

import java.io.Serializable;

public class ResourceAccessData implements Serializable {

	private static final long serialVersionUID = 4665592641386312458L;
	
	private boolean canAccess;
	private boolean canEdit;
	
	public ResourceAccessData() {
		
	}
	
	public ResourceAccessData(boolean canAccess, boolean canEdit) {
		this.canAccess = canAccess;
		this.canEdit = canEdit;
	}
	
	public boolean isCanAccess() {
		return canAccess;
	}
	
	public void setCanAccess(boolean canAccess) {
		this.canAccess = canAccess;
	}
	
	public boolean isCanEdit() {
		return canEdit;
	}
	
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

}
