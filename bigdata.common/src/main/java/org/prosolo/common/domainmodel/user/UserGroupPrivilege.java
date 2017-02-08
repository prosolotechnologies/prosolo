package org.prosolo.common.domainmodel.user;

public enum UserGroupPrivilege {

	None,
	View,
	Edit;
	
	public boolean isPrivilegeIncluded(UserGroupPrivilege priv) {
		switch(this) {
			case None:
				return true;
			case View:
				return priv == View || priv == Edit;
			case Edit:
				return priv == Edit;
		}
		return false;
	}
}
